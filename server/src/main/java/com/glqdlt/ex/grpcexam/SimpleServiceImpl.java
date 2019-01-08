package com.glqdlt.ex.grpcexam;

import com.glqdlt.ex.grpcexam.model.SImpleServiceGrpc;
import com.glqdlt.ex.grpcexam.model.Simple;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.aop.ThrowsAdvice;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimpleServiceImpl extends SImpleServiceGrpc.SImpleServiceImplBase {

    Logger logger
            = LoggerFactory.getLogger(SimpleServiceImpl.class);

    private List<Simple.SimpleResponse> generatedStreamData(int size) {
        return IntStream.rangeClosed(1, size).boxed().map(x ->
                Simple.SimpleResponse.newBuilder()
                        .setSeq(x)
                        .setMessage("Push Message No." + x)
                        .build()
        ).collect(Collectors.toList());
    }

    @Override
    public void serverSideStream(Simple.SimpleRequest request, StreamObserver<Simple.SimpleResponse> responseObserver) {
//        void 메소드로 보이지만, 사실 responseObserver 가 return 역활을 하는 callback 함수인 것을 명심하자.
//        client 에서 blocking 으로 하던, async 로 처리하던 간에 이 responseObserver 의 onNext 를 기다리고 반응이 올 때만 동작한다.
        generatedStreamData(10).forEach(x -> {
            logger.info("Push to Client.");
            responseObserver.onNext(x);
        });
        logger.info("Done!");
        responseObserver.onCompleted();
    }

    @Override
    public void simpleServerToClient(Simple.SimpleRequest request, StreamObserver<Simple.SimpleResponse> responseObserver) {
        Optional<Simple.SimpleResponse> res = generatedStreamData(1).stream().reduce((x1, x2) -> Simple.SimpleResponse
                .newBuilder()
                .setMessage(x1.getMessage() + x2.getMessage())
                .setSeq(1)
                .build());
        res.ifPresent(responseObserver::onNext);

//        serverToClient(){} 는 스트림을 return 하지 않기 때문에, 여러번 onNext() 로 client 에 데이터를 push 하려하면 에러가난다.
//        io.grpc.StatusRuntimeException: CANCELLED: HTTP/2 error code: CANCEL

//        rpc serverToClientStream(SimpleRequest) returns (stream SimpleResponse){}
//        rpc serverToClient(SimpleRequest) returns (SimpleResponse){}

//                gRPC 에 설계한 것을 보면 위 serverToClientStream 에서는 return stream 으로 보내는 반면,
//        gRPC serverToClient 에서는 return SimpleResponse 만을 return 한다.
//        실제 proto 파일의 구현체인 SImpleServiceBase 에서는
//        serverToClientStream(Simple.SimpleRequest request, StreamObserver<Simple.SimpleResponse> responseObserver) {...}
//        serverToClient(Simple.SimpleRequest request, StreamObserver<Simple.SimpleResponse> responseObserver){...}
//         로 같아보이지만 내부에서 차이가 있다.
//          serverToClient(){..} 에는 내부적으로 MethodType.UNARY 이고, serverToClientStream(){..} 에는 MethodType.SERVER_STREAMING 스트리밍 타입이다.
//         이 차이 때문에 MethodType.UNARY 로 정의 된 메소드에서 아래처럼 onNext() 를 여러번 호출하면 HTTP/2 CANCEL 에러가 나게 된다.

//        generatedStreamData(10).forEach(x -> {
//            logger.info("Push to Client.");
//            responseObserver.onNext(x);
//        });
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Simple.SimpleRequest> bidirectionalStream(StreamObserver<Simple.SimpleResponse> responseObserver) {
        return super.bidirectionalStream(responseObserver);
    }

    @Override
    public StreamObserver<Simple.SimpleRequest> clientSideStream(StreamObserver<Simple.SimpleResponse> responseObserver) {
        // 아래는 client 에서 추가적인 request 가 올 때 대응하는 callback 함수이다.
        return new StreamObserver<Simple.SimpleRequest>() {
            long callCount = 0;
            long started = System.nanoTime();

            @Override
            public void onNext(Simple.SimpleRequest simpleRequest) {
                callCount++;

                logger.info("추가로 요청 오고 있음 call Count : {}", callCount);
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error(throwable.getMessage(), throwable);
            }

            @Override
            public void onCompleted() {
                long finished = TimeUnit.SECONDS.toSeconds(System.nanoTime() - started);
                logger.info("finished Time : {}, call Count : {}", finished, callCount);

                responseObserver.onNext(Simple.SimpleResponse.newBuilder().setMessage("이건머임?"+callCount+"번이나 호출했네.. 총 걸린 시간 : "+finished).setSeq((int)callCount).build());
//                아래는 HTTP 2 CANCEL 에러가 날 것이다. 이유는 client streaming 은 request 를 여러번 받아서 client 가 원하는 형태의 response 를 완성해서 호출해주는 것이기 때문에
//                  response 는 한번만 호출이 될 것이라 기대한다. 만약 response 도 여러번 원하는 것은 양방향 스트리밍을 해야한다.

//                for(int i  = 0 ; i < callCount; i++){
//                    responseObserver.onNext(..);
//                }
                responseObserver.onCompleted();
            }
        };
    }
}
