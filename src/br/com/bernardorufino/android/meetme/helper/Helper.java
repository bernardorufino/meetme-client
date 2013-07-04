package br.com.bernardorufino.android.meetme.helper;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

public class Helper {
    public static final String LOG_TAG = "MEETME";
    public static final AtomicLong timer = new AtomicLong(System.nanoTime());

    public static boolean hasPlayServices(Context context) {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
    }

    public static void log(String message) {
        Log.d(LOG_TAG, message);
    }

    public static void logException(Exception e) {
        if (e != null)
            Log.e(LOG_TAG, e.toString(), e);
    }

    public static long time() {
        long window, now = System.nanoTime();
        synchronized (timer) {
            window = now - timer.get();
            timer.set(now);
        }
        return window;
    }

    public static long millisTime() {
        return time() / 1000000;
    }

    public static boolean isInternetException(Exception e) {
        return (e instanceof UnknownHostException)
            || (e instanceof ConnectException);
    }

    // Prevents instantiation
    private Helper() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }

}
