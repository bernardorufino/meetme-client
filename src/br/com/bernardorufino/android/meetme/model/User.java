package br.com.bernardorufino.android.meetme.model;

import br.com.bernardorufino.android.meetme.helper.Helper;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;

public class User implements Serializable {
    private static final int UNDEFINED_ID = -1;

    private int id = UNDEFINED_ID;
    private String name;
    // Using pair of doubles in order to allow serialization since LatLng is not serializable
    private double latitude;
    private double longitude;
    private Group group;

    public User(String name, LatLng position) {
        this.name = name;
        setPosition(position);
    }

    /* package private */ static User fromJSON(JSONObject data) throws JSONException {
        LatLng position = new LatLng(data.getDouble("latitude"), data.getDouble("longitude"));
        User user = new User(data.getString("name"), position);
        user.id = data.getInt("id");
        return user;
    }

    public int getID() {
        return id;
    }

    /* package private */ void setID(int id) {
        this.id = id;
    }

    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    private void setPosition(LatLng position) {
        this.latitude = position.latitude;
        this.longitude = position.longitude;
    }

    public void updatePosition(LatLng position) throws IOException {
        setPosition(position);
        group.updateUserPosition(this);
    }

    public String getName() {
        return name;
    }

    /* package private */ void setGroup(Group group) {
        this.group = group;
    }

    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("<User");
        if (id != UNDEFINED_ID) string.append(" id=" + id);
        if (name != null) string.append(" name=\"" + name + "\"");
        return string.append(" />").toString();
    }

    //TODO: Fix equals contract
    public boolean equals(Object object) {
        if (!(object instanceof User)) return false;
        User user = (User) object;
        boolean nullName = (name == null || user.name == null);
        boolean nullID = (id == UNDEFINED_ID || user.id == UNDEFINED_ID);
        return (nullName && nullID)
            || (nullID && name.equals(user.name))
            || (nullName && id == user.id)
            || (id == user.id && name.equals(user.name));
    }

}
