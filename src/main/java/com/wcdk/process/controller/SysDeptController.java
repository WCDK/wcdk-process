package com.wcdk.process.controller;

import com.wcdk.process.dto.ApiResponse;
import com.wcdk.process.dto.PageResponse;
import com.wcdk.process.dto.SysDeptRequest;
import com.wcdk.process.dto.SysDeptResponse;
import com.wcdk.process.entity.SysDeptEntity;
import com.wcdk.process.repository.SysDeptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/sys/dept", "/sys/dept"})
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SysDeptController {

    private final SysDeptRepository sysDeptRepository;

    @GetMapping("/list")
    public Mono<ApiResponse<PageResponse<SysDeptResponse>>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String deptName,
            @RequestParam(required = false) Integer status) {
        return sysDeptRepository.findAll()
                .filter(dept -> {
                    if (deptName != null && !deptName.isBlank() && dept.getDeptName() != null && !dept.getDeptName().contains(deptName)) return false;
                    if (status != null && !status.equals(dept.getStatus())) return false;
                    return true;
                })
                .collectList()
                .flatMap(all -> {
                    List<SysDeptResponse> responses = all.stream().map(this::toResponse).collect(Collectors.toList());
                    Map<Long, String> nameMap = responses.stream()
                            .collect(Collectors.toMap(SysDeptResponse::getId, SysDeptResponse::getDeptName, (a, b) -> a));
                    responses.forEach(r -> r.setParentDeptName(r.getParentId() != null ? nameMap.get(r.getParentId()) : null));
                    int total = responses.size();
                    int from = Math.max(0, (pageNum - 1) * pageSize);
                    int to = Math.min(responses.size(), from + pageSize);
                    List<SysDeptResponse> page = responses.subList(from, to);
                    return Mono.just(ApiResponse.success(PageResponse.of(page, (long) total, pageNum, pageSize)));
                });
    }

    @GetMapping("/{id}")
    public Mono<ApiResponse<SysDeptResponse>> getById(@PathVariable Long id) {
        return sysDeptRepository.selectById(id)
                .map(d -> ApiResponse.success(toResponse(d)))
                .defaultIfEmpty(ApiResponse.error(404, "部门不存在"));
    }

    @PostMapping
    public Mono<ApiResponse<SysDeptResponse>> create(@RequestBody SysDeptRequest request) {
        SysDeptEntity entity = new SysDeptEntity();
        entity.setParentId(request.getParentId());
        entity.setDeptCode(request.getDeptCode());
        entity.setDeptName(request.getDeptName());
        entity.setSortNo(request.getSortNo() != null ? request.getSortNo() : 0);
        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        entity.setRemark(request.getRemark());
        entity.setCreateTime(Instant.now());
        entity.setUpdateTime(Instant.now());
        return sysDeptRepository.insert(entity)
                .map(d -> ApiResponse.success("创建成功", toResponse(d)));
    }

    @PutMapping("/{id}")
    public Mono<ApiResponse<SysDeptResponse>> update(@PathVariable Long id, @RequestBody SysDeptRequest request) {
        return sysDeptRepository.selectById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("部门不存在")))
                .flatMap(existing -> {
                    existing.setParentId(request.getParentId());
                    existing.setDeptCode(request.getDeptCode());
                    existing.setDeptName(request.getDeptName());
                    existing.setSortNo(request.getSortNo());
                    existing.setStatus(request.getStatus());
                    existing.setRemark(request.getRemark());
                    existing.setUpdateTime(Instant.now());
                    return sysDeptRepository.updateById(existing)
                            .thenReturn(existing);
                })
                .map(d -> ApiResponse.success("更新成功", toResponse(d)));
    }

    @DeleteMapping("/{id}")
    public Mono<ApiResponse<Void>> delete(@PathVariable Long id) {
        return sysDeptRepository.deleteById(id)
                .then(Mono.just(ApiResponse.success("删除成功", null)));
    }

    private SysDeptResponse toResponse(SysDeptEntity entity) {
        return SysDeptResponse.builder()
                .id(entity.getId())
                .parentId(entity.getParentId())
                .deptCode(entity.getDeptCode())
                .deptName(entity.getDeptName())
                .sortNo(entity.getSortNo())
                .status(entity.getStatus())
                .remark(entity.getRemark())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}