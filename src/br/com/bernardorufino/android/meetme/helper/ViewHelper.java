package br.com.bernardorufino.android.meetme.helper;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import br.com.bernardorufino.android.meetme.Definitions;

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

    // Prevents instantiation
    private ViewHelper() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }

}
