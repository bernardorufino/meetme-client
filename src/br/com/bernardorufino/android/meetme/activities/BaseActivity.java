package br.com.bernardorufino.android.meetme.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public abstract class BaseActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeView(savedInstanceState);
        initializeListeners(savedInstanceState);
    }

    // Designed to be overriden for being automatically executed on creation
    protected void initializeView(Bundle savedInstanceState) { /* Empty */ }

    // Designed to be overriden for being automatically executed on creation
    protected void initializeListeners(Bundle savedInstanceState) { /* Empty */ }

}
