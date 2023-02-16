package com.example.springdataredisstudy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
// 선언적 트랜잭션 관리를 활성화하도록 Spring 컨텍스트를 구성한다.
//@EnableTransactionManagement
public class RedisTxContextConfiguration {

    @Autowired
    public DataSource dataSource;

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate template = new RedisTemplate();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
        // 현재 스레드에 대한 연결을 바인딩하여 트랜잭션에 참여하도록 구성한다.
//        template.setEnableTransactionSupport(true);
        return template;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379));
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        // 트랜잭션 management 는 PlatformTransactionManager 를 필요로한다.
        // spring data redis 는 PlatformTransactionManager 구현체를 가지고있지 않다.
        // jdbc 를 사용한다고 가정하면 기존 트랜잭션 관리자를 사용하여 트랜잭션에 참여할 수 있다.
        return new DataSourceTransactionManager(dataSource);
    }
}
