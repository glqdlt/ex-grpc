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
                .intercept(new SimpleHookClientInterceptor())
                .build();

        Simple.SimpleRequest request = Simple.SimpleRequest.newBuilder()
                .setSeq(1)
                .build();


        // Async Single
        SImpleServiceGrpc.SImpleServiceStub asyncSingle = SImpleServiceGrpc.newStub(channel);
        asyncSingle.simpleServerToClient(request, new StreamObserver<Simple.SimpleResponse>() {
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
        async.serverSideStream(request, new StreamObserver<Simple.SimpleResponse>() {
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
        Simple.SimpleResponse eeee = rrrr.simpleServerToClient(request);
        logger.info("sync Single message : {}", eeee.getMessage());
        logger.info("sync Single Message Done");


//        Blocking streams..
        SImpleServiceGrpc.SImpleServiceBlockingStub ssss = SImpleServiceGrpc.newBlockingStub(channel);
        Iterator<Simple.SimpleResponse> res = ssss.serverSideStream(request);

        for (; res.hasNext(); ) {
            Simple.SimpleResponse s = res.next();
            logger.info("sync message : {}", s.getMessage());
        }
        logger.info("sync received server finish call");
        logger.info("sync push finished");


        // client side steram..
        // 클라이언트에서 최초 reqeust 외에 request frame을 여러번 server 에 호출한다. server 는 한번의 response 만 응답한다.
        SImpleServiceGrpc.SImpleServiceStub ddd = SImpleServiceGrpc.newStub(channel);
        StreamObserver<Simple.SimpleRequest> requestStream = ddd.clientSideStream(new StreamObserver<Simple.SimpleResponse>() {
            //            아래는 최초 request 에 의해 최후에 올 response 에 대한 응답 처리
            @Override
            public void onNext(Simple.SimpleResponse simpleResponse) {
                logger.info("몰까요 : {}", simpleResponse.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error(throwable.getMessage(), throwable);
            }

            @Override
            public void onCompleted() {
                logger.info("완료!");
            }
        });

//        실제 request 는 아래에서 이루어진다. 아래 코드는 10번의 request 를 실어서 서버에 보낸다.
        IntStream.rangeClosed(1, 10).forEach(x -> {
            logger.info("Call.. Multi Request No" + x);
            requestStream.onNext(request);
        });
        requestStream.onCompleted();


//        양방향 스트리밍
        SImpleServiceGrpc.SImpleServiceStub yy = SImpleServiceGrpc.newStub(channel);
        StreamObserver<Simple.SimpleRequest> aa = yy.bidirectionalStream(new StreamObserver<Simple.SimpleResponse>() {
            @Override
            public void onNext(Simple.SimpleResponse simpleResponse) {
                logger.info("양방향 {} ",simpleResponse.getMessage());

            }

            @Override
            public void onError(Throwable throwable) {
                logger.error(throwable.getMessage(),throwable);
            }

            @Override
            public void onCompleted() {
                logger.info("양방향 Done");
            }
        });
        aa.onNext(Simple.SimpleRequest.newBuilder().setSeq(11).build());
        aa.onNext(Simple.SimpleRequest.newBuilder().setSeq(22).build());
        aa.onNext(Simple.SimpleRequest.newBuilder().setSeq(33).build());
        aa.onCompleted();



        // channel closed..
        // 10초 뒤에 종료하는 것은 비동기 를 무시하고 종료할 수 있기 때문에 기다리기 위함.
        channel.awaitTermination(20, TimeUnit.SECONDS);
        logger.info("Channel Terminated");


    }


}

