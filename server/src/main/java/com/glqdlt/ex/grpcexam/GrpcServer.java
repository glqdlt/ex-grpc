package com.glqdlt.ex.grpcexam;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class GrpcServer {

    private final Logger logger = LoggerFactory.getLogger(GrpcServer.class);

    private static final String prop = "grpc.server.port";

    @Value("${grpc.server.port}")
    private Integer port;

    @Autowired
    private UserServiceGrpcImplement userServiceGrpcImplement;

    private SimpleServiceImpl simpleService = new SimpleServiceImpl();

    private Server server;

    public void start() throws IOException, InterruptedException {
        if (port == null || port == 0) {
            logger.error("gRpc Server Port is Not SetUp..! '{}' need check!", prop);
            System.exit(-1);
        }
        server = ServerBuilder.forPort(port)
                .addService(userServiceGrpcImplement)
                .addService(simpleService)
                .build();
        server.start();
        logger.info("gRPC Server  Started! Port : {} ", server.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(GrpcServer.this::stop));
        server.awaitTermination();
    }

    private void stop() {
        server.shutdown();
    }

}
