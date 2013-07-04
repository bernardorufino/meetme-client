package br.com.bernardorufino.android.meetme.helper;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class Helper {
    public static final String LOG_TAG = "MEETME";

    public static boolean hasPlayServices(Context context) {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
    }

    public static void log(String message) {
        Log.d(LOG_TAG, message);
    }

    // Prevents instantiation
    private Helper() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }

}
