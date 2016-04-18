package com.github.madbrain.gwtpromise.client;

public interface ErrorHandler<R> {

    Promise<R> execute(Throwable e);
}
