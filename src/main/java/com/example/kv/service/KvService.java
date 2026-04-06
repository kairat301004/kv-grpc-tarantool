package com.example.kv.service;

import com.example.kv.repository.TarantoolRepository;

import java.util.List;

public class KvService {
    
    private final TarantoolRepository repository;
    
    public KvService(TarantoolRepository repository) {
        this.repository = repository;
    }
    
    public void put(String key, byte[] value) {
        repository.put(key, value);
    }
    
    public byte[] get(String key) {
        return repository.get(key);
    }
    
    public void delete(String key) {
        repository.delete(key);
    }
    
    public List<TarantoolRepository.KvPair> range(String from, String to) {
        return repository.range(from, to);
    }
    
    public long count() {
        return repository.count();
    }
    
    public void close() {
        repository.close();
    }
}