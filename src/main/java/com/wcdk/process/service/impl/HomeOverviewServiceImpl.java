package com.wcdk.process.service.impl;

import com.wcdk.process.dto.HomeOverviewResponse;
import com.wcdk.process.mapper.ProcessRequestMapper;
import com.wcdk.process.service.HomeOverviewService;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.springframework.stereotype.Service;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@Service
@RequiredArgsConstructor
public class HomeOverviewServiceImpl implements HomeOverviewService {

    private final RepositoryService repositoryService;

    private final TaskService taskService;

    private final ProcessRequestMapper processRequestMapper;

    @Override
    public HomeOverviewResponse getOverview() {
        return HomeOverviewResponse.builder()
                .definitionCount(repositoryService.createProcessDefinitionQuery().count())
                .modelCount(repositoryService.createModelQuery().count())
                .processCount(processRequestMapper.selectCount(null))
                .taskCount(taskService.createTaskQuery().count())
                .build();
    }
}
