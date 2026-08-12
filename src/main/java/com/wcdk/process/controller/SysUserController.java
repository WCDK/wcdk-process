package com.wcdk.process.controller;

import com.wcdk.process.dto.ApiResponse;
import com.wcdk.process.dto.PageResponse;
import com.wcdk.process.dto.SysUserRequest;
import com.wcdk.process.dto.SysUserResponse;
import com.wcdk.process.entity.SysDeptEntity;
import com.wcdk.process.entity.SysRoleEntity;
import com.wcdk.process.entity.SysUserEntity;
import com.wcdk.process.entity.SysUserRoleEntity;
import com.wcdk.process.repository.SysDeptRepository;
import com.wcdk.process.repository.SysRoleRepository;
import com.wcdk.process.repository.SysUserRepository;
import com.wcdk.process.repository.SysUserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/sys/user", "/sys/user"})
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SysUserController {

    private final SysUserRepository sysUserRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysDeptRepository sysDeptRepository;

    @GetMapping("/list")
    public Mono<ApiResponse<PageResponse<SysUserResponse>>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String deptId,
            @RequestParam(required = false) Integer status) {
        return sysUserRepository.findAll()
                .filter(user -> {
                    if (username != null && !username.isBlank() && user.getUsername() != null && !user.getUsername().contains(username)) return false;
                    if (realName != null && !realName.isBlank() && user.getRealName() != null && !user.getRealName().contains(realName)) return false;
                    if (deptId != null && !deptId.isBlank() && user.getDeptId() != null && !String.valueOf(user.getDeptId()).equals(deptId)) return false;
                    if (status != null && !status.equals(user.getStatus())) return false;
                    return true;
                })
                .flatMap(this::toResponseWithRoles)
                .collectList()
                .map(all -> {
                    int total = all.size();
                    int from = Math.max(0, (pageNum - 1) * pageSize);
                    int to = Math.min(all.size(), from + pageSize);
                    List<SysUserResponse> page = all.subList(from, to);
                    return ApiResponse.success(PageResponse.of(page, (long) total, pageNum, pageSize));
                });
    }

    @GetMapping("/{id}")
    public Mono<ApiResponse<SysUserResponse>> getById(@PathVariable Long id) {
        return sysUserRepository.selectById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("用户不存在")))
                .flatMap(this::toResponseWithRoles)
                .map(ApiResponse::success);
    }

    @PostMapping
    public Mono<ApiResponse<SysUserResponse>> create(@RequestBody SysUserRequest request) {
        SysUserEntity entity = new SysUserEntity();
        entity.setDeptId(request.getDeptId() != null && !String.valueOf(request.getDeptId()).isBlank() ? request.getDeptId() : null);
        entity.setUsername(request.getUsername());
        entity.setPasswordHash(md5(request.getPassword()));
        entity.setRealName(request.getRealName());
        entity.setMobile(request.getMobile());
        entity.setEmail(request.getEmail());
        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        entity.setCreateTime(Instant.now());
        entity.setUpdateTime(Instant.now());
        return sysUserRepository.insert(entity)
                .flatMap(user -> saveUserRoles(user, request.getRoleIds()))
                .map(user -> ApiResponse.success("创建成功", user));
    }

    @PutMapping("/{id}")
    public Mono<ApiResponse<SysUserResponse>> update(@PathVariable Long id, @RequestBody SysUserRequest request) {
        return sysUserRepository.selectById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("用户不存在")))
                .flatMap(existing -> {
                    existing.setDeptId(request.getDeptId() != null && !String.valueOf(request.getDeptId()).isBlank() ? request.getDeptId() : null);
                    existing.setUsername(request.getUsername());
                    existing.setRealName(request.getRealName());
                    existing.setMobile(request.getMobile());
                    existing.setEmail(request.getEmail());
                    existing.setStatus(request.getStatus());
                    existing.setUpdateTime(Instant.now());
                    if (request.getPassword() != null && !request.getPassword().isBlank()) {
                        existing.setPasswordHash(md5(request.getPassword()));
                    }
                    return sysUserRepository.updateById(existing)
                            .thenReturn(existing);
                })
                .flatMap(user -> saveUserRoles(user, request.getRoleIds()))
                .map(user -> ApiResponse.success("更新成功", user));
    }

    @DeleteMapping("/{id}")
    public Mono<ApiResponse<Void>> delete(@PathVariable Long id) {
        return sysUserRoleRepository.findByUserId(id)
                .flatMap(ur -> sysUserRoleRepository.deleteById(String.valueOf(ur.getUserId()) + "_" + ur.getRoleId()))
                .then(sysUserRepository.deleteById(id))
                .then(Mono.just(ApiResponse.success("删除成功", null)));
    }

    private Mono<SysUserResponse> saveUserRoles(SysUserEntity user, List<Long> roleIds) {
        return sysUserRoleRepository.findByUserId(user.getId())
                .flatMap(existing -> sysUserRoleRepository.deleteById(String.valueOf(existing.getUserId()) + "_" + existing.getRoleId()))
                .thenMany(roleIds != null && !roleIds.isEmpty() ?
                        Flux.fromIterable(roleIds).flatMap((Function<? super Long, ? extends Publisher<? extends SysUserRoleEntity>>) roleId -> {
                            SysUserRoleEntity ur = new SysUserRoleEntity();
                            ur.setUserId(user.getId());
                            ur.setRoleId(roleId);
                            ur.setCreateTime(Instant.now());
                            return sysUserRoleRepository.insert(ur);
                        }) : Flux.empty())
                .then(toResponseWithRoles(user));
    }

    private Mono<SysUserResponse> toResponseWithRoles(SysUserEntity user) {
        Mono<Optional<String>> deptNameMono = user.getDeptId() == null
                ? Mono.just(Optional.empty())
                : sysDeptRepository.selectById(user.getDeptId())
                        .map(SysDeptEntity::getDeptName)
                        .map(Optional::ofNullable)
                        .defaultIfEmpty(Optional.empty());
        return deptNameMono.flatMap(deptName -> sysUserRoleRepository.findByUserId(user.getId())
                .map(SysUserRoleEntity::getRoleId)
                .collectList()
                .flatMap(roleIds -> {
                    if (roleIds.isEmpty()) {
                        return Mono.just(buildUserResponse(user, deptName.orElse(null), Collections.emptyList(), Collections.emptyList()));
                    }
                    return Flux.fromIterable(roleIds)
                            .flatMap(sysRoleRepository::selectById)
                            .filter(role -> role != null)
                            .collectList()
                            .map(roles -> {
                                List<String> roleNames = roles.stream().map(SysRoleEntity::getRoleName).collect(Collectors.toList());
                                return buildUserResponse(user, deptName.orElse(null), roleIds, roleNames);
                            });
                }));
    }

    private SysUserResponse buildUserResponse(SysUserEntity user, String deptName, List<Long> roleIds, List<String> roleNames) {
        return SysUserResponse.builder()
                .id(user.getId())
                .deptId(user.getDeptId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .mobile(user.getMobile())
                .email(user.getEmail())
                .status(user.getStatus())
                .lastLoginTime(user.getLastLoginTime())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .deptName(deptName)
                .roleIds(roleIds)
                .roleNames(roleNames)
                .build();
    }

    private String md5(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}