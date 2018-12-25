package com.glqdlt.ex.grpc.client;

import com.glqdlt.ex.grpcexam.model.User;
import com.glqdlt.ex.grpcexam.model.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@SpringBootApplication
public class ClientApplication implements CommandLineRunner {

    private static final String REQUEST_ID = "glqdlt";

    @Value("${grpc.server.port}")
    private Integer port;

    private final Logger logger = LoggerFactory.getLogger(ClientApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    private void callBack(User.UserDetail userDetail) {
        logger.info("Received! Response : {}", userDetail);
    }

    @Override
    public void run(String... args) throws Exception {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", port)
                .usePlaintext()
                .build();
        User.UserRequest req = User.UserRequest.newBuilder().setId(REQUEST_ID).build();

        UserServiceGrpc.UserServiceStub serverResponse = UserServiceGrpc.newStub(channel);
        serverResponse.getUserDetail(req, new StreamObserver<User.UserDetail>() {
            @Override
            public void onNext(User.UserDetail userDetail) {
                callBack(userDetail);
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error(throwable.getMessage(), throwable);
            }

            @Override
            public void onCompleted() {
                logger.info("Done!");
            }
        });
        IntStream.rangeClosed(0, 50).forEach(x -> {
            try {
                logger.info("is Done ?  .. : {}", x);
                Thread.sleep(50);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        });
        channel.awaitTermination(5, TimeUnit.SECONDS);
        logger.info("Channel Terminated");
    }

}

