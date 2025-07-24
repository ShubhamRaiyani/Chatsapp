package com.shubham.chatsapp.serviceTests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class RedisTestRunner  {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void testRedisConnection() {
        redisTemplate.opsForValue().set("test-key", "RedisTestValue");
        String value = redisTemplate.opsForValue().get("test-key");
        assertThat(value).isEqualTo("RedisTestValue");
        System.out.println("Well done");
    }
}
