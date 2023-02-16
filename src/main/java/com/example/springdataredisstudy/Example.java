package com.example.springdataredisstudy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
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
    private RedisTemplate redisTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void example() {
        redisTemplate.opsForList().rightPush("key", "value");
        RedisOperations<String, String> operations = redisTemplate.opsForList().getOperations();
        System.out.println(operations.opsForList().range("key", 0, -1));

        SetOperations<String, Object> setOperations = redisTemplate.opsForSet();
        setOperations.add("setkey", "text2");
        setOperations.add("setkey", "text");
        System.out.println(setOperations.pop("setkey"));
        System.out.println(setOperations.members("setkey"));

        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        Map<String, Object> map = new HashMap<>();
        map.put("firstName", "name");
        map.put("lastName", "lastName");
        map.put("gender", "Man");
        hashOperations.putAll("hashKey", map);

        String firstName = (String) redisTemplate.opsForHash().get("hashKey", "firstName");
        String lastName = (String) redisTemplate.opsForHash().get("hashKey", "lastName");
        String gender = (String) redisTemplate.opsForHash().get("hashKey", "gender");

        System.out.println(redisTemplate.opsForHash().keys("hashKey"));
        System.out.println(firstName);
        System.out.println(lastName);
        System.out.println(gender);
    }

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

//    public void transaction() {
//        List<Object> txResults = redisTemplate.execute(new SessionCallback<List<Object>>() {
//            @Override
//            public List<Object> execute(RedisOperations operations) throws DataAccessException {
//                operations.multi();
//                operations.opsForSet().add("Key", "value1");
//
//                return operations.exec();
//            }
//        });
//        System.out.println("Number of items added to set: " + txResults.get(0));
//    }
}
