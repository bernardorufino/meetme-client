package br.com.bernardorufino.android.meetme.lib;

import android.os.AsyncTask;
import android.os.Handler;
import br.com.bernardorufino.android.meetme.helper.Helper;

import java.util.concurrent.Executor;

// main thread = thread from which this object was created
// worker thread = new thread properly allocated to running timed task
public abstract class TimedTask {

    // Default parallel executor, reuses threads already created with AsyncTask (not serial)
    public static final Executor EXECUTOR = AsyncTask.THREAD_POOL_EXECUTOR;

    private static Executor executor = EXECUTOR;

    public static void setExecutor(Executor givenExecutor) { executor = givenExecutor; }

    // Will run periodically on worker thread, releasing main thread
    public abstract void work();

    // Will run on main thread after completion of work(), for UI updates for example
    public abstract void onComplete();

    // Override for handling case when worker thread is stopped
    // Will run on main thread
    public void onCancel() {
        Helper.log("Worker thread canceled");
    }

    private Runnable onComplete;
    private Runnable onCancel;
    private Handler main;
    private volatile boolean run;

    // Should be called from main thread (probably UI thread)
    public TimedTask() {
        main = new Handler();
        onComplete = new Runnable() {
            public void run() { onComplete(); }
        };
        onCancel = new Runnable() {
            public void run() { onCancel(); }
        };
    }

    // Execution is not guaranteed to start immediately, however interval is mostly precise
    // Priority here is not the compromise to the overall periodicity, instead the compromise
    // is with each interval. In other words, if intervalInMillis is set to 1000 ms and for some
    // reason the thread waits 800 ms, that does NOT mean the next interval will compensate with
    // perhaps 1200 ms, next interval will try to be most close to 1000 ms again.
    public void fire(final long intervalInMillis) {
        run = true;
        // Below code runs in worker thread
        executor.execute(new Runnable() {
            public void run() {
                while (run) {
                    work();
                    main.post(onComplete);
                    try { Thread.sleep(intervalInMillis); }
                    catch (InterruptedException e) { run = false; }
                }
                main.post(onCancel);
            }
        });
    }

    // Can be called to cancel execution
    public void cancel() {
        run = false;
    }



}
