package com.topaz.personalsafety.app;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Tony on 4/20/2014.
 */
public class LocationTrail {
    private String _id;
    public String TicketId;
    public long Time;
    public Date Created;
    public double Latitude;
    public double Longitude;
    public double Altitude;
    public float Speed;
    public float Bearing;
    public float Accuracy;
    public String Provider;
    public long ElapsedNanos;

    public int AvgSpeedIndex;
    public double AvgSpeedTotal;
    public double TotalDistance;
    public long TotalSeconds;

    public LocationTrail()
    {

    }
    public LocationTrail(Location loc)
    {
        Calendar now = Calendar.getInstance();
        this.Created = now.getTime();
        this.Time = loc.getTime();
        this.Latitude = loc.getLatitude();
        this.Longitude = loc.getLongitude();
        this.Altitude = loc.getAltitude();
        this.Speed = loc.getSpeed();
        this.Bearing = loc.getBearing();
        this.Accuracy = loc.getAccuracy();
        this.Provider = loc.getProvider();
        this.ElapsedNanos = loc.getElapsedRealtimeNanos();
    }

    public LocationTrail(JSONObject json)
    {
        try {
            this._id = json.getString("_id");
            this.TicketId = json.getString("TicketId");
            this.Created = new Date(json.getString("Created"));
            this.Latitude = json.getDouble("Latitude");
            this.Longitude = json.getDouble("Longitude");
            this.Altitude = json.getDouble("Altitude");
            this.Time = json.getLong("Time");
            this.Speed = (float)json.getDouble("Speed");
            this.Bearing = (float)json.getDouble("Bearing");
            this.Accuracy = (float)json.getDouble("Accuracy");
            this.Provider = json.getString("Provider");
            if (json.has("ElapsedNanos"))
                this.ElapsedNanos = json.getLong("ElapsedNanos");
        }
        catch (JSONException ex)
        {

        }
    }
    public String getId()
    {
        return _id;
    }
    public double getMPH()
    {
        return this.Speed * 3.6 * 0.621371;
    }
    public void setId(String id)
    {
        _id = id;
    }
}
