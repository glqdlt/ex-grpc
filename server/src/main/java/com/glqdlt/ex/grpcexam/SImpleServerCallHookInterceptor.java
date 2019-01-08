package com.glqdlt.ex.grpcexam;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SImpleServerCallHookInterceptor implements io.grpc.ServerInterceptor {
    Logger logger = LoggerFactory.getLogger(SImpleServerCallHookInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        Context ctx = Context.current();
        logger.info("ctx : {},headers : {}, call : {}", ctx.toString(), headers.toString(), call.toString());
        return Contexts.interceptCall(ctx, call, headers, next);
    }
}
