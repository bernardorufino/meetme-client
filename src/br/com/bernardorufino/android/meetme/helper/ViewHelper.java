package br.com.bernardorufino.android.meetme.helper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;
import br.com.bernardorufino.android.meetme.Definitions;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ViewHelper {

    public static void flash(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void flash(Context context, int stringResource) {
        String message = context.getResources().getString(stringResource);
        flash(context, message);
    }

    public static String withNamespace(String string) {
        return Definitions.NAMESPACE + "." + string;
    }

    //TODO: Not workking... For some reason doesn't build UI, so keeps waiting user input for ever
    // Maybe because I just tested in onCreate() and perhaps can't update UI without activity set up
    public static boolean blockingDialog(Context context, String message, String positiveButton,
                                         String negativeButton) throws InterruptedException {
        final AtomicBoolean result = new AtomicBoolean(); // Just a mutable wrapper for the result
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                synchronized (result) {
                    result.set(true);
                    result.notify();
                }
            }
        });
        if (negativeButton != null) {
            builder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    synchronized (result) {
                        result.set(false);
                        result.notify();
                    }
                }
            });
        } else builder.setCancelable(false);
        builder.show();
        synchronized (result) {
            result.wait();
            return result.get();
        }
    }

    // Prevents instantiation
    private ViewHelper() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }

}
