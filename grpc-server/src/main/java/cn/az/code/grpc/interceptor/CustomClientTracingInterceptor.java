package cn.az.code.grpc.interceptor;

import cn.az.code.grpc.support.ActiveSpanSource;
import cn.az.code.grpc.support.OperationNameConstructor;
import com.google.common.collect.ImmutableMap;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.Context;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * An interceptor that applies tracing via OpenTracing to all client requests.
 *
 * @author ycpang
 * @since 2021-09-15 12:08
 */
public class CustomClientTracingInterceptor implements ClientInterceptor {

    private final Tracer tracer;
    private final OperationNameConstructor operationNameConstructor;
    private final boolean streaming;
    private final boolean verbose;
    private final Set<ClientRequestAttribute> tracedAttributes;
    private final ActiveSpanSource activeSpanSource;

    public CustomClientTracingInterceptor(Tracer tracer) {
        this.tracer = tracer;
        this.operationNameConstructor = OperationNameConstructor.DEFAULT;
        this.streaming = false;
        this.verbose = false;
        this.tracedAttributes = new HashSet<>();
        this.activeSpanSource = ActiveSpanSource.GRPC_CONTEXT;
    }

    public CustomClientTracingInterceptor(Tracer tracer, OperationNameConstructor operationNameConstructor,
                                          boolean streaming,
                                          boolean verbose, Set<ClientRequestAttribute> tracedAttributes, ActiveSpanSource activeSpanSource) {
        this.tracer = tracer;
        this.operationNameConstructor = operationNameConstructor;
        this.streaming = streaming;
        this.verbose = verbose;
        this.tracedAttributes = tracedAttributes;
        this.activeSpanSource = activeSpanSource;
    }

    public Channel intercept(Channel channel) {
        return ClientInterceptors.intercept(channel, this);
    }

    /**
     * Intercept {@link ClientCall} creation by the {@code next} {@link Channel}.
     *
     * <p>
     * Many variations of interception are possible. Complex implementations may
     * return a wrapper
     * around the result of {@code next.newCall()}, whereas a simpler implementation
     * may just modify
     * the header metadata prior to returning the result of {@code next.newCall()}.
     *
     * <p>
     * {@code next.newCall()} <strong>must not</strong> be called under a different
     * {@link Context}
     * other than the current {@code Context}. The outcome of such usage is
     * undefined and may cause
     * memory leak due to unbounded chain of {@code Context}s.
     *
     * @param method      the remote method to be called.
     * @param callOptions the runtime options to be applied to this call.
     * @param next        the channel which is being intercepted.
     * @return the call object for the remote operation, never {@code null}.
     */
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions callOptions, Channel next) {
        final String operationName = operationNameConstructor.constructOperationName(method);

        Span activeSpan = this.activeSpanSource.getActiveSpan();
        final Span span = createSpanFromParent(activeSpan, operationName);

        for (ClientRequestAttribute attr : this.tracedAttributes) {
            switch (attr) {
                case ALL_CALL_OPTIONS:
                    span.setTag("grpc.call_options", callOptions.toString());
                    break;
                case AUTHORITY:
                    if (callOptions.getAuthority() == null) {
                        span.setTag("grpc.authority", "null");
                    } else {
                        span.setTag("grpc.authority", callOptions.getAuthority());
                    }
                    break;
                case COMPRESSOR:
                    if (callOptions.getCompressor() == null) {
                        span.setTag("grpc.compressor", "null");
                    } else {
                        span.setTag("grpc.compressor", callOptions.getCompressor());
                    }
                    break;
                case DEADLINE:
                    if (callOptions.getDeadline() == null) {
                        span.setTag("grpc.deadline_millis", "null");
                    } else {
                        span.setTag("grpc.deadline_millis",
                            callOptions.getDeadline().timeRemaining(TimeUnit.MILLISECONDS));
                    }
                    break;
                case METHOD_NAME:
                    span.setTag("grpc.method_name", method.getFullMethodName());
                    break;
                case METHOD_TYPE:
                    if (method.getType() == null) {
                        span.setTag("grpc.method_type", "null");
                    } else {
                        span.setTag("grpc.method_type", method.getType().toString());
                    }
                    break;
                case HEADERS:
                    break;
                default:
                    break;
            }
        }

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                if (verbose) {
                    span.log("Started call");
                }
                if (tracedAttributes.contains(ClientRequestAttribute.HEADERS)) {
                    span.setTag("grpc.headers", headers.toString());
                }

                tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMap() {
                    @Override
                    public void put(String key, String value) {
                        Metadata.Key<String> headerKey = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
                        headers.put(headerKey, value);
                    }

                    @Override
                    public Iterator<Map.Entry<String, String>> iterator() {
                        throw new UnsupportedOperationException(
                            "TextMapInjectAdapter should only be used with Tracer.inject()");
                    }
                });

                Listener<RespT> tracingResponseListener = new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(
                    responseListener) {

                    @Override
                    public void onHeaders(Metadata headers) {
                        if (verbose) {
                            span.log(ImmutableMap.of("Response headers received", headers.toString()));
                        }
                        delegate().onHeaders(headers);
                    }

                    @Override
                    public void onMessage(RespT message) {
                        if (streaming || verbose) {
                            span.log("Response received");
                        }
                        delegate().onMessage(message);
                    }

                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        if (verbose) {
                            if (status.getCode().value() == 0) {
                                span.log("Call closed");
                            } else {
                                span.log(ImmutableMap.of("Call failed",
                                    Objects.requireNonNull(status.getDescription(), "")));
                            }
                        }
                        span.finish();
                        delegate().onClose(status, trailers);
                    }
                };
                delegate().start(tracingResponseListener, headers);
            }

            @Override
            public void cancel(@Nullable String message, @Nullable Throwable cause) {
                String errorMessage;
                errorMessage = Objects.requireNonNull(message, "Error");
                if (cause == null) {
                    span.log(errorMessage);
                } else {
                    span.log(ImmutableMap.of(errorMessage, cause.getMessage()));
                }
                delegate().cancel(message, cause);
            }

            @Override
            public void halfClose() {
                if (streaming) {
                    span.log("Finished sending messages");
                }
                delegate().halfClose();
            }

            @Override
            public void sendMessage(ReqT message) {
                if (streaming || verbose) {
                    span.log("Message sent");
                }
                delegate().sendMessage(message);
            }
        };
    }

    private Span createSpanFromParent(Span parent, String operateName) {
        if (Objects.isNull(parent)) {
            return this.tracer.buildSpan(operateName).start();
        } else {
            return this.tracer.buildSpan(operateName).asChildOf(parent).start();
        }
    }

    public enum ClientRequestAttribute {
        /**
         * Request Attr
         */
        METHOD_TYPE,
        METHOD_NAME,
        DEADLINE,
        COMPRESSOR,
        AUTHORITY,
        ALL_CALL_OPTIONS,
        HEADERS
    }
}
