package com.glqdlt.ex.grpc.client;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SimpleHookClientInterceptor implements ClientInterceptor {
    private final static Logger logger = LoggerFactory.getLogger(SimpleHookClientInterceptor.class);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        logger.info("method : {}, callOpt : {}, next : {}", method.getType(), callOptions.getCredentials(), next.authority());
        return next.newCall(method, callOptions);
    }
}
