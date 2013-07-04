package br.com.bernardorufino.android.meetme.helper;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ViewHelper {

    public static void flash(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void flash(Context context, int stringResource) {
        String message = context.getResources().getString(stringResource);
        flash(context, message);
    }

    // Prevents instantiation
    private ViewHelper() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }

}
