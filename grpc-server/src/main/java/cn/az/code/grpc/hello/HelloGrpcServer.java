package cn.az.code.grpc.hello;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import cn.az.code.grpc.hello.proto.HelloServiceGrpc;
import cn.az.code.grpc.hello.proto.Request;
import cn.az.code.grpc.hello.proto.Response;
import cn.az.code.grpc.interceptor.CustomServerTracingInterceptor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.opentracing.Tracer;

/**
 * Hello
 *
 * @author ycpang
 * @since 2021-09-15 17:11
 */
public class HelloGrpcServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloGrpcServer.class.getName());

    private Server server;
    private final Tracer tracer;

    public HelloGrpcServer(Tracer tracer) {
        this.tracer = tracer;
    }

    public void start() throws IOException {
        CustomServerTracingInterceptor tracingInterceptor = new CustomServerTracingInterceptor(this.tracer);
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(tracingInterceptor.intercept(new HelloImpl()))
                .build()
                .start();
        LOGGER.info("server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new ThreadFactoryBuilder().build().newThread(
                () -> {
                    LOGGER.warn("shutting down gRPC server since JVM is shutting down.");
                    try {
                        HelloGrpcServer.this.stop();
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.err);
                    }
                    LOGGER.warn("*** server shut down");
                }));
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon
     * threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    static class HelloImpl extends HelloServiceGrpc.HelloServiceImplBase {
        @Override
        public void sayHello(Request request, StreamObserver<Response> responseObserver) {
            Response response = Response.newBuilder()
                    .setMsg("Hello " + request.getMsg() + ", today is " + request.getDate()).setCode(200).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
