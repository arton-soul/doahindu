package com.dearyoti.doahindu.database;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Runs database work away from the UI thread and posts results back to it. */
public final class DatabaseExecutor {

    public interface Callback<T> {
        void onResult(T result);
    }

    private static final ExecutorService IO_EXECUTOR = Executors.newFixedThreadPool(2);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private DatabaseExecutor() {
    }

    public static <T> void execute(Callable<T> operation, Callback<T> onSuccess,
                                   Callback<Exception> onError) {
        IO_EXECUTOR.execute(() -> {
            try {
                T result = operation.call();
                MAIN_HANDLER.post(() -> onSuccess.onResult(result));
            } catch (Exception exception) {
                MAIN_HANDLER.post(() -> onError.onResult(exception));
            }
        });
    }
}
