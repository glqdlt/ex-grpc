package com.glqdlt.ex.grpcexam;

import com.glqdlt.ex.grpcexam.model.User;
import com.glqdlt.ex.grpcexam.model.UserServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserServiceGrpcImplement extends UserServiceGrpc.UserServiceImplBase {
    private final Logger logger = LoggerFactory.getLogger(UserServiceGrpcImplement.class);

    private static final String SOME_USER_ID = "glqdlt";

    private User.UserDetail generateUserDetail() {
        return User.UserDetail.newBuilder()
                .setId(SOME_USER_ID)
                .setAddress("Seoul")
                .setAge(31)
                .setName("Jhun")
                .setPassword("12345")
                .setSex(User.Sex.MAN)
                .addHobbies("Coding")
                .addHobbies("Driving")
                .addHobbies("Walking")
                .putAuth("role", "admin")
                .build();
    }

    @Override
    public void getUserDetail(User.UserRequest request, StreamObserver<User.UserDetail> responseObserver) {
        Optional<String> req = Optional.ofNullable(request.getId());
        logger.info("Get Request : {}",req.orElse("Null?"));
        if (req.isPresent()) {
            if (req.get().toUpperCase().equals(SOME_USER_ID.toUpperCase())) {
                responseObserver.onNext(generateUserDetail());
                responseObserver.onCompleted();
            }
        } else {
            responseObserver.onError(new RuntimeException("Bad Wrong Request..!"));
        }
    }
}
