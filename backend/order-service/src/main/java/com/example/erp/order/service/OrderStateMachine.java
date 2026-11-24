package com.example.erp.order.service;

import com.example.erp.common.core.BizException;
import com.example.erp.common.core.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class OrderStateMachine {
    public static final int PENDING = 0;
    public static final int APPROVED = 1;
    public static final int RECEIVED = 2;
    public static final int COMPLETED = 3;
    public static final int CANCELLED = 4;

    private static final Map<Integer, Set<Integer>> ALLOWED = Map.of(
            PENDING, Set.of(APPROVED, CANCELLED),
            APPROVED, Set.of(RECEIVED, CANCELLED),
            RECEIVED, Set.of(COMPLETED, CANCELLED),
            COMPLETED, Set.of(),
            CANCELLED, Set.of()
    );

    public boolean canTransition(Integer from, Integer to) {
        if (from == null || to == null) return false;
        Set<Integer> targets = ALLOWED.get(from);
        return targets != null && targets.contains(to);
    }

    public void assertTransition(Integer from, Integer to) {
        if (!canTransition(from, to)) throw new BizException(ErrorCode.ORDER_STATUS_ILLEGAL);
    }
}
