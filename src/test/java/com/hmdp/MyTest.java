package com.hmdp;

import cn.hutool.core.lang.UUID;
import com.hmdp.service.IShopService;
import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;

@SpringBootTest
public class MyTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    RedisIdWorker idWorker;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private IShopService shopService;

    @Test
    void test() {
        try (FileWriter fileWriter = new FileWriter("D:\\tmp.json")) {
            for (Integer i = 0; i < 2000; ++i) {
                String now=UUID.randomUUID().toString(true);
                String Key = LOGIN_USER_KEY +now ;
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("nickName", "111");
                userMap.put("id", i.toString());
                fileWriter.write("authorization: " + now+"\n");
                redisTemplate.opsForHash().putAll(Key, userMap);
            }
        } catch (IOException e) {
            System.err.println("写入文件时出错：" + e.getMessage());
        }


    }

}
