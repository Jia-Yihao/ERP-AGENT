package com.example.erp.order.service;

import com.example.erp.common.core.BizException;
import com.example.erp.order.entity.OutboxEvent;
import com.example.erp.order.mapper.OutboxMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxService {
    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);
    private final OutboxMapper mapper;

    @Transactional
    public OutboxEvent publish(OutboxEvent event) {
        event.setStatus(0);
        event.setRetryCount(0);
        event.setNextRetryAt(LocalDateTime.now());
        mapper.insert(event);
        return event;
    }

    public List<OutboxEvent> findPending(LocalDateTime now, int limit) {
        return mapper.findPending(now == null ? LocalDateTime.now() : now, Math.max(1, limit));
    }

    @Transactional
    public OutboxEvent retryFailed(Long id) {
        OutboxEvent event = mapper.selectById(id);
        if (event == null) throw new BizException(404, "Outbox 事件不存在");
        event.setStatus(0);
        event.setNextRetryAt(LocalDateTime.now());
        mapper.updateById(event);
        return event;
    }

    @Scheduled(fixedDelay = 30000)
    public void scanAndSend() {
        for (OutboxEvent event : findPending(LocalDateTime.now(), 50)) {
            try {
                log.info("outbox send simulated: type={}, aggregate={}:{}",
                        event.getEventType(), event.getAggregateType(), event.getAggregateId());
                event.setStatus(1);
                event.setNextRetryAt(null);
            } catch (Exception e) {
                int retries = (event.getRetryCount() == null ? 0 : event.getRetryCount()) + 1;
                event.setStatus(2);
                event.setRetryCount(retries);
                event.setNextRetryAt(LocalDateTime.now().plusSeconds(30L * retries));
                log.warn("outbox send failed", e);
            }
            mapper.updateById(event);
        }
    }
}
