package br.com.bernardorufino.android.meetme.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import br.com.bernardorufino.android.meetme.Definitions;
import br.com.bernardorufino.android.meetme.R;
import br.com.bernardorufino.android.meetme.helper.Helper;
import br.com.bernardorufino.android.meetme.helper.ViewHelper;
import br.com.bernardorufino.android.meetme.model.Group;
import br.com.bernardorufino.android.meetme.model.User;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;

import java.io.IOException;

import static br.com.bernardorufino.android.meetme.Definitions.*;

public class JoinGroupActivity extends BaseActivity {
    private EditText userNameInput;
    private EditText groupPasswordInput;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_group);

        // Enable navigate up button
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Sets up view components
        userNameInput = (EditText) findViewById(R.id.userNameInput);
        groupPasswordInput = (EditText) findViewById(R.id.groupPasswordInput);

    }

    public void joinGroupClick(View view) {
        final ProgressDialog dialog = ProgressDialog.show(this, "Entrar em Groupo", "Procurando grupo");
        new AsyncTask<Void, Void, Intent>() {
            private Exception exception;

            protected Intent doInBackground(Void... params) {
                try {
                    String userName = userNameInput.getText().toString();
                    User user = new User(userName, INITIAL_POSITION);
                    String groupPassword = groupPasswordInput.getText().toString();
                    Group group = Group.join(groupPassword, user);
                    Intent intent = new Intent(JoinGroupActivity.this, MapActivity.class);
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
                    ViewHelper.flash(JoinGroupActivity.this,
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