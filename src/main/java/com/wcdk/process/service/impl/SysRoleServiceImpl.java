package com.wcdk.process.service.impl;

import com.wcdk.process.dto.PageResponse;
import com.wcdk.process.dto.SysRoleRequest;
import com.wcdk.process.dto.SysRoleResponse;
import com.wcdk.process.entity.SysPermissionEntity;
import com.wcdk.process.entity.SysRoleEntity;
import com.wcdk.process.entity.SysRolePermissionEntity;
import com.wcdk.process.repository.SysPermissionRepository;
import com.wcdk.process.repository.SysRolePermissionRepository;
import com.wcdk.process.repository.SysRoleRepository;
import com.wcdk.process.repository.SysUserRoleRepository;
import com.wcdk.process.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * 角色管理服务实现。
 *
 * @auther WCDK
 * @date 2026/08/10
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleRepository sysRoleRepository;
    private final SysRolePermissionRepository sysRolePermissionRepository;
    private final SysPermissionRepository sysPermissionRepository;
    private final SysUserRoleRepository sysUserRoleRepository;

    @Override
    public Mono<PageResponse<SysRoleResponse>> list(
            Integer pageNum, Integer pageSize, String roleName, Integer status) {
        int currentPage = pageNum != null && pageNum > 0 ? pageNum : 1;
        int currentSize = pageSize != null && pageSize > 0 ? pageSize : 10;
        return sysRoleRepository.findAll()
                .filter(role -> matches(role, roleName, status))
                .flatMap(this::toResponseWithPermissions)
                .collectList()
                .map(all -> {
                    int total = all.size();
                    int from = Math.min(total, (currentPage - 1) * currentSize);
                    int to = Math.min(total, from + currentSize);
                    return PageResponse.of(all.subList(from, to), (long) total, currentPage, currentSize);
                });
    }

    @Override
    public Mono<SysRoleResponse> getById(Long id) {
        return findRole(id)
                .flatMap(this::toResponseWithPermissions);
    }

    @Override
    public Mono<SysRoleResponse> create(SysRoleRequest request) {
        SysRoleEntity entity = new SysRoleEntity();
        entity.setRoleCode(request.getRoleCode());
        entity.setRoleName(request.getRoleName());
        entity.setSortNo(request.getSortNo() != null ? request.getSortNo() : 0);
        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        entity.setRemark(request.getRemark());
        entity.setCreateTime(Instant.now());
        entity.setUpdateTime(Instant.now());
        return sysRoleRepository.insert(entity)
                .flatMap(role -> saveRolePermissions(role, request.getPermissionIds()));
    }

    @Override
    public Mono<SysRoleResponse> update(Long id, SysRoleRequest request) {
        return findRole(id)
                .flatMap(existing -> {
                    existing.setRoleCode(request.getRoleCode());
                    existing.setRoleName(request.getRoleName());
                    existing.setSortNo(request.getSortNo());
                    existing.setStatus(request.getStatus());
                    existing.setRemark(request.getRemark());
                    existing.setUpdateTime(Instant.now());
                    return sysRoleRepository.updateById(existing)
                            .thenReturn(existing);
                })
                .flatMap(role -> saveRolePermissions(role, request.getPermissionIds()));
    }

    @Override
    public Mono<Void> delete(Long id) {
        return findRole(id)
                .then(sysUserRoleRepository.findByRoleId(id).hasElements())
                .flatMap(inUse -> {
                    if (inUse) {
                        return Mono.error(new IllegalArgumentException("该角色已分配用户，无法删除"));
                    }
                    return deleteRolePermissions(id)
                            .then(sysRoleRepository.deleteById(id));
                })
                .then();
    }

    private Mono<SysRoleEntity> findRole(Long id) {
        return sysRoleRepository.selectById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("角色不存在")));
    }

    private boolean matches(SysRoleEntity role, String roleName, Integer status) {
        if (roleName != null && !roleName.isBlank()
                && (role.getRoleName() == null || !role.getRoleName().contains(roleName))) {
            return false;
        }
        return status == null || status.equals(role.getStatus());
    }

    private Mono<SysRoleResponse> saveRolePermissions(SysRoleEntity role, List<Long> permissionIds) {
        Flux<SysRolePermissionEntity> inserts = permissionIds == null || permissionIds.isEmpty()
                ? Flux.empty()
                : Flux.fromIterable(permissionIds)
                        .distinct()
                        .flatMap(permissionId -> insertRolePermission(role.getId(), permissionId));
        return deleteRolePermissions(role.getId())
                .thenMany(inserts)
                .then(toResponseWithPermissions(role));
    }

    private Mono<Void> deleteRolePermissions(Long roleId) {
        return sysRolePermissionRepository.findByRoleId(roleId)
                .flatMap(existing -> sysRolePermissionRepository.deleteByRoleIdAndPermissionId(existing.getRoleId(), existing.getPermissionId()))
                .then();
    }

    private Mono<SysRolePermissionEntity> insertRolePermission(Long roleId, Long permissionId) {
        SysRolePermissionEntity entity = new SysRolePermissionEntity();
        entity.setRoleId(roleId);
        entity.setPermissionId(permissionId);
        entity.setCreateTime(Instant.now());
        return sysRolePermissionRepository.insert(entity);
    }

    private Mono<SysRoleResponse> toResponseWithPermissions(SysRoleEntity role) {
        return sysRolePermissionRepository.findByRoleId(role.getId())
                .map(SysRolePermissionEntity::getPermissionId)
                .collectList()
                .flatMap(permissionIds -> {
                    if (permissionIds.isEmpty()) {
                        return Mono.just(buildRoleResponse(role, Collections.emptyList(), Collections.emptyList()));
                    }
                    return Flux.fromIterable(permissionIds)
                            .flatMap(sysPermissionRepository::selectById)
                            .collectList()
                            .map(permissions -> buildRoleResponse(
                                    role,
                                    permissionIds,
                                    permissions.stream().map(SysPermissionEntity::getPermissionName).toList()));
                });
    }

    private SysRoleResponse buildRoleResponse(
            SysRoleEntity role, List<Long> permissionIds, List<String> permissionNames) {
        return SysRoleResponse.builder()
                .id(role.getId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .sortNo(role.getSortNo())
                .status(role.getStatus())
                .remark(role.getRemark())
                .createTime(role.getCreateTime())
                .updateTime(role.getUpdateTime())
                .permissionIds(permissionIds)
                .permissionNames(permissionNames)
                .build();
    }
}