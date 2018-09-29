package com.topaz.personalsafety.app;

import android.text.format.Time;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Tony on 4/20/2014.
 */
public class LocationTicket {
    private String _id;
    public String DeviceId;
    public Date Created;
    public Date Expires;
    public String UserId;
    public String UserName;
    public String Code;
    public Boolean Active;
    public int Trails;

    public LocationTicket()
    {
        Code = Helpers.Gen6DigitNumber();
    }

    public String getId()
    {
        return _id;
    }
    public void setId(String id)
    {
        _id = id;
    }

    public LocationTicket(JSONObject json)
    {
        try {
            this._id = json.getString("_id");
            this.UserId = json.getString("UserId");
            this.UserName = json.getString("UserName");
            this.DeviceId = json.getString("DeviceId");
            this.Created = new Date(json.getString("Created"));
            this.Expires = new Date(json.getString("Expires"));
            this.Active = json.getBoolean("Active");
            this.Code = json.getString("Code");
            this.Trails = json.getInt("Trails");
        }
        catch (JSONException ex)
        {

        }
    }
}

