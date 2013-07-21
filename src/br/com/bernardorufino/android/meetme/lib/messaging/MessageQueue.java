package br.com.bernardorufino.android.meetme.lib.messaging;

import java.util.LinkedHashMap;
import java.util.Map;


// Handling user messages in a queue, when there is a message being displayed,
// it will wait until it disappears to show the new one, much like the following timeline
//
// Display command:  A B - - - - - C D - - - - - E - - - - F - - -
//    Hide command:  - - - A - B - - - - D - C - - - - E - - - - F
//       User sees:  A A A B B - - C C C C C - - E E E - - F F F -
//
// Note that if a message hides before having the chance to be displayed, it will never be displayed
// New messages of the same origin erases the old one and are put as a fresh message
public class MessageQueue<M> {

    private Messager<M> mMessager;
    private Map<String, M> mMessages = new LinkedHashMap<>();

    public MessageQueue(Messager<M> mMessager) {
        this.mMessager = mMessager;
    }

    private void updateMessage() {
        if (mMessages.size() > 0) {
            M message = mMessages.values().iterator().next();
            mMessager.showMessage(message);
        } else {
            mMessager.hideMessage();
        }
    }

    public void add(String tag, M message) {
        mMessages.remove(tag);
        mMessages.put(tag, message);
        if (mMessages.size() == 1) updateMessage();
    }

    public void hideMessage(String tag) {
        mMessages.remove(tag);
        updateMessage();
    }

}
