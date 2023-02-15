package com.example.springdataredisstudy.hashmapping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.hash.HashMapper;
import org.springframework.data.redis.hash.ObjectHashMapper;

import java.util.Map;

public class HashMapping {
    @Autowired
    HashOperations<String, byte[], byte[]> hashOperations;
    HashMapper<Object, byte[], byte[]> mapper = new ObjectHashMapper();

    public void writeHash(String key, Person person) {
        Map<byte[], byte[]> mappedHash = mapper.toHash(person);
        hashOperations.putAll(key, mappedHash);
    }

    public Person loadHash(String key) {
        Map<byte[], byte[]> loadedHash = hashOperations.entries("key");
        return (Person) mapper.fromHash(loadedHash);
    }
}
