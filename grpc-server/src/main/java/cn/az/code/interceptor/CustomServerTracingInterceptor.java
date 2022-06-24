package cn.az.code.interceptor;

import cn.az.code.support.OpenTracingContextKey;
import cn.az.code.support.OperationNameConstructor;
import com.google.common.collect.ImmutableMap;
import io.grpc.BindableService;
import io.grpc.ClientCall;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author ycpang
 * @since 2021-09-15 11:20
 */
public class CustomServerTracingInterceptor implements ServerInterceptor {

    private final Tracer tracer;
    private final OperationNameConstructor operationNameConstructor;
    private final boolean streaming;
    private final boolean verbose;
    private final Set<ServerRequestAttribute> tracedAttributes;

    public CustomServerTracingInterceptor(Tracer tracer) {
        this.tracer = tracer;
        this.operationNameConstructor = OperationNameConstructor.DEFAULT;
        this.streaming = false;
        this.verbose = false;
        this.tracedAttributes = new HashSet<>(Arrays.asList(ServerRequestAttribute.HEADERS,
            ServerRequestAttribute.METHOD_NAME, ServerRequestAttribute.METHOD_TYPE, ServerRequestAttribute.CALL_ATTRIBUTES));
    }

    private CustomServerTracingInterceptor(Tracer tracer, OperationNameConstructor operationNameConstructor, boolean streaming,
                                           boolean verbose, Set<ServerRequestAttribute> tracedAttributes) {
        this.tracer = tracer;
        this.operationNameConstructor = operationNameConstructor;
        this.streaming = streaming;
        this.verbose = verbose;
        this.tracedAttributes = tracedAttributes;
    }

    public ServerServiceDefinition intercept(ServerServiceDefinition serviceDef) {
        return ServerInterceptors.intercept(serviceDef, this);
    }

    /**
     * Add tracing to all requests made to this service.
     *
     * @param bindableService to intercept
     * @return the serviceDef with a tracing interceptor
     */
    public ServerServiceDefinition intercept(BindableService bindableService) {
        return ServerInterceptors.intercept(bindableService, this);
    }

    /**
     * Intercept {@link ServerCall} dispatch by the {@code next} {@link ServerCallHandler}. General
     * semantics of {@link ServerCallHandler#startCall} apply and the returned
     * {@link ServerCall.Listener} must not be {@code null}.
     *
     * <p>If the implementation throws an exception, {@code call} will be closed with an error.
     * Implementations must not throw an exception if they started processing that may use {@code
     * call} on another thread.
     *
     * @param call    object to receive response messages
     * @param headers which can contain extra call metadata from {@link ClientCall#start},
     *                e.g. authentication credentials.
     * @param next    next processor in the interceptor chain
     * @return listener for processing incoming messages for {@code call}, never {@code null}.
     */
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        Map<String, String> headerMap = new HashMap<>();
        for (String k : headers.keys()) {
            if (!k.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                String v = headers.get(Metadata.Key.of(k, Metadata.ASCII_STRING_MARSHALLER));
                headerMap.put(k, v);
            }
        }
        final String operateName = this.operationNameConstructor.constructOperationName(call.getMethodDescriptor());
        final Span span = this.getSpanFromHeaders(headerMap, operateName);

        for (CustomServerTracingInterceptor.ServerRequestAttribute attr : this.tracedAttributes) {
            switch (attr) {
                case METHOD_TYPE:
                    span.setTag("grpc.method_type", call.getMethodDescriptor().getType().toString());
                    break;
                case METHOD_NAME:
                    span.setTag("grpc.method_name", call.getMethodDescriptor().getFullMethodName());
                    break;
                case CALL_ATTRIBUTES:
                    span.setTag("grpc.call_attributes", call.getAttributes().toString());
                    break;
                case HEADERS:
                    span.setTag("grpc.headers", headers.toString());
                    break;
                default:
                    break;
            }
        }
        Context ctxWithSpan = Context.current().withValue(OpenTracingContextKey.getKey(), span);
        ServerCall.Listener<ReqT> listenerWithContext = Contexts.interceptCall(ctxWithSpan, call, headers, next);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listenerWithContext) {

            @Override
            public void onMessage(ReqT message) {
                if (streaming || verbose) {
                    span.log(ImmutableMap.of("Message received", message));
                }
                delegate().onMessage(message);
            }

            @Override
            public void onHalfClose() {
                if (streaming) {
                    span.log("Client finished sending messages");
                }
                delegate().onHalfClose();
            }

            @Override
            public void onCancel() {
                span.log("Call cancelled");
                span.finish();
                delegate().onCancel();
            }

            @Override
            public void onComplete() {
                if (verbose) {
                    span.log("Call completed");
                }
                span.finish();
                delegate().onComplete();
            }
        };
    }

    private Span getSpanFromHeaders(Map<String, String> headers, String operateName) {
        Tracer.SpanBuilder spanBuilder;
        try {
            SpanContext parentSpanCtx = this.tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headers));
            if (Objects.isNull(parentSpanCtx)) {
                spanBuilder = this.tracer.buildSpan(operateName);
            } else {
                spanBuilder = this.tracer.buildSpan(operateName).asChildOf(parentSpanCtx);
            }
        } catch (IllegalArgumentException e) {
            spanBuilder = this.tracer.buildSpan(operateName)
                .withTag("Error", "Extract failed and an IllegalArgumentException was thrown");
        }
        return spanBuilder.start();
    }

    public enum ServerRequestAttribute {
        /**
         * Request Attr
         */
        HEADERS,
        METHOD_TYPE,
        METHOD_NAME,
        CALL_ATTRIBUTES
    }
}
