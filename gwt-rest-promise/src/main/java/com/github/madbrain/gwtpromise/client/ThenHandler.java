package com.github.madbrain.gwtpromise.client;

public interface ThenHandler<T, R> {

    Promise<R> execute(T value);
}
