package br.com.bernardorufino.android.meetme.lib.concurrency;

import android.os.AsyncTask;
import android.os.Handler;
import br.com.bernardorufino.android.meetme.helper.Helper;

import java.util.concurrent.Executor;

// mMainHandler thread = thread from which this object was created
// worker thread = new thread properly allocated to running timed task
public abstract class TimedTask {

    // Default parallel sExecutor, reuses threads already created with AsyncTask (not serial)
    public static final Executor EXECUTOR = AsyncTask.THREAD_POOL_EXECUTOR;

    private static Executor sExecutor = EXECUTOR;

    public static void setExecutor(Executor givenExecutor) { sExecutor = givenExecutor; }

    // Will mRun periodically on worker thread, releasing mMainHandler thread
    public abstract void work();

    // Will mRun on mMainHandler thread after completion of work(), for UI updates for example
    public void onComplete() { /* Empty */ }

    // Override for handling case when worker thread is stopped
    // Will mRun on mMainHandler thread
    public void onCancel() {
        Helper.log("Worker thread canceled");
    }

    private static long UNDEFINED_INTERVAL = -1;

    private Runnable mOnComplete;
    private Runnable mOnCancel;
    private Handler mMainHandler;
    private volatile boolean mRun;
    private long mInterval = UNDEFINED_INTERVAL;

    // Should be called from mMainHandler thread (probably UI thread)
    public TimedTask() {
        mMainHandler = new Handler();
        mOnComplete = new Runnable() {
            public void run() { onComplete(); }
        };
        mOnCancel = new Runnable() {
            public void run() { onCancel(); }
        };
    }

    public TimedTask(long intervalInMillis) {
        mInterval = intervalInMillis;
    }

    public void setInterval(long intervalInMillis) {
        mInterval = intervalInMillis;
    }

    // Execution is not guaranteed to start immediately, however interval is mostly precise
    // Priority here is not the compromise to the overall periodicity, instead the compromise
    // is with each interval. In other words, if intervalInMillis is set to 1000 ms and for some
    // reason the thread waits 800 ms, that does NOT mean the next interval will compensate with
    // perhaps 1200 ms, next interval will try to be most close to 1000 ms again.
    public void fire(final long intervalInMillis) {
        mRun = true;
        // Below code runs in worker thread
        sExecutor.execute(new Runnable() {
            public void run() {
                while (mRun) {
                    work();
                    mMainHandler.post(mOnComplete);
                    try {
                        Thread.sleep(intervalInMillis);
                    } catch (InterruptedException e) {
                        mRun = false;
                    }
                }
                mMainHandler.post(mOnCancel);
            }
        });
    }

    public void fire() {
        if (mInterval == UNDEFINED_INTERVAL) {
            throw new RuntimeException("Interval was not set");
        }
        fire(mInterval);
    }

    // Can be called to cancel execution
    public void cancel() {
        mRun = false;
    }

}
