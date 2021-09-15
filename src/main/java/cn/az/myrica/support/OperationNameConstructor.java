package cn.az.myrica.support;

import io.grpc.MethodDescriptor;

/**
 * @author ycpang
 * @since 2021-09-15 11:58
 */
public interface OperationNameConstructor {

    /**
     * Default span operation name constructor, that will return an RPC's method
     * name when constructOperationName is called.
     */
    OperationNameConstructor DEFAULT = new OperationNameConstructor() {
        @Override
        public <ReqT, RespT> String constructOperationName(MethodDescriptor<ReqT, RespT> method) {
            return method.getFullMethodName();
        }
    };

    /**
     * Constructs a span's operation name from the RPC's method.
     *
     * @param method  the rpc method to extract a name from
     * @param <ReqT>  the rpc request type
     * @param <RespT> the rpc response type
     * @return the operation name
     */
    <ReqT, RespT> String constructOperationName(MethodDescriptor<ReqT, RespT> method);
}
