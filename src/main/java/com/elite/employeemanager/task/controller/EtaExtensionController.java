package com.elite.employeemanager.task.controller;

import com.elite.employeemanager.task.entity.EtaExtension;
import com.elite.employeemanager.task.service.EtaExtensionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/eta-extensions")
@RequiredArgsConstructor
public class EtaExtensionController {

    private final EtaExtensionService etaExtensionService;

    @PostMapping
    @PreAuthorize("hasAuthority('TASK_VIEW')")
    public ResponseEntity<EtaExtension> createEtaExtensionRequest(@RequestBody EtaExtension etaExtension){
        return new ResponseEntity<>(etaExtensionService.createEtaExtensionRequest(etaExtension), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TASK_VIEW')")
    public ResponseEntity<EtaExtension> getEtaExtensionById(@PathVariable Long id){
        return new ResponseEntity<>(etaExtensionService.getEtaExtensionById(id),HttpStatus.OK);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('TASK_UPDATE')")
    public ResponseEntity<EtaExtension> approveEtaExtensionRequest(@PathVariable Long id){
        return new ResponseEntity<>(etaExtensionService.approveEtaExtensionRequest(id),HttpStatus.OK);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('TASK_UPDATE')")
    public ResponseEntity<EtaExtension> rejectEtaExtensionRequest(@PathVariable Long id, @RequestBody String reason){
        return new ResponseEntity<>(etaExtensionService.rejectEtaExtensionRequest(id,reason),HttpStatus.OK);
    }

    @PatchMapping("/{id}/undo")
    @PreAuthorize("hasAuthority('TASK_UPDATE')")
    public ResponseEntity<EtaExtension> undoDecision(@PathVariable Long id){
        return new ResponseEntity<>(etaExtensionService.undoDecision(id),HttpStatus.OK);
    }
}
