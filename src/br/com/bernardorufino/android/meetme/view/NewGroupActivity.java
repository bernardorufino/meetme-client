package br.com.bernardorufino.android.meetme.view;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import br.com.bernardorufino.android.meetme.Definitions;
import br.com.bernardorufino.android.meetme.R;
import br.com.bernardorufino.android.meetme.helper.ViewHelper;
import br.com.bernardorufino.android.meetme.model.Group;
import br.com.bernardorufino.android.meetme.model.User;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;

import java.io.IOException;

import static br.com.bernardorufino.android.meetme.Definitions.*;

public class NewGroupActivity extends BaseActivity {
    private EditText userNameInput;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_group);

        // Enable navigate up button
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Sets up view components
        userNameInput = (EditText) findViewById(R.id.userNameInput);

    }

    public void createGroupClick(View view) {
        new AsyncTask<Void, Void, Intent>() {

            protected Intent doInBackground(Void... params) {
                try {
                    String userName = userNameInput.getText().toString();
                    User user = new User(userName, INITIAL_POSITION);
                    Group group = Group.create(user);
                    Intent intent = new Intent(NewGroupActivity.this, MapActivity.class);
                    intent.putExtra(ViewHelper.withNamespace("group"), group);
                    intent.putExtra(ViewHelper.withNamespace("user"), user);
                    return intent;
                } catch (IOException e) {
                    return null;
                }
            }

            protected void onPostExecute(Intent intent) {
                if (intent == null) {
                    ViewHelper.flash(NewGroupActivity.this, "deu merda na l.53 tio");
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