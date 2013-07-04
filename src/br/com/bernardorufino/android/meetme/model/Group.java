package br.com.bernardorufino.android.meetme.model;

import br.com.bernardorufino.android.meetme.helper.Helper;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class Group implements Serializable {

    public static class UnknownResponseException extends RuntimeException { /* Empty */ }
    public static class UserNotInGroupException extends RuntimeException { /* Empty */ }

    public static Group create(User user) throws IOException {
        try {
            LatLng p = user.getPosition();
            JSONObject response = GroupsAPI.create(user.getName(), p.latitude, p.longitude);
            String password = response.getJSONObject("group").getString("password");
            Group group = new Group(password);
            int userID = response.getJSONObject("user").getInt("id");
            user.setID(userID);
            user.setGroup(group);
            group.update();
            return group;
        } catch (JSONException lowError) {
            UnknownResponseException highError = new UnknownResponseException();
            highError.initCause(lowError);
            throw highError;
        }
    }

    public static Group join(String password, User user) throws IOException {
        try {
            LatLng p = user.getPosition();
            JSONObject response = GroupsAPI.join(password, user.getName(), p.latitude, p.longitude);
            Group group = new Group(password);
            int userID = response.getJSONObject("user").getInt("id");
            user.setID(userID);
            user.setGroup(group);
            group.update();
            return group;
        } catch (JSONException lowError) {
            UnknownResponseException highError = new UnknownResponseException();
            highError.initCause(lowError);
            throw highError;
        }
    }

    private final String password;
    private Collection<User> users;

    public Group(String password) {
        this.password = password;
        this.users = new ArrayList<>();
    }

    public void update() throws IOException {
        JSONObject data = GroupsAPI.retrieve(password);
        try {
            users.clear();
            JSONArray usersData = data.getJSONObject("group").getJSONArray("users");
            for (int i = 0, n = usersData.length(); i < n; i++) {
                JSONObject userData = usersData.getJSONObject(i);
                User user = User.fromJSON(userData);
                user.setGroup(this);
                users.add(user);
            }
        } catch (JSONException lowError) {
            UnknownResponseException highError = new UnknownResponseException();
            highError.initCause(lowError);
            throw highError;
        }
    }

    public User getUser(int id) throws IOException {
        update();
        for (User user : users) {
            if (user.getID() == id) return user;
        }
        return null;
    }

    public void updateUserPosition(User user) throws IOException {
        if (!users.contains(user)) { throw new UserNotInGroupException(); }
        LatLng p = user.getPosition();
        GroupsAPI.update(password, user.getID(), p.latitude, p.longitude);
    }

    public String getPassword() {
        return password;
    }

    public Collection<User> getUsers() {
        return users;
    }
}
