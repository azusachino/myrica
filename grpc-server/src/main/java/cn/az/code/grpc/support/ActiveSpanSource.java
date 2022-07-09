package cn.az.code.grpc.support;

import io.opentracing.Span;

/**
 * An interface that defines how to get the current active span
 *
 * @author ycpang
 * @since 2021-09-15 12:13
 */
public interface ActiveSpanSource {

    ActiveSpanSource NONE = () -> null;

    /**
     * ActiveSpanSource implementation that returns the
     * current span stored in the GRPC context under
     * {@link OpenTracingContextKey}
     */
    ActiveSpanSource GRPC_CONTEXT = OpenTracingContextKey::activeSpan;

    /**
     * get active span
     *
     * @return the active span
     */
    Span getActiveSpan();
}
