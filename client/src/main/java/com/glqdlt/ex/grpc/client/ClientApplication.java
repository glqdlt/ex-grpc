package com.glqdlt.ex.grpc.client;

import com.glqdlt.ex.grpcexam.model.SImpleServiceGrpc;
import com.glqdlt.ex.grpcexam.model.Simple;
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

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

        Simple.SimpleRequest request = Simple.SimpleRequest.newBuilder()
                .setSeq(1)
                .build();


        // Async Single
        SImpleServiceGrpc.SImpleServiceStub asyncSingle = SImpleServiceGrpc.newStub(channel);
        asyncSingle.serverToClient(request, new StreamObserver<Simple.SimpleResponse>() {
            @Override
            public void onNext(Simple.SimpleResponse simpleResponse) {
                logger.info("async Single : {}", simpleResponse.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error(throwable.getMessage(), throwable);
            }

            @Override
            public void onCompleted() {
                logger.info("async Single Done");
            }
        });


        // Async streams..
        SImpleServiceGrpc.SImpleServiceStub async = SImpleServiceGrpc.newStub(channel);
        async.serverToClientStream(request, new StreamObserver<Simple.SimpleResponse>() {
            @Override
            public void onNext(Simple.SimpleResponse simpleResponse) {
                logger.info("async : {}", simpleResponse.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error(throwable.getMessage(), throwable);
            }

            @Override
            public void onCompleted() {
                logger.info("async received server finish call");
                logger.info("async push finished");

            }
        });


        // BLocking Single

        SImpleServiceGrpc.SImpleServiceBlockingStub rrrr = SImpleServiceGrpc.newBlockingStub(channel);
        Simple.SimpleResponse eeee = rrrr.serverToClient(request);
        logger.info("sync Single message : {}", eeee.getMessage());
        logger.info("sync Single Message Done");


//        Blocking streams..
        SImpleServiceGrpc.SImpleServiceBlockingStub ssss = SImpleServiceGrpc.newBlockingStub(channel);
        Iterator<Simple.SimpleResponse> res = ssss.serverToClientStream(request);

        for (; res.hasNext(); ) {
            Simple.SimpleResponse s = res.next();
            logger.info("sync message : {}", s.getMessage());
        }
        logger.info("sync received server finish call");
        logger.info("sync push finished");


        channel.awaitTermination(10, TimeUnit.SECONDS);
        logger.info("Channel Terminated");
    }

}

