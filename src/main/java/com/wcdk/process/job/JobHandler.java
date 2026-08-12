package com.wcdk.process.job;
import com.wcdk.process.entity.JobEntity;
import reactor.core.publisher.Mono;
@FunctionalInterface
public interface JobHandler {
    Mono<Void> execute(JobEntity job);
}