package com.example.kv.grpc;

import com.example.kv.service.KvService;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;

import java.util.logging.Logger;

public class KvServiceImpl extends KvServiceGrpc.KvServiceImplBase {
    
    private static final Logger log = Logger.getLogger(KvServiceImpl.class.getName());
    private final KvService kvService;
    
    public KvServiceImpl(KvService kvService) {
        this.kvService = kvService;
    }
    
    @Override
    public void put(PutRequest request, StreamObserver<Empty> responseObserver) {
        try {
            byte[] value = request.getValue().isEmpty() ? null : request.getValue().toByteArray();
            kvService.put(request.getKey(), value);
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
            log.info("PUT: " + request.getKey());
        } catch (Exception e) {
            log.severe("PUT error: " + e.getMessage());
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void get(GetRequest request, StreamObserver<GetResponse> responseObserver) {
        try {
            byte[] value = kvService.get(request.getKey());
            GetResponse.Builder response = GetResponse.newBuilder();
            
            if (value != null) {
                response.setValue(ByteString.copyFrom(value));
                response.setFound(true);
            } else {
                response.setFound(false);
            }
            
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
            log.info("GET: " + request.getKey());
        } catch (Exception e) {
            log.severe("GET error: " + e.getMessage());
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void delete(DeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            kvService.delete(request.getKey());
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
            log.info("DELETE: " + request.getKey());
        } catch (Exception e) {
            log.severe("DELETE error: " + e.getMessage());
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void range(RangeRequest request, StreamObserver<KvPair> responseObserver) {
        try {
            var result = kvService.range(request.getFrom(), request.getTo());
            for (var kv : result) {
                KvPair pair = KvPair.newBuilder()
                    .setKey(kv.getKey())
                    .setValue(kv.getValue() != null ? ByteString.copyFrom(kv.getValue()) : ByteString.EMPTY)
                    .build();
                responseObserver.onNext(pair);
            }
            responseObserver.onCompleted();
            log.info("RANGE: " + request.getFrom() + " -> " + request.getTo() + " (" + result.size() + " items)");
        } catch (Exception e) {
            log.severe("RANGE error: " + e.getMessage());
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void count(Empty request, StreamObserver<CountResponse> responseObserver) {
        try {
            long count = kvService.count();
            CountResponse response = CountResponse.newBuilder()
                .setCount(count)
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("COUNT: " + count);
        } catch (Exception e) {
            log.severe("COUNT error: " + e.getMessage());
            responseObserver.onError(e);
        }
    }
}