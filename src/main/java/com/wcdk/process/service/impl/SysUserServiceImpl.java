package com.wcdk.process.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wcdk.process.common.PageResponse;
import com.wcdk.process.dto.SysRoleResponse;
import com.wcdk.process.dto.SysUserResponse;
import com.wcdk.process.dto.SysUserSaveRequest;
import com.wcdk.process.entity.SysUser;
import com.wcdk.process.entity.SysUserRole;
import com.wcdk.process.mapper.SysDeptMapper;
import com.wcdk.process.mapper.SysUserMapper;
import com.wcdk.process.mapper.SysUserRoleMapper;
import com.wcdk.process.service.SysRoleService;
import com.wcdk.process.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysDeptMapper sysDeptMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleService sysRoleService;

    public SysUserServiceImpl(SysDeptMapper sysDeptMapper,
                              SysUserRoleMapper sysUserRoleMapper,
                              SysRoleService sysRoleService) {
        this.sysDeptMapper = sysDeptMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleService = sysRoleService;
    }

    @Override
    public PageResponse<SysUserResponse> listUser(long pageNum, long pageSize, String username, String realName, Long deptId, Integer status) {
        Page<SysUser> page = lambdaQuery()
                .like(StringUtils.hasText(username), SysUser::getUsername, username == null ? null : username.trim())
                .like(StringUtils.hasText(realName), SysUser::getRealName, realName == null ? null : realName.trim())
                .eq(deptId != null, SysUser::getDeptId, deptId)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime)
                .page(new Page<>(Math.max(pageNum, 1L), Math.max(pageSize, 1L)));
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getRecords().stream().map(this::toResponse).toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserResponse createUser(SysUserSaveRequest request) {
        validateCreateRequest(request);
        checkDuplicateUsername(null, request.getUsername());
        LocalDateTime now = LocalDateTime.now();
        SysUser entity = SysUser.builder()
                .deptId(request.getDeptId())
                .username(request.getUsername().trim())
                .passwordHash(encryptPassword(request.getPassword().trim()))
                .realName(request.getRealName().trim())
                .mobile(trimValue(request.getMobile()))
                .email(trimValue(request.getEmail()))
                .status(request.getStatus() == null ? 1 : request.getStatus())
                .createTime(now)
                .updateTime(now)
                .build();
        save(entity);
        replaceUserRoles(entity.getId(), request.getRoleIds());
        return toResponse(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserResponse updateUser(Long id, SysUserSaveRequest request) {
        validateUpdateRequest(request);
        checkDuplicateUsername(id, request.getUsername());
        SysUser entity = getRequiredUser(id);
        entity.setDeptId(request.getDeptId());
        entity.setUsername(request.getUsername().trim());
        if (StringUtils.hasText(request.getPassword())) {
            entity.setPasswordHash(encryptPassword(request.getPassword().trim()));
        }
        entity.setRealName(request.getRealName().trim());
        entity.setMobile(trimValue(request.getMobile()));
        entity.setEmail(trimValue(request.getEmail()));
        entity.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        entity.setUpdateTime(LocalDateTime.now());
        updateById(entity);
        replaceUserRoles(id, request.getRoleIds());
        return toResponse(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        getRequiredUser(id);
        sysUserRoleMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));
        removeById(id);
    }

    @Override
    public SysUser getByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return lambdaQuery().eq(SysUser::getUsername, username.trim()).one();
    }

    @Override
    public SysUserResponse getUserResponse(Long id) {
        return toResponse(getRequiredUser(id));
    }

    @Override
    public void updateLastLoginTime(Long id) {
        SysUser entity = getRequiredUser(id);
        entity.setLastLoginTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        updateById(entity);
    }

    private void validateCreateRequest(SysUserSaveRequest request) {
        validateUpdateRequest(request);
        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("新增用户时密码不能为空");
        }
    }

    private void validateUpdateRequest(SysUserSaveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("用户参数不能为空");
        }
        if (!StringUtils.hasText(request.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!StringUtils.hasText(request.getRealName())) {
            throw new IllegalArgumentException("姓名不能为空");
        }
    }

    private void checkDuplicateUsername(Long id, String username) {
        long count = lambdaQuery()
                .eq(SysUser::getUsername, username.trim())
                .ne(id != null, SysUser::getId, id)
                .count();
        if (count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
    }

    private SysUser getRequiredUser(Long id) {
        SysUser entity = getById(id);
        if (entity == null) {
            throw new IllegalArgumentException("未找到对应用户");
        }
        return entity;
    }

    private void replaceUserRoles(Long userId, List<Long> roleIds) {
        sysUserRoleMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        roleIds.stream().distinct().forEach(roleId -> sysUserRoleMapper.insert(SysUserRole.builder()
                .userId(userId)
                .roleId(roleId)
                .createTime(now)
                .build()));
    }

    private SysUserResponse toResponse(SysUser entity) {
        Map<Long, String> deptMap = new HashMap<>();
        sysDeptMapper.selectList(null).forEach(item -> deptMap.put(item.getId(), item.getDeptName()));
        List<Long> roleIds = sysRoleService.listRoleIdsByUserId(entity.getId());
        List<SysRoleResponse> roleResponses = sysRoleService.listByIds(roleIds);
        return SysUserResponse.builder()
                .id(entity.getId())
                .deptId(entity.getDeptId())
                .deptName(entity.getDeptId() == null ? null : deptMap.get(entity.getDeptId()))
                .username(entity.getUsername())
                .realName(entity.getRealName())
                .mobile(entity.getMobile())
                .email(entity.getEmail())
                .status(entity.getStatus())
                .lastLoginTime(entity.getLastLoginTime())
                .createTime(entity.getCreateTime())
                .roleIds(roleIds)
                .roleNames(roleResponses.stream().map(SysRoleResponse::getRoleName).toList())
                .build();
    }

    private String encryptPassword(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
    }

    private String trimValue(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
