package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RedisLock {
    private StringRedisTemplate redisTemplate;
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public RedisLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        uuid = UUID.randomUUID().toString();
    }

    public boolean lock(String key, Long time) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, uuid, time, TimeUnit.MINUTES);
        return Boolean.TRUE.equals(success);
    }

    public void unlock(String key) {
        if (uuid.equals(redisTemplate.opsForValue().get(key)))
            redisTemplate.delete(key);
    }
}
