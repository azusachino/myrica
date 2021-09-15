package cn.az.myrica;

import cn.az.myrica.hello.HelloGrpcServer;
import io.jaegertracing.Configuration;
import io.opentracing.Tracer;

/**
 * GRPC server
 *
 * @author ycpang
 * @since 2021-09-15 17:08
 */
public class MyricaApplication {

    public static void main(String[] args) throws Exception {
        // global tracer
        Configuration configuration = new Configuration("myrica-grpc-server");

        Tracer tracer = configuration.getTracer();
        HelloGrpcServer server = new HelloGrpcServer(tracer);
        server.start();
        server.blockUntilShutdown();
    }
}
