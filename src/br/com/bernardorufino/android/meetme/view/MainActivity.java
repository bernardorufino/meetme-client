package br.com.bernardorufino.android.meetme.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import br.com.bernardorufino.android.meetme.R;

public class MainActivity extends BaseActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

    }

    public void joinGroupClick(View view) {
        Intent intent = new Intent(this, JoinGroupActivity.class);
        startActivity(intent);
    }

    public void newGroupClick(View view) {
        Intent intent = new Intent(this, NewGroupActivity.class);
        startActivity(intent);
    }


}
