package com.bitdance.giveortake;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by nora on 6/17/13.
 */
public class Item implements Serializable {
    private long id;
    private String name;
    private String description;

    private static final String JSON_ID = "id";
    private static final String JSON_NAME = "name";
    private static final String JSON_DESC = "description";

    // TODO: remove this -- for testing
    public Item(String name) {
        this.name = name;
    }

    public Item(JSONObject jsonObject) throws JSONException {
        id = jsonObject.getLong(JSON_ID);
        name = jsonObject.getString(JSON_NAME);
        description = jsonObject.getString(JSON_DESC);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return getName();
    }
}
