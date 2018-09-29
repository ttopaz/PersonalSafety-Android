package com.topaz.personalsafety.app;

/**
 * Created by Tony on 4/19/2014.
 */
import android.content.Context;
import android.location.Location;

import com.topaz.personalsafety.app.R;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Defines app-wide constants and utilities
 */
public final class LocationUtils {

    // Debugging tag for the application
    public static final String APPTAG = "LocationSample";

    // Name of shared preferences repository that stores persistent state
    public static final String SHARED_PREFERENCES =
            "com.example.android.location.SHARED_PREFERENCES";

    // Key for storing the "updates requested" flag in shared preferences
    public static final String KEY_UPDATES_REQUESTED =
            "com.example.android.location.KEY_UPDATES_REQUESTED";

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    public static final int UPDATE_INTERVAL_IN_SECONDS = 20;

    // A fast interval ceiling
    public static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;

    // Create an empty string for initializing strings
    public static final String EMPTY_STRING = new String();

    /**
     * Get the latitude and longitude from the Location object returned by
     * Location Services.
     *
     * @param currentLocation A Location object containing the current location
     * @return The latitude and longitude of the current location, or null if no
     * location is available.
     */
    public static String getLatLng(Context context, Location currentLocation) {
        // If the location is valid
        if (currentLocation != null) {

            // Return the latitude and longitude as strings
            return context.getString(
                    R.string.latitude_longitude,
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude());
        } else {

            // Otherwise, return the empty string
            return EMPTY_STRING;
        }
    }
}

class LocationSerializer implements JsonSerializer<Location>
{
    public JsonElement serialize(Location t, Type type,
                                 JsonSerializationContext jsc)
    {
        JsonObject jo = new JsonObject();
        jo.addProperty("mProvider", t.getProvider());
        jo.addProperty("mAccuracy", t.getAccuracy());
        jo.addProperty("mAltitude", t.getAltitude());
        jo.addProperty("mLatitude", t.getLatitude());
        jo.addProperty("mLongitude", t.getLongitude());
        jo.addProperty("mBearing", t.getBearing());
        jo.addProperty("mSpeed", t.getSpeed());
        jo.addProperty("mTime", t.getTime());
        jo.addProperty("mmElapsedRealtimeNanos", t.getElapsedRealtimeNanos());
        return jo;
    }

}

class LocationDeserializer implements JsonDeserializer<Location>
{
    public Location deserialize(JsonElement je, Type type,
                                JsonDeserializationContext jdc)
            throws JsonParseException
    {
        JsonObject jo = je.getAsJsonObject();
        Location l = new Location(jo.getAsJsonPrimitive("mProvider").getAsString());
        l.setAccuracy(jo.getAsJsonPrimitive("mAccuracy").getAsFloat());
        l.setAltitude(jo.getAsJsonPrimitive("mAltitude").getAsFloat());
        l.setLatitude(jo.getAsJsonPrimitive("mLatitude").getAsFloat());
        l.setLongitude(jo.getAsJsonPrimitive("mLongitude").getAsFloat());
        l.setBearing(jo.getAsJsonPrimitive("mBearing").getAsFloat());
        l.setSpeed(jo.getAsJsonPrimitive("mSpeed").getAsFloat());
        l.setTime(jo.getAsJsonPrimitive("mTime").getAsLong());
        l.setElapsedRealtimeNanos(jo.getAsJsonPrimitive("mmElapsedRealtimeNanos").getAsLong());
        return l;
    }
}

