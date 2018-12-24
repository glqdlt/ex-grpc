package com.glqdlt.ex.grpc.client;

import com.glqdlt.ex.grpcexam.model.User;
import com.glqdlt.ex.grpcexam.model.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClientApplication implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(ClientApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 29999)
                .usePlaintext()
                .build();

        UserServiceGrpc.UserServiceBlockingStub blockingStub = UserServiceGrpc.newBlockingStub(channel);
//        UserServiceGrpc.UserServiceStub stub = UserServiceGrpc.newStub(channel);
//        UserServiceGrpc.UserServiceFutureStub featureStub = UserServiceGrpc.newFutureStub(channel);

        User.UserRequest req = User.UserRequest.newBuilder().setId("glqdlt").build();
        User.UserDetail res = blockingStub.getUserDetail(req);

        logger.info("Result : {}", res);
        channel.shutdown();
    }
}

