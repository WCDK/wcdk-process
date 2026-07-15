package com.wcdk.proces.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wcdk.proces.common.PageResponse;
import com.wcdk.proces.dto.SysDeptResponse;
import com.wcdk.proces.dto.SysDeptSaveRequest;
import com.wcdk.proces.entity.SysDept;
import com.wcdk.proces.mapper.SysDeptMapper;
import com.wcdk.proces.service.SysDeptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    public PageResponse<SysDeptResponse> listDept(long pageNum, long pageSize, String deptName, Integer status) {
        Page<SysDept> page = lambdaQuery()
                .like(StringUtils.hasText(deptName), SysDept::getDeptName, deptName == null ? null : deptName.trim())
                .eq(status != null, SysDept::getStatus, status)
                .orderByAsc(SysDept::getSortNo)
                .orderByDesc(SysDept::getCreateTime)
                .page(new Page<>(Math.max(pageNum, 1L), Math.max(pageSize, 1L)));
        Map<Long, String> deptNameMap = buildDeptNameMap();
        List<SysDeptResponse> records = page.getRecords().stream()
                .map(item -> toResponse(item, deptNameMap))
                .toList();
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysDeptResponse createDept(SysDeptSaveRequest request) {
        validateRequest(request);
        checkDuplicateCode(null, request.getDeptCode());
        LocalDateTime now = LocalDateTime.now();
        SysDept entity = SysDept.builder()
                .parentId(request.getParentId())
                .deptCode(request.getDeptCode().trim())
                .deptName(request.getDeptName().trim())
                .sortNo(defaultSortNo(request.getSortNo()))
                .status(defaultStatus(request.getStatus()))
                .remark(trimValue(request.getRemark()))
                .createTime(now)
                .updateTime(now)
                .build();
        save(entity);
        return toResponse(entity, buildDeptNameMap());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysDeptResponse updateDept(Long id, SysDeptSaveRequest request) {
        validateRequest(request);
        SysDept entity = getRequiredDept(id);
        checkDuplicateCode(id, request.getDeptCode());
        entity.setParentId(request.getParentId());
        entity.setDeptCode(request.getDeptCode().trim());
        entity.setDeptName(request.getDeptName().trim());
        entity.setSortNo(defaultSortNo(request.getSortNo()));
        entity.setStatus(defaultStatus(request.getStatus()));
        entity.setRemark(trimValue(request.getRemark()));
        entity.setUpdateTime(LocalDateTime.now());
        updateById(entity);
        return toResponse(entity, buildDeptNameMap());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDept(Long id) {
        SysDept entity = getRequiredDept(id);
        long childrenCount = lambdaQuery().eq(SysDept::getParentId, id).count();
        if (childrenCount > 0) {
            throw new IllegalArgumentException("该部门下存在子部门，无法删除");
        }
        removeById(entity.getId());
    }

    private void validateRequest(SysDeptSaveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("部门参数不能为空");
        }
        if (!StringUtils.hasText(request.getDeptCode())) {
            throw new IllegalArgumentException("部门编码不能为空");
        }
        if (!StringUtils.hasText(request.getDeptName())) {
            throw new IllegalArgumentException("部门名称不能为空");
        }
    }

    private void checkDuplicateCode(Long id, String deptCode) {
        long count = lambdaQuery()
                .eq(SysDept::getDeptCode, deptCode.trim())
                .ne(id != null, SysDept::getId, id)
                .count();
        if (count > 0) {
            throw new IllegalArgumentException("部门编码已存在");
        }
    }

    private SysDept getRequiredDept(Long id) {
        SysDept entity = getById(id);
        if (entity == null) {
            throw new IllegalArgumentException("未找到对应部门");
        }
        return entity;
    }

    private Map<Long, String> buildDeptNameMap() {
        Map<Long, String> map = new HashMap<>();
        list().forEach(item -> map.put(item.getId(), item.getDeptName()));
        return map;
    }

    private SysDeptResponse toResponse(SysDept entity, Map<Long, String> deptNameMap) {
        return SysDeptResponse.builder()
                .id(entity.getId())
                .parentId(entity.getParentId())
                .parentDeptName(entity.getParentId() == null ? null : deptNameMap.get(entity.getParentId()))
                .deptCode(entity.getDeptCode())
                .deptName(entity.getDeptName())
                .sortNo(entity.getSortNo())
                .status(entity.getStatus())
                .remark(entity.getRemark())
                .createTime(entity.getCreateTime())
                .build();
    }

    private Integer defaultStatus(Integer status) {
        return status == null ? 1 : status;
    }

    private Integer defaultSortNo(Integer sortNo) {
        return sortNo == null ? 0 : sortNo;
    }

    private String trimValue(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
