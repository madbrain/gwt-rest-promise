package com.github.madbrain.gwtpromise.client;

import com.google.gwt.core.client.Scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BasePromise<T> implements Promise<T> {

    private static class IdentityThen<T, R> implements ThenHandler<T, R> {

        @Override
        public Promise<R> execute(T value) {
            return Promises.ok((R)value);
        }
    }

    private static class IdentityError<R> implements ErrorHandler<R> {

        @Override
        public Promise<R> execute(Throwable reason) {
            return Promises.error(reason);
        }
    }

    private static class Future<T, R> {

        private final BasePromise<R> deferred;
        private final ThenHandler<T, R> thenHandler;
        private final ErrorHandler<R> errorHandler;

        public Future(BasePromise<R> deferred, ThenHandler<T, R> then, ErrorHandler<R> error) {
            this.deferred = deferred;
            this.thenHandler = then;
            this.errorHandler = error;
        }

        public void resolve(final T value) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    try {
                        resolveDeferred(thenHandler.execute(value));
                    } catch (Exception e) {
                        rejectDeferred(e);
                    }
                }
            });
        }

        public void reject(final Throwable reason) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    try {
                        resolveDeferred(errorHandler.execute(reason));
                    } catch (Exception e) {
                        rejectDeferred(e);
                    }
                }
            });
        }

        private void rejectDeferred(Throwable caught) {
            if (deferred != null) {
                deferred.reject(caught);
            }
        }

        private void resolveDeferred(Promise<R> value) {
            if (deferred != null) {
                if (value == deferred) {
                    deferred.reject(new RuntimeException("Circular Resolution"));
                } else {
                    deferred.resolve(value);
                }
            }
        }

    }

    interface State<T> {

        State<T> resolve(T value);

        State<T> reject(Throwable caught);

        <R> Promise<R> then(ThenHandler<T, R> handler, ErrorHandler<R> errorHandler);

        void done(ThenHandler<T, Void> handler, ErrorHandler<Void> errorHandler);
    }

    private class PendingState implements State<T> {

        @Override
        public State<T> resolve(T value) {
            resolveFutures(value);
            return new FulfilledState(value);
        }

        @Override
        public State<T> reject(Throwable caught) {
            rejectFutures(caught);
            return new RejectedState(caught);
        }

        @Override
        public <R> Promise<R> then(ThenHandler<T, R> handler, ErrorHandler<R> errorHandler) {
            BasePromise<R> newDeferred = new BasePromise<>();
            futures.add(new Future<>(newDeferred, handler, errorHandler));
            return newDeferred;
        }

        @Override
        public void done(ThenHandler<T, Void> handler, ErrorHandler<Void> errorHandler) {
            futures.add(new Future<>(null, handler, errorHandler));
        }

        private void resolveFutures(T value) {
            for (Future<T, ?> future : futures) {
                future.resolve(value);
            }
            futures = Collections.emptyList();
        }

        private void rejectFutures(Throwable reason) {
            for (Future<T, ?> future : futures) {
                future.reject(reason);
            }
            futures = Collections.emptyList();
        }
    }

    private abstract class ResolvedState implements State<T> {

        @Override
        public State<T> resolve(T value) {
            return this;
        }

        @Override
        public State<T> reject(Throwable caught) {
            return this;
        }

        @Override
        public <R> Promise<R> then(ThenHandler<T, R> handler, ErrorHandler<R> errorHandler) {
            BasePromise<R> newDeferred = new BasePromise<>();
            resolve(new Future<>(newDeferred, handler, errorHandler));
            return newDeferred;
        }

        @Override
        public void done(ThenHandler<T, Void> handler, ErrorHandler<Void> errorHandler) {
            resolve(new Future<>(null, handler, errorHandler));
        }

        protected abstract <R> void resolve(Future<T, R> future);
    }

    private class RejectedState extends ResolvedState {

        private final Throwable reason;

        public RejectedState(Throwable reason) {
            this.reason = reason;
        }

        @Override
        protected <R> void resolve(Future<T, R> future) {
            future.reject(reason);
        }

    }

    private class FulfilledState extends ResolvedState {

        private final T outcome;

        public FulfilledState(T value) {
            this.outcome = value;
        }

        @Override
        protected <R> void resolve(Future<T, R> future) {
            future.resolve(outcome);
        }
    }

    private State<T> state = new PendingState();
    private List<Future<T, ?>> futures = new ArrayList<>();

    @Override
    public <R> Promise<R> then(ThenHandler<T, R> handler) {
        return state.then(handler, new IdentityError<R>());
    }

    @Override
    public <R> Promise<R> then(ThenHandler<T, R> handler, ErrorHandler<R> errorHandler) {
        return state.then(handler, errorHandler);
    }

    @Override
    public void done(ThenHandler<T, Void> handler, ErrorHandler<Void> errorHandler) {
        state.done(handler, errorHandler);
    }

    @Override
    public <R> Promise<R> fail(ErrorHandler<R> handler) {
        return state.then(new IdentityThen<T, R>(), handler);
    }

    protected void resolve(T value) {
        state = state.resolve(value);
    }

    protected void reject(Throwable caught) {
        state = state.reject(caught);
    }

    protected void resolve(Promise<T> promise) {
        if (promise == null) {
            resolve((T)null);
            return;
        }
        try {
            promise.done(
                    new ThenHandler<T, Void>() {
                        @Override
                        public Promise<Void> execute(T value) {
                            resolve(value);
                            return null;
                        }

                    },
                    new ErrorHandler<Void>() {
                        @Override
                        public Promise<Void> execute(Throwable reason) {
                            reject(reason);
                            return null;
                        }
                    }
            );
        } catch (Exception exception) {
            reject(exception);
        }
    }

}
