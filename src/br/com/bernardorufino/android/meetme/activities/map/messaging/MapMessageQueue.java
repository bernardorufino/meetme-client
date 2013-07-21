package br.com.bernardorufino.android.meetme.activities.map.messaging;

import android.widget.ProgressBar;
import android.widget.TextView;
import br.com.bernardorufino.android.meetme.lib.messaging.MessageQueue;

// TODO: Composition instead of inheritance
public class MapMessageQueue extends MessageQueue<MapMessage> {

    public static final String DEFAULT_TAG = "default";

    public MapMessageQueue(TextView textView, ProgressBar progressBar) {
        super(new MapMessager(textView, progressBar));
    }

    public void addMessage(String tag, String text) {
        super.add(tag, new MapMessage(text, false));
    }

    public void addMessage(String text) {
        super.add(DEFAULT_TAG, new MapMessage(text, false));
    }

    public void addLoadingMessage(String tag, String text) {
        super.add(tag, new MapMessage(text, true));
    }

    public void addLoadingMessage(String text) {
        super.add(DEFAULT_TAG, new MapMessage(text, true));
    }

    public void hideMessage() {
        super.hideMessage(DEFAULT_TAG);
    }

}
