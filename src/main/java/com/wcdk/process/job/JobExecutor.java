package com.wcdk.process.job;

import com.wcdk.process.entity.JobEntity;
import com.wcdk.process.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobExecutor {
    private static final String READY="READY", RUNNING="RUNNING", COMPLETED="COMPLETED", DEAD="DEAD";
    private final JobRepository repository;
    private final Map<String, JobHandler> handlers = new ConcurrentHashMap<>();
    private final String owner = UUID.randomUUID().toString();
    public void register(String type, JobHandler handler) { handlers.put(type, handler); }
    @Scheduled(fixedDelayString="${wcdk.process.job.poll-delay-ms:5000}")
    public void poll() { pollAndProcess().subscribe(null, e -> log.error("Job polling failed", e)); }
    public Mono<Integer> pollAndProcess() { return repository.findByStatusAndDueAtLessThanEqual(READY, Instant.now()).take(50).concatMap(this::run).count().map(Long::intValue); }
    private Mono<Void> run(JobEntity job) {
        job.setStatus(RUNNING); job.setLockOwner(owner); job.setLockToken(UUID.randomUUID().toString()); job.setLockUntil(Instant.now().plusSeconds(60));
        return repository.updateById(job).then(Mono.defer(() -> { JobHandler h=handlers.get(job.getJobType()); return h==null ? Mono.error(new IllegalStateException("No handler for job type: "+job.getJobType())) : h.execute(job); })).then(mark(job)).onErrorResume(e -> fail(job,e));
    }
    private Mono<Void> mark(JobEntity j) { j.setStatus(COMPLETED); j.setLockUntil(null); return repository.updateById(j).then(); }
    private Mono<Void> fail(JobEntity j, Throwable e) { int n=j.getRetryCount()==null?0:j.getRetryCount()+1, max=j.getMaxRetries()==null?3:j.getMaxRetries(); j.setRetryCount(n); j.setLastError(e.getMessage()); j.setLockUntil(null); j.setStatus(n>=max?DEAD:READY); j.setDueAt(Instant.now().plus(Duration.ofSeconds(Math.min(300,1L<<Math.min(n,8))))); return repository.updateById(j).then(); }
}