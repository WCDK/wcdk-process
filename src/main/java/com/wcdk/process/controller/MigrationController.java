package com.wcdk.process.controller;

import com.wcdk.process.dto.ApiResponse;
import com.wcdk.process.migration.MigrationRequest;
import com.wcdk.process.migration.MigrationResult;
import com.wcdk.process.migration.MigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/migration")
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class MigrationController {

    private final MigrationService migrationService;

    @PostMapping("/migrate")
    public Mono<ApiResponse<MigrationResult>> migrateProcessInstance(@RequestBody MigrationRequest request) {
        return migrationService.migrateProcessInstance(request)
                .map(result -> ApiResponse.success(result));
    }

    @PostMapping("/definition/{id}/suspend")
    public Mono<ApiResponse<Void>> suspendDefinition(@PathVariable String id) {
        return migrationService.suspendProcessDefinition(id)
                .then(Mono.just(ApiResponse.success(null)));
    }

    @PostMapping("/definition/{id}/activate")
    public Mono<ApiResponse<Void>> activateDefinition(@PathVariable String id) {
        return migrationService.activateProcessDefinition(id)
                .then(Mono.just(ApiResponse.success(null)));
    }

    @PostMapping("/execution/{id}/suspend")
    public Mono<ApiResponse<Void>> suspendExecution(@PathVariable String id) {
        return migrationService.suspendExecution(id)
                .then(Mono.just(ApiResponse.success(null)));
    }

    @PostMapping("/execution/{id}/activate")
    public Mono<ApiResponse<Void>> activateExecution(@PathVariable String id) {
        return migrationService.activateExecution(id)
                .then(Mono.just(ApiResponse.success(null)));
    }

    @PostMapping("/task/{id}/suspend")
    public Mono<ApiResponse<Void>> suspendTask(@PathVariable String id) {
        return migrationService.suspendTask(id)
                .then(Mono.just(ApiResponse.success(null)));
    }

    @PostMapping("/task/{id}/activate")
    public Mono<ApiResponse<Void>> activateTask(@PathVariable String id) {
        return migrationService.activateTask(id)
                .then(Mono.just(ApiResponse.success(null)));
    }
}