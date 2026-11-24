/**
 * 缓存与并发基础设施：CacheService 双实现（Redis / Caffeine 本地降级）、
 * LockService 分布式锁（SET NX PX + Lua 释放）、幂等注解与切面。
 */
package com.example.erp.common.cache;
