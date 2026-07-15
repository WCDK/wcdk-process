package com.wcdk.proces.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wcdk.proces.common.PageResponse;
import com.wcdk.proces.dto.SysPermissionResponse;
import com.wcdk.proces.dto.SysPermissionSaveRequest;
import com.wcdk.proces.entity.SysPermission;
import com.wcdk.proces.entity.SysRolePermission;
import com.wcdk.proces.entity.SysUserRole;
import com.wcdk.proces.mapper.SysPermissionMapper;
import com.wcdk.proces.mapper.SysRolePermissionMapper;
import com.wcdk.proces.mapper.SysUserRoleMapper;
import com.wcdk.proces.service.SysPermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    public SysPermissionServiceImpl(SysRolePermissionMapper sysRolePermissionMapper,
                                    SysUserRoleMapper sysUserRoleMapper) {
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
    }

    @Override
    public PageResponse<SysPermissionResponse> listPermission(long pageNum,
                                                              long pageSize,
                                                              String permissionName,
                                                              String permissionType,
                                                              Long parentId,
                                                              Integer status) {
        Page<SysPermission> page = lambdaQuery()
                .like(StringUtils.hasText(permissionName), SysPermission::getPermissionName, permissionName == null ? null : permissionName.trim())
                .eq(StringUtils.hasText(permissionType), SysPermission::getPermissionType, permissionType == null ? null : permissionType.trim())
                .eq(parentId != null, SysPermission::getParentId, parentId)
                .eq(status != null, SysPermission::getStatus, status)
                .orderByAsc(SysPermission::getSortNo)
                .orderByDesc(SysPermission::getCreateTime)
                .page(new Page<>(Math.max(pageNum, 1L), Math.max(pageSize, 1L)));
        Map<Long, String> permissionNameMap = list().stream()
                .collect(Collectors.toMap(SysPermission::getId, SysPermission::getPermissionName, (left, right) -> left));
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getRecords().stream().map(item -> toResponse(item, permissionNameMap)).toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysPermissionResponse createPermission(SysPermissionSaveRequest request) {
        validateRequest(request);
        checkDuplicateCode(null, request.getPermissionCode());
        LocalDateTime now = LocalDateTime.now();
        SysPermission entity = SysPermission.builder()
                .parentId(request.getParentId())
                .permissionCode(request.getPermissionCode().trim())
                .permissionName(request.getPermissionName().trim())
                .permissionType(trimValue(request.getPermissionType()))
                .routePath(trimValue(request.getRoutePath()))
                .sortNo(request.getSortNo() == null ? 0 : request.getSortNo())
                .status(request.getStatus() == null ? 1 : request.getStatus())
                .remark(trimValue(request.getRemark()))
                .createTime(now)
                .updateTime(now)
                .build();
        save(entity);
        return toResponse(entity, Map.of());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysPermissionResponse updatePermission(Long id, SysPermissionSaveRequest request) {
        validateRequest(request);
        checkDuplicateCode(id, request.getPermissionCode());
        SysPermission entity = getRequiredPermission(id);
        entity.setParentId(request.getParentId());
        entity.setPermissionCode(request.getPermissionCode().trim());
        entity.setPermissionName(request.getPermissionName().trim());
        entity.setPermissionType(trimValue(request.getPermissionType()));
        entity.setRoutePath(trimValue(request.getRoutePath()));
        entity.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        entity.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        entity.setRemark(trimValue(request.getRemark()));
        entity.setUpdateTime(LocalDateTime.now());
        updateById(entity);
        return toResponse(entity, Map.of());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long id) {
        getRequiredPermission(id);
        long childrenCount = lambdaQuery().eq(SysPermission::getParentId, id).count();
        if (childrenCount > 0) {
            throw new IllegalArgumentException("该权限下存在子权限，无法删除");
        }
        sysRolePermissionMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getPermissionId, id));
        removeById(id);
    }

    @Override
    public List<Long> listPermissionIdsByRoleId(Long roleId) {
        return sysRolePermissionMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getRoleId, roleId))
                .stream()
                .map(SysRolePermission::getPermissionId)
                .toList();
    }

    @Override
    public List<SysPermissionResponse> listByIds(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return List.of();
        }
        Map<Long, String> permissionNameMap = new HashMap<>();
        list().forEach(item -> permissionNameMap.put(item.getId(), item.getPermissionName()));
        return super.listByIds(permissionIds).stream()
                .map(item -> toResponse(item, permissionNameMap))
                .toList();
    }

    @Override
    public Set<String> listPermissionCodesByUserId(Long userId) {
        List<Long> roleIds = sysUserRoleMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return Set.of();
        }
        List<Long> permissionIds = sysRolePermissionMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRolePermission>()
                        .in(SysRolePermission::getRoleId, roleIds))
                .stream()
                .map(SysRolePermission::getPermissionId)
                .distinct()
                .toList();
        if (permissionIds.isEmpty()) {
            return Set.of();
        }
        return super.listByIds(permissionIds).stream()
                .map(SysPermission::getPermissionCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    private void validateRequest(SysPermissionSaveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("权限参数不能为空");
        }
        if (!StringUtils.hasText(request.getPermissionCode())) {
            throw new IllegalArgumentException("权限编码不能为空");
        }
        if (!StringUtils.hasText(request.getPermissionName())) {
            throw new IllegalArgumentException("权限名称不能为空");
        }
    }

    private void checkDuplicateCode(Long id, String permissionCode) {
        long count = lambdaQuery()
                .eq(SysPermission::getPermissionCode, permissionCode.trim())
                .ne(id != null, SysPermission::getId, id)
                .count();
        if (count > 0) {
            throw new IllegalArgumentException("权限编码已存在");
        }
    }

    private SysPermission getRequiredPermission(Long id) {
        SysPermission permission = getById(id);
        if (permission == null) {
            throw new IllegalArgumentException("未找到对应权限");
        }
        return permission;
    }

    private SysPermissionResponse toResponse(SysPermission entity, Map<Long, String> permissionNameMap) {
        return SysPermissionResponse.builder()
                .id(entity.getId())
                .parentId(entity.getParentId())
                .parentPermissionName(entity.getParentId() == null ? null : permissionNameMap.get(entity.getParentId()))
                .permissionCode(entity.getPermissionCode())
                .permissionName(entity.getPermissionName())
                .permissionType(entity.getPermissionType())
                .routePath(entity.getRoutePath())
                .sortNo(entity.getSortNo())
                .status(entity.getStatus())
                .remark(entity.getRemark())
                .createTime(entity.getCreateTime())
                .build();
    }

    private String trimValue(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
