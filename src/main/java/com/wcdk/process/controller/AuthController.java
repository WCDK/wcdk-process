package com.wcdk.process.controller;

import com.wcdk.process.dto.ApiResponse;
import com.wcdk.process.entity.SysDeptEntity;
import com.wcdk.process.entity.SysPermissionEntity;
import com.wcdk.process.entity.SysUserEntity;
import com.wcdk.process.repository.SysDeptRepository;
import com.wcdk.process.repository.SysPermissionRepository;
import com.wcdk.process.repository.SysRolePermissionRepository;
import com.wcdk.process.repository.SysUserRepository;
import com.wcdk.process.repository.SysUserRoleRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class AuthController {

    private final SysUserRepository sysUserRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final SysRolePermissionRepository sysRolePermissionRepository;
    private final SysPermissionRepository sysPermissionRepository;
    private final SysDeptRepository sysDeptRepository;

    private final Map<String, UserSession> tokenStore = new ConcurrentHashMap<>();

    @PostMapping("/login")
    public Mono<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            return Mono.just(ApiResponse.error(400, "用户名和密码不能为空"));
        }
        return sysUserRepository.findByUsername(request.getUsername())
                .flatMap(user -> {
                    if (!Objects.equals(user.getPasswordHash(), md5(request.getPassword()))) {
                        return Mono.just(ApiResponse.<Map<String, Object>>error(401, "用户名或密码错误"));
                    }
                    if (user.getStatus() != null && user.getStatus() == 0) {
                        return Mono.just(ApiResponse.<Map<String, Object>>error(403, "用户已被禁用"));
                    }
                    return buildCurrentUser(user)
                            .map(currentUser -> {
                                String token = UUID.randomUUID().toString().replace("-", "");
                                tokenStore.put(token, new UserSession(user.getUsername(), currentUser));
                                Map<String, Object> data = new LinkedHashMap<>();
                                data.put("token", token);
                                data.put("currentUser", currentUser);
                                log.info("User logged in: {}", request.getUsername());
                                return ApiResponse.success("登录成功", data);
                            });
                })
                .switchIfEmpty(Mono.fromSupplier(() ->
                        ApiResponse.<Map<String, Object>>error(401, "用户名或密码错误")));
    }

    @GetMapping("/me")
    public Mono<ApiResponse<Map<String, Object>>> me(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        return Mono.fromCallable(() -> {
            if (token == null || token.isBlank()) {
                return ApiResponse.<Map<String, Object>>error(401, "未登录");
            }
            UserSession session = tokenStore.get(token);
            if (session == null) {
                return ApiResponse.<Map<String, Object>>error(401, "登录已过期");
            }
            return ApiResponse.success(session.currentUser);
        });
    }

    @PostMapping("/logout")
    public Mono<ApiResponse<Void>> logout(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        return Mono.fromCallable(() -> {
            if (token != null && !token.isBlank()) {
                UserSession removed = tokenStore.remove(token);
                if (removed != null) {
                    log.info("User logged out: {}", removed.username);
                }
            }
            return ApiResponse.success("退出成功", null);
        });
    }

    private Mono<Map<String, Object>> buildCurrentUser(SysUserEntity user) {
        Mono<Optional<String>> deptNameMono = user.getDeptId() == null
                ? Mono.just(Optional.empty())
                : sysDeptRepository.selectById(user.getDeptId())
                        .map(SysDeptEntity::getDeptName)
                        .map(Optional::ofNullable)
                        .defaultIfEmpty(Optional.empty());
        return deptNameMono.flatMap(deptName -> sysUserRoleRepository.findByUserId(user.getId())
                .map(ur -> ur.getRoleId())
                .collectList()
                .flatMap(roleIds -> {
                    if (roleIds.isEmpty()) {
                        return Mono.just(buildUserMap(user, deptName.orElse(null), Collections.emptyList(), Collections.emptyList()));
                    }
                    return Flux.fromIterable(roleIds)
                            .flatMap(roleId -> sysRolePermissionRepository.findByRoleId(roleId))
                            .map(rp -> rp.getPermissionId())
                            .distinct()
                            .collectList()
                            .flatMap(permissionIds -> {
                                if (permissionIds.isEmpty()) {
                                    return Mono.just(buildUserMap(user, deptName.orElse(null), roleIds, Collections.emptyList()));
                                }
                                return Flux.fromIterable(permissionIds)
                                        .flatMap(sysPermissionRepository::selectById)
                                        .filter(p -> p != null)
                                        .collectList()
                                        .map(permissions -> buildUserMap(user, deptName.orElse(null), roleIds, permissions));
                            });
                }));
    }

    private Map<String, Object> buildUserMap(SysUserEntity user, String deptName, List<Long> roleIds, List<SysPermissionEntity> permissions) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("deptName", deptName);
        result.put("roleIds", roleIds);

        List<String> permissionCodes = new ArrayList<>();
        permissionCodes.add("*");
        for (SysPermissionEntity p : permissions) {
            permissionCodes.add(p.getPermissionCode());
        }
        result.put("permissionCodes", permissionCodes);

        List<Map<String, Object>> allResources = new ArrayList<>();
        for (SysPermissionEntity p : permissions) {
            Map<String, Object> resource = new LinkedHashMap<>();
            resource.put("id", String.valueOf(p.getId()));
            resource.put("parentId", p.getParentId() != null ? String.valueOf(p.getParentId()) : null);
            resource.put("permissionCode", p.getPermissionCode());
            resource.put("permissionName", p.getPermissionName());
            resource.put("permissionType", p.getPermissionType());
            resource.put("routePath", p.getRoutePath());
            resource.put("icon", p.getIcon());
            resource.put("sortNo", p.getSortNo());
            resource.put("remark", p.getRemark());
            allResources.add(resource);
        }

        List<Map<String, Object>> tree = buildPermissionTree(allResources, null);
        result.put("permissionResources", tree);

        return result;
    }

    private List<Map<String, Object>> buildPermissionTree(List<Map<String, Object>> all, String parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Map<String, Object> item : all) {
            String pid = (String) item.get("parentId");
            if (parentId == null && pid == null) {
                tree.add(item);
            } else if (parentId != null && parentId.equals(pid)) {
                tree.add(item);
            }
        }
        tree.sort((a, b) -> {
            int sa = a.get("sortNo") != null ? ((Number) a.get("sortNo")).intValue() : 0;
            int sb = b.get("sortNo") != null ? ((Number) b.get("sortNo")).intValue() : 0;
            return Integer.compare(sa, sb);
        });
        for (Map<String, Object> node : tree) {
            node.put("children", buildPermissionTree(all, (String) node.get("id")));
        }
        return tree;
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

    @Data
    /**
     * WCDK 流程模块类型。
     *
     * @author WCDK
     * @version 1.0
     */
    static class LoginRequest {
        private String username;
        private String password;
    }

    /**
     * WCDK 流程模块类型。
     *
     * @author WCDK
     * @version 1.0
     */
    static class UserSession {
        final String username;
        final Map<String, Object> currentUser;

        UserSession(String username, Map<String, Object> currentUser) {
            this.username = username;
            this.currentUser = currentUser;
        }
    }
}