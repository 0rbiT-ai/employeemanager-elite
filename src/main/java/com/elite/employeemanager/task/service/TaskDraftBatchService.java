package com.elite.employeemanager.task.service;

import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;
import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.msteams.service.TeamsService;
import com.elite.employeemanager.task.entity.TaskDraftBatch;
import com.elite.employeemanager.task.repository.TaskDraftBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskDraftBatchService {
    private final TaskDraftBatchRepository taskDraftBatchRepository;
    private final SecurityUtils securityUtils;
    private final TeamsService teamsService;

    @Transactional
    public void saveDraft(String teamsMessage, String teamsGroupId, String teamsChannelId) {
        if (teamsMessage == null || teamsMessage.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teams message cannot be empty");
        }
        User user = securityUtils.getCurrentUser();
        taskDraftBatchRepository.findByCreatedByAndStatus(user.getId(), "OPEN")
                .ifPresentOrElse(taskDraftBatch -> {
                            taskDraftBatch.setTeamsMessage(teamsMessage);
                            taskDraftBatch.setTeamsGroupId(teamsGroupId);
                            taskDraftBatch.setTeamsChannelId(teamsChannelId);
                            taskDraftBatchRepository.save(taskDraftBatch);
                        },
                        () -> {
                            TaskDraftBatch newDraft = TaskDraftBatch.builder()
                                    .status("OPEN")
                                    .teamsMessage(teamsMessage)
                                    .teamsGroupId(teamsGroupId)
                                    .teamsChannelId(teamsChannelId)
                                    .build();
                            taskDraftBatchRepository.save(newDraft);
                        });

    }

    @Transactional(readOnly = true)
    public Optional<TaskDraftBatch> getMyDraft() {
        return taskDraftBatchRepository.findByCreatedByAndStatus(securityUtils.getCurrentUser().getId(), "OPEN");
    }

    private TaskDraftBatch getMyDraftOrThrow() {
        return getMyDraft()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No open drafts for current user"));
    }

    @Transactional
    public void deleteDraft() {
        TaskDraftBatch batch = getMyDraftOrThrow();
        batch.setStatus("DISCARDED");
        taskDraftBatchRepository.save(batch);
    }

    @Transactional
    public void sendToTeams(){
        TaskDraftBatch batch = getMyDraftOrThrow();
        if (batch.getTeamsGroupId() == null || batch.getTeamsGroupId().isBlank() ||
                batch.getTeamsChannelId() == null || batch.getTeamsChannelId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teams Group ID and Channel ID are required to send the draft to Teams");
        }
        teamsService.postMessage("Today's Task Draft",batch.getTeamsMessage(),securityUtils.getCurrentEmployee().getName(),batch.getTeamsGroupId(), batch.getTeamsChannelId());
        batch.setStatus("PUBLISHED");
        taskDraftBatchRepository.save(batch);
    }
}
