package com.example.kv;

import com.example.kv.grpc.KvServiceImpl;
import com.example.kv.repository.TarantoolRepository;
import com.example.kv.service.KvService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.util.logging.Logger;

public class ServerApp {
    
    private static final Logger log = Logger.getLogger(ServerApp.class.getName());
    private static final int PORT = 9090;
    
    public static void main(String[] args) throws Exception {
        log.info("Starting KV gRPC Server...");
        
        TarantoolRepository repository = new TarantoolRepository();
        KvService kvService = new KvService(repository);
        KvServiceImpl grpcService = new KvServiceImpl(kvService);
        
        Server server = ServerBuilder
                .forPort(PORT)
                .addService(grpcService)
                .build();
        
        server.start();
        
        log.info("✓ Server started on port " + PORT);
        log.info("✓ gRPC endpoint: localhost:" + PORT);
        log.info("✓ Tarantool endpoint: localhost:3301");
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down...");
            server.shutdown();
            kvService.close();
            log.info("✓ Server stopped");
        }));
        
        server.awaitTermination();
    }
}