package br.com.bernardorufino.android.meetme.lib.messaging;

public interface Messager<T> {

    public void showMessage(T message);

    public void hideMessage();

}
