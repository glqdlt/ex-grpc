package com.glqdlt.ex.grpcexam;

import com.glqdlt.ex.grpcexam.model.User;
import com.glqdlt.ex.grpcexam.model.UserServiceGrpc;
import io.grpc.stub.StreamObserver;

public class SomeService extends UserServiceGrpc.UserServiceImplBase{

    @Override
    public void getUserDetail(User.UserRequest request, StreamObserver<User.UserDetail> responseObserver) {




    }
}
