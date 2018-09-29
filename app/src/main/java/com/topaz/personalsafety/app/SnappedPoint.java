package com.topaz.personalsafety.app;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Tony on 2/19/2017.
 */
public class SnappedPoint {
    /**
     * {@code location} contains a latitude and longitude value representing the snapped location.
     */
    public LatLng Location;

    /**
     * {@code originalIndex} is an integer that indicates the corresponding value in the original
     * request. Each value in the request should map to a snapped value in the response. However, if
     * you've set interpolate=true, then it's possible that the response will contain more coordinates
     * than the request. Interpolated values will not have an originalIndex. These values are indexed
     * from 0, so a point with an originalIndex of 4 will be the snapped value of the 5th lat/lng
     * passed to the path parameter.
     *
     * <p>A point that was not on the original path, or when interpolate=false will have an
     * originalIndex of -1.
     */
    public int OriginalIndex = -1;

/*
    "location": {
        "latitude": -35.2784167,
                "longitude": 149.1294692
    },
            "originalIndex": 0,
            "placeId": "ChIJoR7CemhNFmsRQB9QbW7qABM"
*/

    /**
     * {@code placeId} is a unique identifier for a place. All placeIds returned by the Roads API will
     * correspond to road segments. The placeId can be passed to the speedLimit method to determine
     * the speed limit along that road segment.
     */
    public String PlaceId;

    public SnappedPoint(JSONObject json)
    {
        try {
            JSONObject location = json.getJSONObject("location");

            this.Location = new LatLng(location.getDouble("latitude"), location.getDouble("longitude"));
            if (json.has("originalIndex"))
                this.OriginalIndex = json.getInt("originalIndex");
            else
                this.OriginalIndex = -1;
            if (json.has("placeId"))
                this.PlaceId = json.getString("placeId");
        }
        catch (JSONException ex)
        {

        }
    }
}
