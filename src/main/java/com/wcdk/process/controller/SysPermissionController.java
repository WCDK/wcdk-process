package com.wcdk.process.controller;

import com.wcdk.process.dto.ApiResponse;
import com.wcdk.process.dto.PageResponse;
import com.wcdk.process.dto.SysPermissionRequest;
import com.wcdk.process.dto.SysPermissionResponse;
import com.wcdk.process.entity.SysPermissionEntity;
import com.wcdk.process.repository.SysPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/sys/permission", "/sys/permission"})
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SysPermissionController {

    private final SysPermissionRepository sysPermissionRepository;

    @GetMapping("/list")
    public Mono<ApiResponse<PageResponse<SysPermissionResponse>>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String permissionName,
            @RequestParam(required = false) String permissionType,
            @RequestParam(required = false) Integer status) {
        return sysPermissionRepository.findAll()
                .filter(p -> {
                    if (permissionName != null && !permissionName.isBlank() && p.getPermissionName() != null && !p.getPermissionName().contains(permissionName)) return false;
                    if (permissionType != null && !permissionType.isBlank() && !permissionType.equals(p.getPermissionType())) return false;
                    if (status != null && !status.equals(p.getStatus())) return false;
                    return true;
                })
                .collectList()
                .map(all -> {
                    List<SysPermissionResponse> responses = all.stream().map(this::toResponse).collect(Collectors.toList());
                    int total = responses.size();
                    int from = Math.max(0, (pageNum - 1) * pageSize);
                    int to = Math.min(responses.size(), from + pageSize);
                    List<SysPermissionResponse> page = responses.subList(from, to);
                    return ApiResponse.success(PageResponse.of(page, (long) total, pageNum, pageSize));
                });
    }

    @GetMapping("/{id}")
    public Mono<ApiResponse<SysPermissionResponse>> getById(@PathVariable Long id) {
        return sysPermissionRepository.selectById(id)
                .map(p -> ApiResponse.success(toResponse(p)))
                .defaultIfEmpty(ApiResponse.error(404, "权限不存在"));
    }

    @PostMapping
    public Mono<ApiResponse<SysPermissionResponse>> create(@RequestBody SysPermissionRequest request) {
        SysPermissionEntity entity = new SysPermissionEntity();
        entity.setParentId(request.getParentId());
        entity.setPermissionCode(request.getPermissionCode());
        entity.setPermissionName(request.getPermissionName());
        entity.setPermissionType(request.getPermissionType());
        entity.setRoutePath(request.getRoutePath());
        entity.setSortNo(request.getSortNo() != null ? request.getSortNo() : 0);
        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        entity.setRemark(request.getRemark());
        entity.setIcon(request.getIcon());
        entity.setCreateTime(Instant.now());
        entity.setUpdateTime(Instant.now());
        return sysPermissionRepository.insert(entity)
                .map(p -> ApiResponse.success("创建成功", toResponse(p)));
    }

    @PutMapping("/{id}")
    public Mono<ApiResponse<SysPermissionResponse>> update(@PathVariable Long id, @RequestBody SysPermissionRequest request) {
        return sysPermissionRepository.selectById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("权限不存在")))
                .flatMap(existing -> {
                    existing.setParentId(request.getParentId());
                    existing.setPermissionCode(request.getPermissionCode());
                    existing.setPermissionName(request.getPermissionName());
                    existing.setPermissionType(request.getPermissionType());
                    existing.setRoutePath(request.getRoutePath());
                    existing.setSortNo(request.getSortNo());
                    existing.setStatus(request.getStatus());
                    existing.setRemark(request.getRemark());
                    existing.setIcon(request.getIcon());
                    existing.setUpdateTime(Instant.now());
                    return sysPermissionRepository.updateById(existing).flatMap(p -> Mono.just(existing));
                })
                .map(p -> ApiResponse.success("更新成功", toResponse(p)));
    }

    @DeleteMapping("/{id}")
    public Mono<ApiResponse<Void>> delete(@PathVariable Long id) {
        return sysPermissionRepository.deleteById(id)
                .then(Mono.just(ApiResponse.success("删除成功", null)));
    }

    private SysPermissionResponse toResponse(SysPermissionEntity entity) {
        return SysPermissionResponse.builder()
                .id(entity.getId())
                .parentId(entity.getParentId())
                .permissionCode(entity.getPermissionCode())
                .permissionName(entity.getPermissionName())
                .permissionType(entity.getPermissionType())
                .routePath(entity.getRoutePath())
                .sortNo(entity.getSortNo())
                .status(entity.getStatus())
                .remark(entity.getRemark())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .icon(entity.getIcon())
                .build();
    }
}