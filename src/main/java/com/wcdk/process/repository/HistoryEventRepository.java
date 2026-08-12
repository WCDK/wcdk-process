package com.wcdk.process.repository;

import com.wcdk.process.entity.HistoryEventEntity;
import com.wcdk.r2dbc.BaseRepository;
import com.wcdk.r2dbc.Repository;
import reactor.core.publisher.Flux;

@Repository
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface HistoryEventRepository extends BaseRepository<HistoryEventEntity> {

    Flux<HistoryEventEntity> findByProcessInstanceId(String processInstanceId);

    Flux<HistoryEventEntity> findByExecutionId(String executionId);

    Flux<HistoryEventEntity> findByTaskId(String taskId);

    Flux<HistoryEventEntity> findByEventType(String eventType);
}