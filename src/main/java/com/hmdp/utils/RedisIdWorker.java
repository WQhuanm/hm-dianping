package com.hmdp.utils;


import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

@Component
public class RedisIdWorker {
    private static final long BEGIN_TIMESTAMP = 1704067200L;
    private final StringRedisTemplate redisTemplate;
    private static final int leftShift = 32;

    public RedisIdWorker(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public long nextId(String prefix) {
        long now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - BEGIN_TIMESTAMP;
        long increment = redisTemplate.opsForValue().increment(prefix + now);
        if(increment == 1) {
            redisTemplate.expire(prefix + now, 5, TimeUnit.SECONDS);
        }
        return now << leftShift | increment;
    }

}
