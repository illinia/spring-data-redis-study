package com.example.springdataredisstudy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.*;

import javax.annotation.Resource;
import java.net.URL;
import java.util.List;

public class Example {
//    @Autowired
//    private RedisTemplate<String, String> template;
//
//    @Resource(name = "redisTemplate")
//    private ListOperations<String, String> listOps;
//
//    public void addLink(String userId, URL url) {
//        listOps.leftPush(userId, url.toExternalForm());
//    }

//    String template
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void addLink(String userId, URL url) {
        redisTemplate.opsForList().leftPush(userId, url.toExternalForm());
    }

    public void useCallback() {
        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                Long size = connection.dbSize();
                ((StringRedisConnection) connection).set("key", "value");
                return null;
            }
        });
    }

    public void transaction() {
        List<Object> txResults = redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForSet().add("Key", "value1");

                return operations.exec();
            }
        });
        System.out.println("Number of items added to set: " + txResults.get(0));
    }
}
