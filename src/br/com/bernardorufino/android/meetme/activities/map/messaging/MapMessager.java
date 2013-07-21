package br.com.bernardorufino.android.meetme.activities.map.messaging;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import br.com.bernardorufino.android.meetme.lib.messaging.MessageQueue;
import br.com.bernardorufino.android.meetme.lib.messaging.Messager;

public class MapMessager implements Messager<MapMessage> {

    private final TextView mTextView;
    private final ProgressBar mProgressBar;

    MapMessager(TextView textView, ProgressBar progressBar) {
        mTextView = textView;
        mProgressBar = progressBar;
    }

    @Override
    public void showMessage(MapMessage mapMessage) {
        mTextView.setText(mapMessage.text);
        mTextView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(mapMessage.loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void hideMessage() {
        mTextView.setText("");
        // Using INVISIBLE instead of GONE to preserve height of container
        mTextView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

}
