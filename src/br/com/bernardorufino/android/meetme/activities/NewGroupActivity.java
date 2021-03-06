package br.com.bernardorufino.android.meetme.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import br.com.bernardorufino.android.meetme.R;
import br.com.bernardorufino.android.meetme.helper.Helper;
import br.com.bernardorufino.android.meetme.helper.ViewHelper;
import br.com.bernardorufino.android.meetme.model.Group;
import br.com.bernardorufino.android.meetme.model.User;
import br.com.bernardorufino.android.meetme.activities.map.MapActivity;

import java.io.IOException;

import static br.com.bernardorufino.android.meetme.Definitions.*;

public class NewGroupActivity extends BaseActivity {
    private EditText userNameInput;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_group);

        // Enable navigate up button
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Sets up activities components
        userNameInput = (EditText) findViewById(R.id.userNameInput);

    }

    private String userName;

    private boolean validateInput() {
        boolean valid = true;

        // User name
        userNameInput.setError(null);
        userName = userNameInput.getText().toString().trim();
        if (userName.isEmpty()) {
            userNameInput.setError("Seu nome não pode ser vazio");
            valid = false;
        }

        return valid;
    }

    public void createGroupClick(View view) {
        if (!validateInput()) return;
        final ProgressDialog dialog = ProgressDialog.show(this, "Novo Groupo", "Criando grupo");
        new AsyncTask<Void, Void, Intent>() {
            private Exception exception;

            protected Intent doInBackground(Void... params) {
                try {
                    User user = new User(userName, INITIAL_POSITION);
                    Group group = Group.create(user);
                    Intent intent = new Intent(NewGroupActivity.this, MapActivity.class);
                    intent.putExtra(ViewHelper.withNamespace("group"), group);
                    intent.putExtra(ViewHelper.withNamespace("user"), user);
                    exception = null;
                    return intent;
                } catch (IOException e) {
                    exception = e;
                    return null;
                }
            }

            protected void onPostExecute(Intent intent) {
                dialog.hide();
                if (intent == null) {
                    ViewHelper.flash(NewGroupActivity.this,
                        (Helper.isInternetException(exception))
                            ? getString(R.string.internet_error)
                            : getString(R.string.generic_error)
                    );
                    Helper.logException(exception);
                } else {
                    startActivity(intent);
                }
            }

        }.execute();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}