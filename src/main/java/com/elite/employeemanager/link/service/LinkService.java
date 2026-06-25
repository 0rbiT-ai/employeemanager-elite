package com.elite.employeemanager.link.service;

import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.link.entity.Link;
import com.elite.employeemanager.link.repository.LinkRepository;
import com.elite.employeemanager.team.entity.TeamEmployee;
import com.elite.employeemanager.team.repository.TeamEmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final SecurityUtils securityUtils;
    private final EmployeeRepository employeeRepository;
    private final TeamEmployeeRepository teamEmployeeRepository;

    @Transactional
    public Link createLink(Link link) {
        if (link.getFilename() == null || link.getFilename().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filename is required");
        }
        if (link.getFilelink() == null || link.getFilelink().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filelink is required");
        }
        return linkRepository.save(link);
    }

    @Transactional(readOnly = true)
    public Page<Link> getLinks(String filename, Long createdBy, LocalDate createdAtDate, Pageable pageable) {
        Specification<Link> spec = Specification.unrestricted();

        if (filename != null && !filename.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("filename")), "%" + filename.toLowerCase() + "%")
            );
        }

        if (createdBy != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("createdBy"), createdBy)
            );
        }

        if (createdAtDate != null) {
            LocalDateTime startOfDay = createdAtDate.atStartOfDay();
            LocalDateTime endOfDay = createdAtDate.plusDays(1).atStartOfDay();
            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("createdAt"), startOfDay, endOfDay)
            );
        }

        return linkRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Link getLinkById(Long id) {
        return linkRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Link not found"));
    }

    @Transactional
    public Link updateLink(Long id, Link details) {
        Link link = getLinkById(id);

        checkOwnershipOrPrivilege(link, "update");

        if (details.getFilename() == null || details.getFilename().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filename is required");
        }
        if (details.getFilelink() == null || details.getFilelink().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filelink is required");
        }

        link.setFilename(details.getFilename());
        link.setFilelink(details.getFilelink());
        return linkRepository.save(link);
    }

    @Transactional
    public void deleteLink(Long id) {
        Link link = getLinkById(id);

        checkOwnershipOrPrivilege(link, "delete");

        linkRepository.delete(link);
    }

    private void checkOwnershipOrPrivilege(Link link, String action) {
        Employee currentEmployee = securityUtils.getCurrentEmployee();

        // 1. Admins can update/delete any link
        if (currentEmployee.getRoles().contains("ADMIN")) {
            return;
        }

        // 2. The creator (matched by User ID) can update/delete their own link
        if (Objects.equals(link.getCreatedBy(), currentEmployee.getUser().getId())) {
            return;
        }

        // 3. Leads (TEAM_LEAD or SUB_LEAD) of the creator employee can update/delete
        Employee creatorEmployee = employeeRepository.findByUserId(link.getCreatedBy()).orElse(null);
        if (creatorEmployee != null) {
            List<TeamEmployee> creatorTeams = teamEmployeeRepository.findByEmployee(creatorEmployee);
            boolean isCreatorLead = creatorTeams.stream()
                    .map(TeamEmployee::getTeam)
                    .anyMatch(team ->
                            (team.getLead() != null && team.getLead().getId().equals(currentEmployee.getId())) ||
                            (team.getSubLead() != null && team.getSubLead().getId().equals(currentEmployee.getId()))
                    );
            if (isCreatorLead) {
                return;
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to " + action + " this link");
    }
}
