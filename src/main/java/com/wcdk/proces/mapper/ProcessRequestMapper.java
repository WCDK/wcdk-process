package com.wcdk.proces.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wcdk.proces.entity.ProcessRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
@Mapper
public interface ProcessRequestMapper extends BaseMapper<ProcessRequest> {

    ProcessRequest selectByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    List<ProcessRequest> selectOrderByCreateTimeDesc();
}
