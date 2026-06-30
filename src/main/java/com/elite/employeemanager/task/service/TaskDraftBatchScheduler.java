package com.elite.employeemanager.task.service;

import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.msteams.service.TeamsService;
import com.elite.employeemanager.task.entity.TaskDraftBatch;
import com.elite.employeemanager.task.repository.TaskDraftBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskDraftBatchScheduler {

    private final TaskDraftBatchRepository taskDraftBatchRepository;
    private final EmployeeRepository employeeRepository;
    private final TeamsService teamsService;

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    /**
     * Fires at 8:00 PM IST every day.
     * Finds all OPEN draft batches created today and sends a reminder to Teams.
     * Marks each batch as REMINDED so it is not sent again.
     */
    @Scheduled(cron = "0 0 20 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void sendDailyReminders() {
        LocalDateTime startOfToday = LocalDate.now(IST).atStartOfDay();

        List<TaskDraftBatch> openBatches =
                taskDraftBatchRepository.findAllByStatusAndCreatedAtAfter("OPEN", startOfToday);

        if (openBatches.isEmpty()) {
            log.info("8 PM Reminder: No open draft batches found for today. Skipping.");
            return;
        }

        log.info("8 PM Reminder: Found {} open draft batch(es). Sending reminders...", openBatches.size());

        for (TaskDraftBatch batch : openBatches) {
            try {
                String senderName = resolveSenderName(batch.getCreatedBy());
                String message = buildReminderMessage(batch, senderName);

                teamsService.postMessage(
                        "⏰ Staged Tasks Reminder — Action Required",
                        message,
                        "System Scheduler",
                        batch.getTeamsGroupId(),
                        batch.getTeamsChannelId()
                );

                batch.setStatus("REMINDED");
                batch.setReminderSentAt(LocalDateTime.now(IST));
                taskDraftBatchRepository.save(batch);

                log.info("Reminder sent for draft batch id={} (created by {})", batch.getId(), senderName);
            } catch (Exception e) {
                log.error("Failed to send reminder for draft batch id={}. Skipping.", batch.getId(), e);
                // Continue with next batch — don't let one failure block others
            }
        }
    }

    private String resolveSenderName(Long createdById) {
        if (createdById == null) return "Unknown";
        return employeeRepository.findById(createdById)
                .map(Employee::getName)
                .orElse("Unknown (ID: " + createdById + ")");
    }

    private String buildReminderMessage(TaskDraftBatch batch, String creatorName) {
        return "<b>The following staged tasks were not published today by "
                + creatorName + ".</b><br/>"
                + "Please review and publish them as soon as possible.<br/><br/>"
                + batch.getTeamsMessage();
    }
}
