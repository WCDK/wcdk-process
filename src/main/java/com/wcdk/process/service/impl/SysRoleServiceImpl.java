package com.wcdk.process.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wcdk.process.common.PageResponse;
import com.wcdk.process.dto.SysPermissionResponse;
import com.wcdk.process.dto.SysRoleResponse;
import com.wcdk.process.dto.SysRoleSaveRequest;
import com.wcdk.process.entity.SysRole;
import com.wcdk.process.entity.SysRolePermission;
import com.wcdk.process.entity.SysUserRole;
import com.wcdk.process.mapper.SysRoleMapper;
import com.wcdk.process.mapper.SysRolePermissionMapper;
import com.wcdk.process.mapper.SysUserRoleMapper;
import com.wcdk.process.service.SysPermissionService;
import com.wcdk.process.service.SysRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysPermissionService sysPermissionService;

    public SysRoleServiceImpl(SysRolePermissionMapper sysRolePermissionMapper,
                              SysUserRoleMapper sysUserRoleMapper,
                              SysPermissionService sysPermissionService) {
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysPermissionService = sysPermissionService;
    }

    @Override
    public PageResponse<SysRoleResponse> listRole(long pageNum, long pageSize, String roleName, Integer status) {
        Page<SysRole> page = lambdaQuery()
                .like(StringUtils.hasText(roleName), SysRole::getRoleName, roleName == null ? null : roleName.trim())
                .eq(status != null, SysRole::getStatus, status)
                .orderByAsc(SysRole::getSortNo)
                .orderByDesc(SysRole::getCreateTime)
                .page(new Page<>(Math.max(pageNum, 1L), Math.max(pageSize, 1L)));
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getRecords().stream().map(this::toResponse).toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRoleResponse createRole(SysRoleSaveRequest request) {
        validateRequest(request);
        checkDuplicateCode(null, request.getRoleCode());
        LocalDateTime now = LocalDateTime.now();
        SysRole entity = SysRole.builder()
                .roleCode(request.getRoleCode().trim())
                .roleName(request.getRoleName().trim())
                .sortNo(request.getSortNo() == null ? 0 : request.getSortNo())
                .status(request.getStatus() == null ? 1 : request.getStatus())
                .remark(trimValue(request.getRemark()))
                .createTime(now)
                .updateTime(now)
                .build();
        save(entity);
        replaceRolePermissions(entity.getId(), request.getPermissionIds());
        return toResponse(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRoleResponse updateRole(Long id, SysRoleSaveRequest request) {
        validateRequest(request);
        checkDuplicateCode(id, request.getRoleCode());
        SysRole entity = getRequiredRole(id);
        entity.setRoleCode(request.getRoleCode().trim());
        entity.setRoleName(request.getRoleName().trim());
        entity.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        entity.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        entity.setRemark(trimValue(request.getRemark()));
        entity.setUpdateTime(LocalDateTime.now());
        updateById(entity);
        replaceRolePermissions(id, request.getPermissionIds());
        return toResponse(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        getRequiredRole(id);
        sysUserRoleMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, id));
        sysRolePermissionMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, id));
        removeById(id);
    }

    @Override
    public List<Long> listRoleIdsByUserId(Long userId) {
        return sysUserRoleMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
    }

    @Override
    public List<SysRoleResponse> listByIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return super.listByIds(roleIds).stream().map(this::toResponse).toList();
    }

    @Override
    public List<String> listRoleNamesByUserId(Long userId) {
        return listByIds(listRoleIdsByUserId(userId)).stream()
                .map(SysRoleResponse::getRoleName)
                .toList();
    }

    private void validateRequest(SysRoleSaveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("��ɫ��������Ϊ��");
        }
        if (!StringUtils.hasText(request.getRoleCode())) {
            throw new IllegalArgumentException("��ɫ���벻��Ϊ��");
        }
        if (!StringUtils.hasText(request.getRoleName())) {
            throw new IllegalArgumentException("��ɫ���Ʋ���Ϊ��");
        }
    }

    private void checkDuplicateCode(Long id, String roleCode) {
        long count = lambdaQuery()
                .eq(SysRole::getRoleCode, roleCode.trim())
                .ne(id != null, SysRole::getId, id)
                .count();
        if (count > 0) {
            throw new IllegalArgumentException("��ɫ�����Ѵ���");
        }
    }

    private SysRole getRequiredRole(Long id) {
        SysRole role = getById(id);
        if (role == null) {
            throw new IllegalArgumentException("δ�ҵ���Ӧ��ɫ");
        }
        return role;
    }

    private void replaceRolePermissions(Long roleId, List<Long> permissionIds) {
        sysRolePermissionMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId));
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        permissionIds.stream().distinct().forEach(permissionId -> sysRolePermissionMapper.insert(SysRolePermission.builder()
                .roleId(roleId)
                .permissionId(permissionId)
                .createTime(now)
                .build()));
    }

    private SysRoleResponse toResponse(SysRole entity) {
        List<Long> permissionIds = sysPermissionService.listPermissionIdsByRoleId(entity.getId());
        List<String> permissionNames = sysPermissionService.listByIds(permissionIds).stream()
                .map(SysPermissionResponse::getPermissionName)
                .toList();
        return SysRoleResponse.builder()
                .id(entity.getId())
                .roleCode(entity.getRoleCode())
                .roleName(entity.getRoleName())
                .sortNo(entity.getSortNo())
                .status(entity.getStatus())
                .remark(entity.getRemark())
                .permissionIds(permissionIds)
                .permissionNames(permissionNames)
                .createTime(entity.getCreateTime())
                .build();
    }

    private String trimValue(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
