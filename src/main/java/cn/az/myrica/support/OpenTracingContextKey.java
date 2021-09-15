package cn.az.myrica.support;

import io.grpc.Context;
import io.opentracing.Span;

/**
 * A Context key for the current OpenTracing trace state. Can be used to get the active span, or to set the active span for a scoped unit of work.
 *
 * @author ycpang
 * @since 2021-09-15 12:09
 */
public class OpenTracingContextKey {

    public static final String KEY_NAME = "io.opentracing.active-span";
    private static final Context.Key<Span> KEY = Context.key(KEY_NAME);

    /**
     * @return the active span for the current request
     */
    public static Span activeSpan() {
        return KEY.get();
    }

    /**
     * @return the OpenTracing context key
     */
    public static Context.Key<Span> getKey() {
        return KEY;
    }
}
