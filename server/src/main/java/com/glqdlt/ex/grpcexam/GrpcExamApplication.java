package com.glqdlt.ex.grpcexam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GrpcExamApplication implements CommandLineRunner {

    @Autowired
    private GrpcServer grpcServer;

    public static void main(String[] args) {
        SpringApplication.run(GrpcExamApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        grpcServer.start();
    }
}

