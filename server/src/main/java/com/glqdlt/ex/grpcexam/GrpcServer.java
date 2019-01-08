package com.glqdlt.ex.grpcexam;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
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

    private SImpleServerCallHookInterceptor sImpleServerCallHookInterceptor = new SImpleServerCallHookInterceptor();

    private Server server;

    public void start() throws IOException, InterruptedException {
        if (port == null || port == 0) {
            logger.error("gRpc Server Port is Not SetUp..! '{}' need check!", prop);
            System.exit(-1);
        }
        server = ServerBuilder.forPort(port)
                .addService(userServiceGrpcImplement)
//                특정 서비스마다 인터셉터를 걸어서 후킹할 수 있게 할 수 있다. 인터셉터는 여러개를 등록시킬 수 있다. 아래를 참조.
//                이를 응용하면 authentication 를 책임지는 횡단관심사를 해결할 수 있다.
                .addService(ServerInterceptors.intercept(simpleService,sImpleServerCallHookInterceptor))
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
