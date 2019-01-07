package com.glqdlt.ex.grpcexam;

import com.glqdlt.ex.grpcexam.model.SImpleServiceGrpc;
import com.glqdlt.ex.grpcexam.model.Simple;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimpleServiceImpl extends SImpleServiceGrpc.SImpleServiceImplBase {

    Logger logger
            = LoggerFactory.getLogger(SimpleServiceImpl.class);

    private List<Simple.SimpleResponse> generatedStreamData() {
        return IntStream.rangeClosed(0, 10).boxed().map(x ->
                Simple.SimpleResponse.newBuilder()
                        .setSeq(x)
                        .setMessage("Helloo ! + " + x)
                        .build()
        ).collect(Collectors.toList());
    }

    @Override
    public void serverToClientStream(Simple.SimpleRequest request, StreamObserver<Simple.SimpleResponse> responseObserver) {
        generatedStreamData().forEach(x -> {
            logger.info("Push!! to Client!!");
            responseObserver.onNext(x);
        });
        logger.info("Done!");
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Simple.SimpleRequest> bidirectionStream(StreamObserver<Simple.SimpleResponse> responseObserver) {
        return super.bidirectionStream(responseObserver);
    }
}
