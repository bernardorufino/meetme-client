package br.com.bernardorufino.android.meetme.model;

import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class User {
    private int id;
    private final String name;
    private LatLng position;
    private Group group;

    public User(String name, LatLng position) {
        this.name = name;
        this.position = position;
    }

    /* package private */ static User fromJSON(JSONObject data) throws JSONException {
        LatLng position = new LatLng(data.getDouble("latitude"), data.getDouble("longitude"));
        User user = new User(data.getString("name"), position);
        user.id = data.getInt("id");
        return user;
    }

    /* package private */ int getID() {
        return id;
    }

    /* package private */ void setID(int id) {
        this.id = id;
    }

    public LatLng getPosition() {
        return position;
    }

    public void updatePosition(LatLng position) throws IOException {
        this.position = position;
        group.updateUserPosition(this);
    }

    public String getName() {
        return name;
    }

    public Group getGroup() {
        return group;
    }

    /* package private */ void setGroup(Group group) {
        this.group = group;
    }
}
