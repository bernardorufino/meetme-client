package br.com.bernardorufino.android.meetme.view;

import android.app.Activity;
import android.content.Intent;

public abstract class BaseActivity extends Activity {

    // Stuff to be used by other activities

    protected void goTo(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

}
