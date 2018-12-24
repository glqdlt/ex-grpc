package com.glqdlt.ex.grpc.client;

import com.glqdlt.ex.grpcexam.model.User;
import com.glqdlt.ex.grpcexam.model.UserServiceGrpc;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.*;
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

    private void callBack(Future<User.UserDetail> futre){
        try {
            logger.info("Received! Response : {}", futre.get());
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", port)
                .usePlaintext()
                .build();
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);
        User.UserRequest req = User.UserRequest.newBuilder().setId(REQUEST_ID).build();

        ListenableFuture<User.UserDetail> serverResponse = UserServiceGrpc.newFutureStub(channel).getUserDetail(req);
        serverResponse.addListener(() -> callBack(serverResponse), pool);
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
        pool.shutdown();
        logger.info("Thread Pool Shutdown.");
    }

}

