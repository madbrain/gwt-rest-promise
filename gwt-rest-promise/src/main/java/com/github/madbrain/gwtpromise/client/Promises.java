package com.github.madbrain.gwtpromise.client;

public class Promises {

    private Promises() {}

    public static <R> Promise<R> error(Throwable reason) {
        BasePromise<R> promise = new BasePromise<>();
        promise.reject(reason);
        return promise;
    }

    public static <R> Promise<R> ok() {
        return new BasePromise<>();
    }

    public static <R> Promise<R> ok(R value) {
        BasePromise<R> promise = new BasePromise<>();
        promise.resolve(value);
        return promise;
    }
}
