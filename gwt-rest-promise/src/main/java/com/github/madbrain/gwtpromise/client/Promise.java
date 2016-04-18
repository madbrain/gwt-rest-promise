package com.github.madbrain.gwtpromise.client;

public interface Promise<T> {
    <R> Promise<R> then(ThenHandler<T, R> handler);
    <R> Promise<R> then(ThenHandler<T, R> handler, ErrorHandler<R> errorHandler);
    <R> Promise<R> fail(ErrorHandler<R> handler);
    void done(ThenHandler<T, Void> thenHandler, ErrorHandler<Void> errorHandler);
}
