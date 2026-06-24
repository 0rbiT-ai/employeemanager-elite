package com.elite.employeemanager.timesheet.repository;

import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.timesheet.entity.TimesheetEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimesheetEntryRepository extends
        JpaRepository<TimesheetEntry,Long>,
        JpaSpecificationExecutor<TimesheetEntry> {

    public List<TimesheetEntry> findByEmployeeId(Long employeeId);

    List<TimesheetEntry> findByTask(Task task);

    List<TimesheetEntry> findByEmployeeAndWorkDate(Employee employee, LocalDate date);
}
