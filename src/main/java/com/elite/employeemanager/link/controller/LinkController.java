package com.elite.employeemanager.link.controller;

import com.elite.employeemanager.link.entity.Link;
import com.elite.employeemanager.link.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/links")
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @PostMapping
    @PreAuthorize("hasAuthority('LINK_CREATE')")
    public ResponseEntity<Link> createLink(@RequestBody Link link) {
        return new ResponseEntity<>(linkService.createLink(link), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LINK_VIEW')")
    public ResponseEntity<Page<Link>> getAllLinks(
            @RequestParam(required = false) String filename,
            @RequestParam(required = false) Long createdBy,
            @RequestParam(required = false) LocalDate createdAt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        // Validate sort field to prevent SQL/field injection or errors
        if (!List.of("filename", "createdBy", "createdAt").contains(sortBy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort field. Must be one of: filename, createdBy, createdAt");
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return new ResponseEntity<>(linkService.getLinks(filename, createdBy, createdAt, pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LINK_VIEW')")
    public ResponseEntity<Link> getLinkById(@PathVariable Long id) {
        return new ResponseEntity<>(linkService.getLinkById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('LINK_UPDATE')")
    public ResponseEntity<Link> updateLink(@PathVariable Long id, @RequestBody Link details) {
        return new ResponseEntity<>(linkService.updateLink(id, details), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LINK_DELETE')")
    public ResponseEntity<String> deleteLink(@PathVariable Long id) {
        linkService.deleteLink(id);
        return new ResponseEntity<>("Link deleted successfully", HttpStatus.OK);
    }
}
