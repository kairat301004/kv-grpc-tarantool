package com.example.kv.repository;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolClientFactory;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.tuple.TarantoolTuple;

import java.util.*;
import java.util.logging.Logger;

public class TarantoolRepository {
    
    private static final Logger log = Logger.getLogger(TarantoolRepository.class.getName());
    private final TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client;
    
    public TarantoolRepository() {
        this.client = TarantoolClientFactory.createClient()
                .withAddress("localhost", 3301)
                .build();
        log.info("Connected to Tarantool");
    }
    
    public void put(String key, byte[] value) {
        String lua;
        if (value == null) {
            lua = String.format("box.space.KV:replace{'%s', nil}", key);
        } else {
            String valueStr = Base64.getEncoder().encodeToString(value);
            lua = String.format("box.space.KV:replace{'%s', '%s'}", key, valueStr);
        }
        client.syncOps().eval(lua, Collections.emptyList());
        log.fine("PUT: " + key);
    }
    
    public byte[] get(String key) {
        String lua = String.format("return box.space.KV:get('%s')", key);
        List<?> result = client.syncOps().eval(lua, Collections.emptyList());
        
        if (result == null || result.isEmpty()) {
            return null;
        }
        
        List<?> tuple = (List<?>) result.get(0);
        if (tuple.size() < 2 || tuple.get(1) == null) {
            return null;
        }
        
        return Base64.getDecoder().decode(tuple.get(1).toString());
    }
    
    public void delete(String key) {
        String lua = String.format("box.space.KV:delete('%s')", key);
        client.syncOps().eval(lua, Collections.emptyList());
        log.fine("DELETE: " + key);
    }
    
    public List<KvPair> range(String from, String to) {
        String lua = 
            "local r = {} " +
            "for _, t in box.space.KV:pairs() do " +
            "  if t[1] >= '" + from + "' and t[1] <= '" + to + "' then " +
            "    table.insert(r, {key = t[1], value = t[2]}) " +
            "  end " +
            "end " +
            "return r";
        
        List<?> result = client.syncOps().eval(lua, Collections.emptyList());
        List<KvPair> list = new ArrayList<>();
        
        for (Object obj : result) {
            if (obj instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) obj;
                String key = (String) map.get("key");
                Object val = map.get("value");
                byte[] bytes = val != null ? Base64.getDecoder().decode(val.toString()) : null;
                list.add(new KvPair(key, bytes));
            }
        }
        return list;
    }
    
    public long count() {
        List<?> result = client.syncOps().eval("return box.space.KV:count()", Collections.emptyList());
        if (result != null && !result.isEmpty() && result.get(0) instanceof Number) {
            return ((Number) result.get(0)).longValue();
        }
        return 0;
    }
    
    public void close() {
        if (client != null) {
            client.close();
        }
        log.info("Disconnected from Tarantool");
    }
    
    public static class KvPair {
        private final String key;
        private final byte[] value;
        
        public KvPair(String key, byte[] value) {
            this.key = key;
            this.value = value;
        }
        
        public String getKey() { return key; }
        public byte[] getValue() { return value; }
    }
}