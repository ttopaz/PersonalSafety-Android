package com.topaz.personalsafety.app;

import java.io.IOException;
import java.io.InputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.SyncStateContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.annotations.SerializedName;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends ActionBarActivity implements
        ActionBar.TabListener,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    // A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private GoogleApiClient mGoogleApiClient;

    // Handles to UI widgets
    private TextView mConnectionState;
    private Button mServiceButton;

    private Date mTrackingStarted = null;

    private TextView mStatSpeed;
    private TextView mStatAvgSpeed;
    private TextView mStatTime;
    private TextView mStatDistance;

    private TextView mSmallStatSpeed;
    private TextView mSmallStatAvgSpeed;
    private TextView mSmallStatTime;
    private TextView mSmallStatDistance;

    private TextView mLargeStatSpeed;
    private TextView mLargeStatAvgSpeed;
    private TextView mLargeStatTime;
    private TextView mLargeStatDistance;

    private Menu mMenu;

    private static Handler trackTimerHandler = new Handler();

    GoogleMap googleMap = null;
    View mapView = null;
    private static String recvToken = null;
    private static String deviceId = null;
    private static String deviceTicketId = null;
    private static String locationTicketId = null;
    private static Handler ticketsGetHandler = new Handler();
    private final static ArrayList<LocationTicketTarget> selectedTickets = new ArrayList<LocationTicketTarget>();
    private final static ArrayList<LocationTrail> viewTrails = new ArrayList<LocationTrail>();

    static final int TRAIL_TYPE_NONE = 0;
    static final int TRAIL_TYPE_ME = 1;
    static final int TRAIL_TYPE_FOLLOW = 2;
    static final int TRAIL_TYPE_HISTORY = 3;


    private final static FollowTicketData followData = new FollowTicketData();

/*    private static String followTicketId = null;
    private static String followTicketName = null;
    private static boolean followPlaybackMode = false;
    private static int followPlaybackIndex = 0;
    private static int followTicketType = 0;
    private final static ArrayList<LocationTrail> followTrails = new ArrayList<LocationTrail>();
*/

    private boolean includeMarkers = false;
    private final HashMap<Marker, String> mapMarkers = new HashMap<Marker, String>();
    private int trailRefreshPeriod;
    static final int PICK_CONTACT_REQUEST = 1;
    static final int PICK_TRAIL_REQUEST = 2;
    private int googleMapType = GoogleMap.MAP_TYPE_HYBRID;

    // Handle to SharedPreferences for this app
    SharedPreferences mPrefs;

    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;

    boolean mUpdatesRequested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//        actionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null)
        {
            googleMapType = savedInstanceState.getInt("MapType");
            deviceTicketId = savedInstanceState.getString("deviceTicketId");
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++)
        {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }


        //DataFragment fragment = (DataFragment)mSectionsPagerAdapter.getItem(0);

//        mLatLng = (TextView) fragment.getView().findViewById(R.id.lat_lng);
//        mAddress = (TextView) fragment.getView().findViewById(R.id.address);
//        mActivityIndicator = (ProgressBar) fragment.getView().findViewById(R.id.address_progress);
//        mConnectionState = (TextView) fragment.getView().findViewById(R.id.text_connection_state);
//        mConnectionStatus = (TextView) fragment.getView().findViewById(R.id.text_connection_status);

        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();

        /*
         * Set the update interval
         */
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        // Note that location updates are off until the user turns them on
        mUpdatesRequested = false;

        // Open Shared Preferences
        mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        // Get an editor
        mEditor = mPrefs.edit();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        LocalBroadcastManager.getInstance(this).registerReceiver(
                msgReceiver, new IntentFilter("gpsInfo"));

        if (mServiceButton != null)
        {
            if (isServiceRunnning())
            {
                mServiceButton.setText("Stop Tracking");
            }
            else
                mServiceButton.setText("Start Tracking");
        }

        Intent msgIntent = new Intent("msgInfo");
        msgIntent.putExtra("deviceTicketId", "");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(msgIntent);
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state)
    {
        super.onSaveInstanceState(state);
        state.putInt("MapType", googleMapType);
        state.putString("deviceTicketId", deviceTicketId);
    }
    @Override
    public void onRestoreInstanceState(Bundle state)
    {
        super.onRestoreInstanceState(state);
        googleMapType = state.getInt("MapType");
        deviceTicketId = state.getString("deviceTicketId");
    }

    public void updateSettings(Context context)
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        trailRefreshPeriod = Integer.parseInt(settings.getString("trailRefresh", "30000"));
    }

    private boolean isServiceRunnning()
    {
        ActivityManager manager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if ("com.topaz.personalsafety.app.TrackMeService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void startStopService(View v)
    {
        try
        {
            if (!isServiceRunnning())
            {
                startService(new Intent(getBaseContext(), TrackMeService.class));
            }
            else
            {
                stopService(new Intent(getBaseContext(), TrackMeService.class));
            }
/*            if (isServiceRunnning())
                mServiceButton.setText("Stop Tracking");
            else
                mServiceButton.setText("Start Tracking");*/
        }
        catch (Exception ex)
        {

        }
    }

    private void loadTickets()
    {
        RESTMgr.getInstance().getTargetActiveTickets(recvToken, Helpers.GetPhoneNumber(this), new RESTMgr.OnTaskCompleted()
        {
            @Override
            public void onTaskCompleted(Object result) {
                try
                {
                    Toast.makeText(getApplicationContext(), "Got Tickets", Toast.LENGTH_SHORT).show();
                }
                catch (Exception ex)
                {

                }
            }
        });
    }

    private void addTarget(String targetName, String targetNumber)
    {
        TrackMsgInfo info = new TrackMsgInfo();
        info.PhoneNumber = targetNumber;
        info.SendGPS = true;
        info.Message = "Here is my location";
        info.OneTime = false;
        info.Name = targetName;

        Intent msgIntent = new Intent("msgInfo");
        msgIntent.putExtra("start", new Gson().toJson(info));
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(msgIntent);
    }

    private void addTicket(String target)
    {
        if (recvToken == null)
            return;
        if (locationTicketId != null)
            return;
        LocationTicket ticket = new LocationTicket();
        Calendar now = Calendar.getInstance();
        ticket.Created = now.getTime();
        now.add(Calendar.HOUR,1);
        ticket.Expires = now.getTime();
        ticket.UserId = "Tony";
        ticket.DeviceId = getDeviceId();
//        ticket.Target = target;

        RESTMgr.getInstance().addTicket(recvToken, ticket, new RESTMgr.OnTaskCompleted()
        {
            @Override
            public void onTaskCompleted(Object result) {
                try {
                    JSONObject obj = (JSONObject)result;
                    TrackMsgInfo info = new TrackMsgInfo();
//                    info.TicketId = obj.getString("_id");
                    info.PhoneNumber = obj.getString("Target");
                    info.SendGPS = true;
                    info.Message = "Here is my location";

                    Intent msgIntent = new Intent("msgInfo");
                    msgIntent.putExtra("start", new Gson().toJson(info));
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(msgIntent);
//                    locationTicketId = ((JSONObject)result).getString("_id");
                }
                catch (Exception ex)
                {

                }
            }
        });
    }
    private void addTrail(Location loc)
    {
        if (recvToken == null)
            return;
        if (locationTicketId == null)
            return;
        LocationTrail trail = new LocationTrail(loc);
        trail.TicketId = locationTicketId;
        RESTMgr.getInstance().addTrail(recvToken, trail, new RESTMgr.OnTaskCompleted()
        {
            @Override
            public void onTaskCompleted(Object result)
            {
                try {

                }
                catch (Exception ex)
                {

                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);

        mMenu = menu;

        /*SubMenu sub = menu.addSubMenu(2, 2, 1, "Map Display");

        sub.add(2, 3, 1, "Normal");
        sub.add(2, 4, 2, "Satellite");
        sub.add(2, 5, 3, "Terrain");
        sub.add(2, 6, 4, "Hybrid");

        menu.findItem(2).setVisible(true);

        sub.setGroupCheckable(2, true, true);

        switch (googleMapType)
        {
            case GoogleMap.MAP_TYPE_NORMAL:
                sub.findItem(3).setChecked(true);
                break;
            case GoogleMap.MAP_TYPE_SATELLITE:
                sub.findItem(4).setChecked(true);
                break;
            case GoogleMap.MAP_TYPE_TERRAIN:
                sub.findItem(5).setChecked(true);
                break;
            case GoogleMap.MAP_TYPE_HYBRID:
                sub.findItem(6).setChecked(true);
                break;
        }
*/
/*        menu.add(1, 7, 3, "Track Me");
        menu.add(1, 8, 4, "History");
        menu.add(1, 9, 5, "Follow");
       // if (followTicketType == TRAIL_TYPE_HISTORY)
        {
            menu.add(1, 10, 6, "Details");
         //   if (followPlaybackMode)
            {
           //     if (followPlaybackIndex > 0)
                {
                    menu.add(1, 12, 7, "Stop Playback");
                }
             //   else
                {
                    menu.add(1, 13, 7, "Start Playback");
                }
            }
        }
        menu.add(1, 11, 9, "Markers");

        menu.findItem(11).setCheckable(true);
        menu.findItem(11).setChecked(includeMarkers);

        menu.add(1, 1, 10, "Settings");
*/

        boolean serviceOn = isServiceRunnning();

        menu.findItem(R.id.action_service).setChecked(serviceOn);

        menu.findItem(R.id.action_send).setEnabled(serviceOn);
        menu.findItem(R.id.action_send).getIcon().setAlpha(serviceOn ? 255 : 128);

        if (menu.findItem(R.id.action_service).isChecked())
            menu.findItem(R.id.action_service).setIcon(R.drawable.ic_action_location_found);
        else
            menu.findItem(R.id.action_service).setIcon(R.drawable.ic_action_location_off);

        menu.findItem(R.id.action_markers).setChecked(includeMarkers);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.action_details).setVisible(followData.followTicketType == TRAIL_TYPE_HISTORY);
        menu.findItem(R.id.action_stop).setVisible(followData.followTicketType == TRAIL_TYPE_HISTORY && followData.followPlaybackMode);
        menu.findItem(R.id.action_play).setVisible(followData.followTicketType == TRAIL_TYPE_HISTORY && !followData.followPlaybackMode);

        boolean serviceOn = isServiceRunnning();
        menu.findItem(R.id.action_send).setEnabled(serviceOn);
        menu.findItem(R.id.action_send).getIcon().setAlpha(serviceOn ? 255 : 128);
        menu.findItem(R.id.action_me).setEnabled(serviceOn);
//        menu.findItem(2).setVisible( mViewPager.getCurrentItem() == 1);

/*        if (hasMaps)
        {
            SubMenu sub = menu.findItem(2).getSubMenu();
            sub.findItem(3).setChecked(googleMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL);
            sub.findItem(4).setChecked(googleMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE);
            sub.findItem(5).setChecked(googleMap.getMapType() == GoogleMap.MAP_TYPE_TERRAIN);
            sub.findItem(6).setChecked(googleMap.getMapType() == GoogleMap.MAP_TYPE_HYBRID);
        }*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_map_display_normal:
                item.setChecked(true);
                googleMapType = GoogleMap.MAP_TYPE_NORMAL;
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.action_map_display_satellite:
                item.setChecked(true);
                googleMapType = GoogleMap.MAP_TYPE_SATELLITE;
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.action_map_display_terrain:
                item.setChecked(true);
                googleMapType = GoogleMap.MAP_TYPE_TERRAIN;
                googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            case R.id.action_map_display_hybrid:
                item.setChecked(true);
                googleMapType = GoogleMap.MAP_TYPE_HYBRID;
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.action_me:
                selectMe(item.getActionView());
                return true;
            case R.id.action_history:
                selectHistory(item.getActionView());
                return true;
            case R.id.action_follow:
                selectTickets(item.getActionView());
                return true;
            case R.id.action_details:
                showTrails(item.getActionView());
                return true;
            case R.id.action_markers:
                includeMarkers ^= true;
                if (followData.followPlaybackMode)
                    refreshPath(false, followData.followPlaybackIndex);
                else
                    refreshPath(false, followData.followTrails.size() - 1);
                item.setChecked(includeMarkers);
                return true;
            case R.id.action_stop:
                stopHistoryPlayback();
                return true;
            case R.id.action_play:
                startHistoryPlayback();
                return true;
            case R.id.action_send:
                onSendLocation(item.getActionView());
                return true;
            case R.id.action_service:
                startStopService(item.getActionView());
                item.setChecked(isServiceRunnning());
                if (item.isChecked())
                    item.setIcon(R.drawable.ic_action_location_found);
                else
                    item.setIcon(R.drawable.ic_action_location_off);
                mMenu.findItem(R.id.action_send).getIcon().setAlpha(item.isChecked() ? 255 : 128);
                return true;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    /*
        * Called when the Activity is no longer visible at all.
        * Stop updates and disconnect.
        */
    @Override
    public void onStop()
    {
        // If the client is connected
        if (mGoogleApiClient.isConnected())
        {
            stopPeriodicUpdates();
        }

        // After disconnect() is called, the client is considered "dead".
        mGoogleApiClient.disconnect();

        super.onStop();
    }
    /*
     * Called when the Activity is going into the background.
     * Parts of the UI may be visible, but the Activity is inactive.
     */
    @Override
    public void onPause()
    {
        // Save the current setting for updates
        mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, mUpdatesRequested);

        mEditor.putInt("MapType", googleMapType);

        mEditor.commit();

        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(msgReceiver);
    }

    /*
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @Override
    public void onStart()
    {
        super.onStart();

        /*
         * Connect the client. Don't re-start any requests here;
         * instead, wait for onResume()
         */
        mGoogleApiClient.connect();
    }
    /*
     * Called when the system detects that this Activity is now visible.
     */
    @Override
    public void onResume()
    {
        super.onResume();

        updateSettings(this);
        RESTMgr.getInstance().updateSettings(this);

        if (recvToken == null)
        {
            String recvId = Helpers.GetRecvDeviceId(this);
            try
            {
                RESTMgr.getInstance().login(recvId, recvId, new RESTMgr.OnTaskCompleted()
                {
                    @Override
                    public void onTaskCompleted(Object result)
                    {
                        try
                        {
                            recvToken = ((JSONObject) result).getString("token");
//                            Toast.makeText(getApplicationContext(), String.format("Token : %s", recvToken), Toast.LENGTH_SHORT).show();
                        }
                        catch (JSONException ex)
                        {

                        }
                    }
                });
            }
            catch (Exception ex)
            {

            }
        }

/*        String authToken = mPrefs.getString("authToken", "");
        if (authToken == null || authToken.length() == 0)
        {
            Intent intent = new Intent(this, LoginRegister.class);
            startActivity(intent);
        }
*/
/*
        if (mServiceButton != null)
        {
            if (isServiceRunnning())
                mServiceButton.setText("Stop Tracking");
            else
                mServiceButton.setText("Start Tracking");
        }*/
        // If the app already has a setting for getting location updates, get it
        if (mPrefs.contains(LocationUtils.KEY_UPDATES_REQUESTED))
        {
            mUpdatesRequested = mPrefs.getBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
            googleMapType = mPrefs.getInt("MapType", GoogleMap.MAP_TYPE_HYBRID);
            // Otherwise, turn off location updates until requested
        }
        else
        {
            mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
            mEditor.commit();
        }

        if (mapView != null)
        {
/*            ToggleButton button = (ToggleButton)mapView.findViewById(R.id.markers);
            if (button != null)
                button.setChecked(includeMarkers);*/
        }
        ticketsGetHandler.removeCallbacks(ticketsTimer);
        if (followData.followTrails.size() > 0)
        {
            ticketsGetHandler.postDelayed(ticketsTimer, 1000);
            refreshPath(false, followData.followPlaybackMode ? followData.followPlaybackIndex : followData.followTrails.size() - 1);
/*            if (selectedTickets.size() == 1)
            {
                followTicket = selectedTickets.get(0);
                locateMarker(followTicket);
            }*/
        }
    }

    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Choose what to do based on the request code
        switch (requestCode)
        {
            case PICK_CONTACT_REQUEST:
                if (resultCode == Activity.RESULT_OK)
                {
                    // Get the URI that points to the selected contact
                    Uri contactUri = intent.getData();

                    String [] vals = new String[2];
                    Helpers.GetContactInfo(this, contactUri, vals);

                    String number = vals[0];
                    String name = vals[1];
                    addTarget(name, Helpers.CleanPhoneNumber(number));
                }
            break;
            case PICK_TRAIL_REQUEST:
                if (resultCode == Activity.RESULT_OK)
                {
                    int trailIndex = intent.getIntExtra("trailIndex", 0);
                    if (trailIndex < followData.followTrails.size())
                    {
                        LatLng position = new LatLng(followData.followTrails.get(trailIndex).Latitude,
                                followData.followTrails.get(trailIndex).Longitude);
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 17);
                        googleMap.animateCamera(cameraUpdate);
                        if (followData.followTicketType == TRAIL_TYPE_HISTORY)
                        {
                            followData.followPlaybackIndex = trailIndex;
                        }
                    }
                }
                break;

            // If the request code matches the code sent in onConnectionFailed
            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // Log the result
                        Log.d(LocationUtils.APPTAG, getString(R.string.resolved));

                        // Display the result
//                        mConnectionState.setText(R.string.connected);
//                        mConnectionStatus.setText(R.string.resolved);
                        break;

                    // If any other result was returned by Google Play services
                    default:
                        // Log the result
                        Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));

                        // Display the result
                        //mConnectionState.setText(R.string.disconnected);
  //                      mConnectionStatus.setText(R.string.no_resolution);

                        break;
                }

                // If any other request code was received
            default:
                // Report that this Activity received an unknown requestCode
                Log.d(LocationUtils.APPTAG,
                        getString(R.string.unknown_activity_request_code, requestCode));

                break;
        }
    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(LocationUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
            }
            return false;
        }
    }

    /**
     * Invoked by the "Get Location" button.
     *
     * Calls getLastLocation() to get the current location
     *
     * @param v The view object associated with this method, in this case a Button.
     */
    public void getLocation(View v) {

        // If Google Play Services is available
        if (servicesConnected()) {
        }
    }

    public void onSendLocation(View v)
    {
        if (isServiceRunnning())
        {
            Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
            pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
            startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
        }
    }

    /**
     * Invoked by the "Get Address" button.
     * Get the address of the current location, using reverse geocoding. This only works if
     * a geocoding service is available.
     *
     * @param v The view object associated with this method, in this case a Button.
     */
    // For Eclipse with ADT, suppress warnings about Geocoder.isPresent()
    @SuppressLint("NewApi")
    public void getAddress(View v) {

        // In Gingerbread and later, use Geocoder.isPresent() to see if a geocoder is available.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && !Geocoder.isPresent()) {
            // No geocoder is present. Issue an error message
            Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
            return;
        }

        if (servicesConnected()) {

            Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            // Turn the indefinite activity indicator on
            //mActivityIndicator.setVisibility(View.VISIBLE);

            // Start the background task
            (new MainActivity.GetAddressTask(this)).execute(currentLocation);
        }
    }


    /**
     * Invoked by the "Start Updates" button
     * Sends a request to start location updates
     *
     * @param v The view object associated with this method, in this case a Button.
     */
    public void startUpdates(View v) {
        mUpdatesRequested = true;

        if (servicesConnected()) {
            startPeriodicUpdates();
        }
    }

    /**
     * Invoked by the "Stop Updates" button
     * Sends a request to remove location updates
     * request them.
     *
     * @param v The view object associated with this method, in this case a Button.
     */
    public void stopUpdates(View v) {
        mUpdatesRequested = false;

        if (recvToken != null && locationTicketId != null)
            RESTMgr.getInstance().deleteTicket(recvToken, locationTicketId, null);
        locationTicketId = null;
        if (servicesConnected()) {
            stopPeriodicUpdates();
        }
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
//        if (mConnectionStatus != null)
  //          mConnectionStatus.setText(R.string.connected);

        if (mUpdatesRequested) {
            startPeriodicUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
/*    @Override
    public void onDisconnected() {
//        mConnectionStatus.setText(R.string.disconnected);
    }
*/
    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /**
     * Report location updates to the UI.
     *
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location location) {

        // Report to the UI that the location was updated
    //    mConnectionStatus.setText(R.string.location_updated);

        // In the UI, set the latitude and longitude to the value received
//        mLatLng.setText(LocationUtils.getLatLng(this, location));

/*        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Location.class, new LocationDeserializer());
        gsonBuilder.registerTypeAdapter(Location.class, new LocationSerializer());
        Gson gson = gsonBuilder.create();
        String json = gson.toJson(location);
*/
        addTrail(location);

//        Toast.makeText(this, json, Toast.LENGTH_SHORT).show();

//        drawMarker(location);
    }

    private void removeTicketMarkers(String ticketId) {
        final HashSet<Marker> removeMarkers = new HashSet<Marker>();
        for(Marker marker: mapMarkers.keySet())
        {
            try {
                if (mapMarkers.get(marker).equals(ticketId))
                    removeMarkers.add(marker);
            }
            catch (Exception ex)
            {

            }
        }
        for(Marker marker: removeMarkers)
        {
            mapMarkers.remove(marker);
            marker.remove();
        }
    }

    private void locateMarker(String trailId)
    {
        if (googleMap == null)
            return;

        for(Marker marker: mapMarkers.keySet())
        {
            try {
                if (mapMarkers.get(marker).equals(trailId)) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17);
                    googleMap.animateCamera(cameraUpdate);
                    break;
                }
            }
            catch (Exception ex)
            {

            }
        }
    }

    private Bitmap createTrailBitmap(LocationTrail trail, int count)
    {
        Bitmap bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

        Paint fill = new Paint();
        fill.setColor(Color.RED);

        Canvas canvas = new Canvas(bmp);
        canvas.drawCircle(50, 50, 50, fill);

        String numText = String.format("#%d", count);
        Paint textFill = new Paint();
        textFill.setTextSize(35);
        textFill.setFakeBoldText(true);
        textFill.setColor(Color.BLACK);
        float wd = textFill.measureText(numText, 0, numText.length());
        Rect bounds = new Rect();
        textFill.getTextBounds(numText, 0, numText.length(), bounds);
        canvas.drawText(numText, (100 - wd)/2, 40, textFill);

        textFill.setColor(Color.WHITE);
        String speedText = String.format("%.02f", trail.getMPH());
        wd = textFill.measureText(speedText, 0, speedText.length());

        canvas.drawText(speedText, (100 - wd)/2, 40 + bounds.height() + 8, textFill);

        return bmp;
    }

    private void refreshPath(boolean autoZoom, int toIndex)
    {
        if (googleMap == null)
            return;
        googleMap.clear();
        LocationTrail lastTrail = null;

        PolylineOptions lineOptions = new PolylineOptions().width(16).color(Color.BLUE).geodesic(true);


        int count = 1;
        int disp = 1;
        mapMarkers.clear();
        viewTrails.clear();

        for(int index = 0; index <= toIndex; index++)
        {
            LocationTrail trail = followData.followTrails.get(index);
//            if (count % 4 == 0)
            {
                if (count == 1 || trail.Accuracy < 20)
                {
                    LatLng position = new LatLng(trail.Latitude, trail.Longitude);
                    lineOptions.add(position);

//                    if (disp % 5 == 0)
                    {
                        if (includeMarkers)
                        {
                            Bitmap bmp = createTrailBitmap(trail, count);
                            drawMarker(trail, String.format("#%d", count), bmp);
                        }
                        viewTrails.add(trail);
                        count++;
                    }
                    disp++;
                }
            }
        }
        googleMap.addPolyline(lineOptions);

        if (followData.followTrails.size() > 0)
        {
            LocationTrail markTrail = followData.followPlaybackMode ? followData.followTrails.get(followData.followPlaybackIndex)
                    : followData.followTrails.get(followData.followTrails.size() - 1);

            Marker marker = drawMarker(markTrail, "Latest Location", null);
            if (autoZoom)
            {
                CameraPosition.Builder builder = new CameraPosition.Builder().target(marker.getPosition()).bearing(markTrail.Bearing);
                float zoom = googleMap.getCameraPosition().zoom;
                if (zoom < 14f)
                    zoom = 17f;
                builder = builder.zoom(zoom);

                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(builder.build());
                googleMap.animateCamera(cameraUpdate);
            }
            if (!followData.followPlaybackMode)
                marker.showInfoWindow();
            if (viewTrails.size() == 0)
                viewTrails.add(markTrail);
        }
    }

    private void renderTicketTrailStats(LocationTrail trail, boolean renderTime)
    {
        if (renderTime && mStatTime != null)
        {
            long seconds = TimeUnit.SECONDS.toSeconds(trail.TotalSeconds) % 60;
            long minutes = TimeUnit.SECONDS.toMinutes(trail.TotalSeconds) % 60;
            long hours = TimeUnit.SECONDS.toHours(trail.TotalSeconds);

            mStatTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

            if (mSmallStatTime != null)
                mSmallStatTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            if (mLargeStatTime != null)
                mLargeStatTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        }
        if (mStatSpeed != null)
        {
            String speedText = String.format("%.02f", trail.getMPH());
            mStatSpeed.setText(speedText);

            if (mSmallStatSpeed != null)
                mSmallStatSpeed.setText(speedText);
            if (mLargeStatSpeed != null)
                mLargeStatSpeed.setText(speedText);
        }
        if (mStatDistance != null)
        {
            String distText = String.format("%.02f", (0.621371 * trail.TotalDistance) / 1000);
            mStatDistance.setText(distText);

            if (mSmallStatDistance != null)
                mSmallStatDistance.setText(distText);
            if (mLargeStatDistance != null)
                mLargeStatDistance.setText(distText);
        }
        if (trail.AvgSpeedIndex != 0 && mStatAvgSpeed != null)
        {
            String speedText = String.format("%.02f", (trail.AvgSpeedTotal * 3.6 * 0.621371) / (double)trail.AvgSpeedIndex);
            mStatAvgSpeed.setText(speedText);

            if (mSmallStatAvgSpeed != null)
                mSmallStatAvgSpeed.setText(speedText);
            if (mLargeStatAvgSpeed != null)
                mLargeStatAvgSpeed.setText(speedText);
        }
    }

    private void setTicketTrailStats(LocationTrail trail, boolean renderStats, boolean renderTime)
    {
        int index = followData.followTrails.indexOf(trail);

        double totalSpeed = index == 0 ? 0.0 : followData.followTrails.get(index - 1).AvgSpeedTotal;
        double totalDistance = index == 0 ? 0.0 : followData.followTrails.get(index - 1).TotalDistance;
        int followIndex = index == 0 ? 0: followData.followTrails.get(index - 1).AvgSpeedIndex;
        LocationTrail lastTrail = index == 0 ? null : followData.followTrails.get(index - 1);

        if (lastTrail != null)
        {
            final float[] results = new float[5];
            results[0] = 0;
            Location.distanceBetween(
                    lastTrail.Latitude, lastTrail.Longitude,
                    trail.Latitude, trail.Longitude, results);
            float distance = results[0];

            totalDistance += distance;

            long elapsedTime = trail.ElapsedNanos - lastTrail.ElapsedNanos;
            if (elapsedTime > 0)
            {
                float seconds = (float) elapsedTime / 1000000000.0f;
                if (seconds > 0.f)
                    trail.Speed = (float)distance/seconds;
            }
        }

        if (trail.Speed > 2.0)
        {
            totalSpeed += trail.Speed;
            followIndex++;
        }
        trail.AvgSpeedIndex = followIndex;
        trail.TotalDistance = totalDistance;
        trail.AvgSpeedTotal = totalSpeed;
        if (trail.ElapsedNanos > 0)
        {
            trail.TotalSeconds = (trail.ElapsedNanos - followData.followTrails.get(0).ElapsedNanos) / 1000000000;
        }
        else
        {
            long diffInMs = trail.Created.getTime() - followData.followTrails.get(0).Created.getTime();
            trail.TotalSeconds = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
        }

        if (renderStats)
            renderTicketTrailStats(trail, renderTime);
    }

    private void renderTicketTrails(boolean autoZoom, boolean renderStats, boolean renderTime)
    {
        for (LocationTrail trail: followData.followTrails)
        {
            setTicketTrailStats(trail, renderStats, renderTime);
        }
        refreshPath(autoZoom, followData.followTrails.size() - 1);
    }

    private void loadTicketTrailsAndRender(final boolean autoZoom)
    {
        loadTicketTrails(new OnAsyncCompleted()
        {
            @Override
            public void OnAsyncCompleted(Object result)
            {
                renderTicketTrails(autoZoom, true, true);
            }
        });
    }

    private void loadTicketTrails(OnAsyncCompleted callback)
    {
        if (followData.followTicketId == null)
            return;

        final OnAsyncCompleted _callback = callback;

        if (followData.followTrails.size() > 0 && followData.followTrails.get(0).TicketId.compareTo(followData.followTicketId) != 0)
        {
            followData.followTrails.clear();
        }

        String fromId = followData.followTrails.size() == 0 ? null : followData.followTrails.get(followData.followTrails.size() - 1).getId();

        RESTMgr.getInstance().getTicketTrails(recvToken, followData.followTicketId, fromId, new RESTMgr.OnTaskCompleted()
        {
            @Override
            public void onTaskCompleted(Object result)
            {
                try
                {
                    JSONArray trailArray = (JSONArray) result;
                    if (trailArray.length() > 0)
                    {
                        for (int i = 0; i < trailArray.length(); i++)
                        {
                            JSONObject trailObj = (JSONObject)trailArray.get(i);
                            LocationTrail trail = new LocationTrail(trailObj);

                            followData.followTrails.add(trail);
                        }
                    }
                    if (_callback != null)
                        _callback.OnAsyncCompleted(null);
                }
                catch (Exception ex)
                {

                }
            }
        });
    }

    private void updateMapPath(final boolean autoZoom, boolean activeOnly)
    {
        if (followData.followTicketId == null)
            return;

        if (!activeOnly)
        {
            followData.followTrails.clear();
            loadTicketTrailsAndRender(autoZoom);
            return;
        }

        final String ticketId = followData.followTicketId;

        followData.followTicketId = null;

        RESTMgr.getInstance().getTicket(recvToken, ticketId, new RESTMgr.OnTaskCompleted()
        {
            @Override
            public void onTaskCompleted(Object result)
            {
                try
                {
                    JSONObject ticketObject = (JSONObject) result;
                    LocationTicket ticket = new LocationTicket(ticketObject);
                    if (ticket.Active)
                    {
                        followData.followPlaybackMode = false;
                        followData.followTicketId = ticket.getId();
                        loadTicketTrailsAndRender(autoZoom);
                    }
                    else
                    {
                        followData.followTicketId = null;
                        trackTimerHandler.removeCallbacks(trackingTimeTimer);
                    }
                }
                catch (Exception ex)
                {

                }
            }
        });
    }

    private Marker drawMarker(LocationTrail location, String title, Bitmap bmp)
    {
        if (googleMap == null)
            return null;

        LatLng currentPosition = new LatLng(location.Latitude,
                location.Longitude);

        java.text.DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String locationDate = location.Created.toString();

        Marker marker = googleMap.addMarker(new MarkerOptions().position(currentPosition).anchor(0.5f, 0.5f).snippet(locationDate));
        if (bmp != null)
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
        else
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow));
       //     marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        if (title.length() > 0)
            marker.setTitle(title);

        marker.setRotation(location.Bearing);

        marker.setFlat(true);

        mapMarkers.put(marker, location.getId());

        return marker;
    }
    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {

        if (mGoogleApiClient != null)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            locationTicketId = null;
            addTicket(null);
        }
//        if (mConnectionState != null)
  //          mConnectionState.setText(R.string.location_requested);
    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates()
    {
        if (mGoogleApiClient != null)
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        if (mConnectionState != null)
            mConnectionState.setText(R.string.location_updates_stopped);
        locationTicketId = null;
    }

    public float getTicketColor(LocationTicketTarget ticket)
    {
        float [] colors = {
                BitmapDescriptorFactory.HUE_RED,
                BitmapDescriptorFactory.HUE_ORANGE,
                BitmapDescriptorFactory.HUE_YELLOW,
                BitmapDescriptorFactory.HUE_GREEN,
                BitmapDescriptorFactory.HUE_CYAN,
                BitmapDescriptorFactory.HUE_AZURE,
                BitmapDescriptorFactory.HUE_BLUE,
                BitmapDescriptorFactory.HUE_VIOLET,
                BitmapDescriptorFactory.HUE_MAGENTA,
                BitmapDescriptorFactory.HUE_ROSE,
        };
        return colors[selectedTickets.indexOf(ticket) % colors.length];
    }

    public void findTicket(View v)
    {
        showTicketZoom();
    }

    private void showTicketZoom()
    {
        final ArrayList<CharSequence> ticketNames = new ArrayList<CharSequence>();

        boolean[] checkedTickets = new boolean[ticketNames.size()];

        for(LocationTicketTarget ticket : selectedTickets) {
            ticketNames.add(ticket.UserName + " / " + ticket.Created.toString());
        }

        DialogInterface.OnClickListener ticketsDialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //followTicket = selectedTickets.get(which);
                //locateMarker(followTicket);
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Follow Track");
        builder.setItems(ticketNames.toArray(new CharSequence[ticketNames.size()]), ticketsDialogListener);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showTrails(View v)
    {
        Intent intent = new Intent(this, TrailsActivity.class);
        Bundle bundle = new Bundle();
        Gson gson = new GsonBuilder().serializeNulls().create();
        bundle.putString("trails", gson.toJson(followData.followTrails));
        intent.putExtras(bundle);
        startActivityForResult(intent, PICK_TRAIL_REQUEST);
    }

    public void showHideMarkers(View v)
    {
        ToggleButton button = (ToggleButton)v;
        if (button.isChecked())
            includeMarkers = true;
        else
            includeMarkers = false;
        button.setChecked(includeMarkers);

        if (followData.followPlaybackMode)
            refreshPath(false, followData.followPlaybackIndex);
        else
            refreshPath(false, followData.followTrails.size() - 1);
    }
    public void showSpeedGraph(View v)
    {

    }
    public void selectTickets(View v)
    {
        showTicketsSelection();
    }
    public void selectMe(View v)
    {
        if (deviceTicketId != null)
        {
            followData.followTicketType = TRAIL_TYPE_ME;
            followData.followPlaybackMode = false;
            followData.followTicketId = deviceTicketId;
            followData.followTicketName = "Me";
            if (mapView != null)
            {
                TextView mapTitle = (TextView)mapView.findViewById(R.id.mapTitle);
                mapTitle.setText(followData.followTicketName);
            }
            ticketsGetHandler.removeCallbacks(ticketsTimer);
            ticketsGetHandler.postDelayed(ticketsTimer, 1000);
            mTrackingStarted = Calendar.getInstance().getTime();
            trackTimerHandler.removeCallbacks(trackingTimeTimer);
            //trackTimerHandler.postDelayed(trackingTimeTimer, 1000);
            updateMapPath(true, true);
        }
    }
    public void selectHistory(View v)
    {
        showHistorySelection();
    }

/*    public void myTickets(View v)
    {
        showMyTicketsSelection();
    }
*/
    private void showOtherTicketsSelection()
    {
        if (recvToken == null)
            return;
        String [] vals = new String[2];
        Uri dataUri = Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
        Helpers.GetContactInfo(this, dataUri, vals);
        String phoneNumber = Helpers.CleanPhoneNumber(vals[0]);
        RESTMgr.getInstance().getTargetActiveTickets(recvToken, phoneNumber, new RESTMgr.OnTaskCompleted()
        {
            @Override
            public void onTaskCompleted(Object result)
            {
                try
                {
                    final ArrayList<CharSequence> ticketNames = new ArrayList<CharSequence>();
                    final ArrayList<LocationTicketTarget> dataTickets = new ArrayList<LocationTicketTarget>();
                    final ArrayList<LocationTicketTarget> removedTickets = new ArrayList<LocationTicketTarget>();
                    final ArrayList<LocationTicketTarget> newTickets = new ArrayList<LocationTicketTarget>();
                    JSONArray ticketArray = (JSONArray) result;
                    for (int i = 0; i < ticketArray.length(); i++)
                    {
                        JSONObject tickObj = (JSONObject) ticketArray.get(i);
                        LocationTicketTarget ticket = new LocationTicketTarget(tickObj);
                        dataTickets.add(ticket);
                        ticketNames.add(ticket.UserName + " / " + ticket.Created.toString());
                    }

                    boolean[] checkedTickets = new boolean[ticketNames.size()];

                    for (int i = 0; i < dataTickets.size(); i++)
                    {
                        checkedTickets[i] = false;
                        for (LocationTicketTarget ticket : selectedTickets)
                        {
                            if (ticket.getId().equals(dataTickets.get(i).getId()))
                            {
                                checkedTickets[i] = true;
                                break;
                            }
                        }
                    }
                    for (int i = selectedTickets.size() - 1; i >= 0; i--)
                    {
                        LocationTicketTarget ticket = selectedTickets.get(i);
                        boolean hasTicket = false;
                        for (LocationTicketTarget dataTicket : dataTickets)
                        {
                            if (dataTicket.getId().equals(ticket.getId()))
                            {
                                hasTicket = true;
                                break;
                            }
                        }
                        if (!hasTicket)
                        {
                            removeTicketMarkers(ticket.getId());
                            selectedTickets.remove(i);
                        }
                    }

                    DialogInterface.OnMultiChoiceClickListener ticketsDialogListener = new DialogInterface.OnMultiChoiceClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked)
                        {
                            if (isChecked)
                                selectedTickets.add(dataTickets.get(which));
                            else
                            {
                                removedTickets.add(dataTickets.get(which));
                                for (LocationTicket ticket : selectedTickets)
                                {
                                    if (ticket.getId().equals(dataTickets.get(which).getId()))
                                    {
                                        selectedTickets.remove(ticket);
                                        break;
                                    }
                                }
                            }

                            //                            onChangeSelectedColours();
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Select Tracks");
                    builder.setMultiChoiceItems(ticketNames.toArray(new CharSequence[ticketNames.size()]), checkedTickets, ticketsDialogListener);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            for (LocationTicket ticket : removedTickets)
                                removeTicketMarkers(ticket.getId());
/*                            ticketsGetHandler.removeCallbacks(ticketsTimer);
                            followTicket = null;
                            if (selectedTickets.size() > 0)
                            {
                                ticketsGetHandler.postDelayed(ticketsTimer, 1000);
                                if (selectedTickets.size() == 1)
                                {
                                    followTicket = selectedTickets.get(0);
                                    locateMarker(followTicket);
                                }
                            }*/
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    AlertDialog dialog = builder.create();
/*                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
                    {
                        @Override
                        public void onDismiss(final DialogInterface dialog)
                        {
                            for (LocationTicket ticket : removedTickets)
                                removeTicketMarkers(ticket.getId());
                            ticketsGetHandler.removeCallbacks(ticketsTimer);
                            if (selectedTickets.size() > 0)
                            {
                                ticketsGetHandler.postDelayed(ticketsTimer, 1000);
                                if (selectedTickets.size() == 1)
                                    locateMarker(selectedTickets.get(0));
                            }
                        }
                    });*/
                    dialog.show();
                }
                catch (JSONException ex)
                {

                }
            }
        });
    }

    private void stopHistoryPlayback()
    {
        ticketsGetHandler.removeCallbacks(ticketsTimer);
        followData.followPlaybackMode = false;
        updateMapPath(false, false);
    }

    private void startHistoryPlayback()
    {
        followData.followTicketType = TRAIL_TYPE_HISTORY;
        followData.followPlaybackMode = false;
        if (mapView != null)
        {
            TextView mapTitle = (TextView) mapView.findViewById(R.id.mapTitle);
            mapTitle.setText(followData.followTicketName);
        }

        ticketsGetHandler.removeCallbacks(ticketsTimer);

        if (followData.followTrails.size() > 0)
        {
            followData.followTicketId = followData.followTrails.get(0).TicketId;
            followData.followPlaybackMode = true;
            ticketsGetHandler.postDelayed(ticketsTimer, 1000);
        }
        else
        {
            loadTicketTrails(new OnAsyncCompleted()
            {
                @Override
                public void OnAsyncCompleted(Object result)
                {
                    followData.followPlaybackMode = true;
                    ticketsGetHandler.postDelayed(ticketsTimer, 1000);
                }
            });
        }
    }

    private void showHistorySelection()
    {
        if (recvToken == null)
            return;
        String [] vals = new String[2];
        Uri dataUri = Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
        Helpers.GetContactInfo(this, dataUri, vals);
        String phoneNumber = Helpers.CleanPhoneNumber(vals[0]);
        RESTMgr.getInstance().getUserTickets(recvToken, phoneNumber, new RESTMgr.OnTaskCompleted()
        {
            @Override
            public void onTaskCompleted(Object result)
            {
                final ArrayList<String> ticketIds = new ArrayList<String>();
                final ArrayList<HashMap<String, String>> historyDataList = new ArrayList<HashMap<String, String>>();
                try
                {
                    JSONArray ticketArray = (JSONArray) result;

                    ArrayList<LocationTicket> tickets = new ArrayList<LocationTicket>();
                    for (int i = 0; i < ticketArray.length(); i++)
                    {
                        JSONObject tickObj = (JSONObject) ticketArray.get(i);
                        LocationTicket ticket = new LocationTicket(tickObj);
                        if (ticket.Created != null)
                            tickets.add(new LocationTicket(tickObj));
                    }

                    Collections.sort(tickets, new Comparator<LocationTicket>()
                    {
                        public int compare(LocationTicket o1, LocationTicket o2)
                        {
                            return o2.Created.compareTo(o1.Created);
                        }
                    });

                    String todayString = android.text.format.DateFormat.format("MM/dd/yyyy", new Date()).toString();

                    for (LocationTicket ticket : tickets)
                    {
                        ticketIds.add(ticket.getId());
                        HashMap<String, String> map = new HashMap<String, String>();

                        String dateString = android.text.format.DateFormat.format("MM/dd/yyyy", ticket.Created).toString();
                        if (dateString.compareTo(todayString) == 0)
                            dateString = "Today";
                        dateString = dateString.concat(android.text.format.DateFormat.format(" hh:mm:ss a", ticket.Created).toString());

                        map.put("name", ticket.UserName + " / " + dateString);
                        map.put("date", dateString);
                        map.put("trails", String.format("%d", ticket.Trails));
                        map.put("id", ticket.getId());

                        historyDataList.add(map);
                    }
                }
                catch (JSONException ex)
                {

                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("History");

                LayoutInflater inflater = getLayoutInflater();
                View listViewCont = (View) inflater.inflate(R.layout.map_history_list, null);
                builder.setView(listViewCont);

                final ListView lv = (ListView) listViewCont.findViewById(R.id.history_list);
                SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), historyDataList, R.layout.map_history_list_row, new String[]{"date", "trails"}, new int[]{R.id.ticket_date, R.id.ticket_trails});
                lv.setAdapter(adapter);

                final AlertDialog dialog = builder.create();

                lv.setOnCreateContextMenuListener(new ListView.OnCreateContextMenuListener()
                {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
                    {
                        if (v.getId() == R.id.history_list)
                        {
                            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
                            HashMap<String, String> ticketInfo = (HashMap<String, String>)lv.getItemAtPosition(info.position);
                            menu.setHeaderTitle(ticketInfo.get("name"));

                            menu.add(Menu.NONE, 1, 1, "Play");
                            menu.add(Menu.NONE, 1, 2, "Delete");
                        }

                        MenuItem.OnMenuItemClickListener menuClickListener = new MenuItem.OnMenuItemClickListener()
                        {
                            @Override
                            public boolean onMenuItemClick(MenuItem item)
                            {
                                final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
                                HashMap<String, String> ticketInfo = (HashMap<String, String>)lv.getItemAtPosition(info.position);
                                switch (item.getItemId())
                                {
                                    case 1:
                                        followData.followPlaybackIndex = 0;
                                        followData.followTicketId = ticketIds.get(info.position);
                                        followData.followTicketName = historyDataList.get(info.position).get("name");
                                        followData.followTrails.clear();
                                        startHistoryPlayback();
                                        dialog.hide();
                                        break;
                                    case 2:
                                        RESTMgr.getInstance().deleteTicket(recvToken, ticketInfo.get("id"), new RESTMgr.OnTaskCompleted()
                                        {
                                            @Override
                                            public void onTaskCompleted(Object result)
                                            {
                                                historyDataList.remove(info.position);
                                                lv.invalidateViews();
                                            }
                                        });
                                        break;
                                }
                                return true;
                            }
                        };

                        for (int i = 0, n = menu.size(); i < n; i++)
                            menu.getItem(i).setOnMenuItemClickListener(menuClickListener);
                    }
                });

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
                    {
                        followData.followTicketType = TRAIL_TYPE_HISTORY;
                        followData.followPlaybackMode = false;
                        followData.followPlaybackIndex = 0;
                        followData.followTicketId = ticketIds.get(position);
                        followData.followTicketName = historyDataList.get(position).get("name");
                        if (mapView != null)
                        {
                            TextView mapTitle = (TextView) mapView.findViewById(R.id.mapTitle);
                            mapTitle.setText("History / " + followData.followTicketName);
                        }
                        ticketsGetHandler.removeCallbacks(ticketsTimer);
                    //    ticketsGetHandler.postDelayed(ticketsTimer, 1000);
                        updateMapPath(true, false);
                        dialog.hide();
                    }
                });

                dialog.show();
            }
        });
    }

    private void showTicketsSelection()
    {
        if (recvToken == null)
            return;
        String [] vals = new String[2];
        Uri dataUri = Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
        Helpers.GetContactInfo(this, dataUri, vals);
        String phoneNumber = Helpers.CleanPhoneNumber(vals[0]);
        RESTMgr.getInstance().getTargetActiveTickets(recvToken, phoneNumber, new RESTMgr.OnTaskCompleted()
        {
            @Override
            public void onTaskCompleted(Object result)
            {
                final ArrayList<CharSequence> ticketNames = new ArrayList<CharSequence>();
                final ArrayList<String> ticketIds = new ArrayList<String>();

                try
                {
                    JSONArray ticketArray = (JSONArray) result;
                    for (int i = 0; i < ticketArray.length(); i++)
                    {
                        JSONObject tickObj = (JSONObject) ticketArray.get(i);
                        LocationTicketTarget ticket = new LocationTicketTarget(tickObj);
                        ticketIds.add(ticket.TicketId);
                        ticketNames.add(ticket.UserName + " / " + ticket.Created.toString());
                    }
                }
                catch (JSONException ex)
                {

                }

                DialogInterface.OnClickListener ticketsDialogListener = new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        followData.followTicketType = TRAIL_TYPE_FOLLOW;
                        followData.followTicketId = ticketIds.get(which);
                        followData.followTicketName = ticketNames.get(which).toString();
                        if (mapView != null)
                        {
                            TextView mapTitle = (TextView) mapView.findViewById(R.id.mapTitle);
                            mapTitle.setText(followData.followTicketName);
                        }
                        ticketsGetHandler.removeCallbacks(ticketsTimer);
                        ticketsGetHandler.postDelayed(ticketsTimer, 1000);
                        mTrackingStarted = Calendar.getInstance().getTime();
                        trackTimerHandler.removeCallbacks(trackingTimeTimer);
                        trackTimerHandler.postDelayed(trackingTimeTimer, 1000);
                        updateMapPath(true, true);
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Select Track To Follow");
                builder.setItems(ticketNames.toArray(new CharSequence[ticketNames.size()]), ticketsDialogListener);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    Runnable ticketsTimer = new Runnable()
    {
        @Override
        public void run()
        {
            if (followData.followPlaybackMode)
            {
                if (followData.followPlaybackIndex < followData.followTrails.size())
                {
                    setTicketTrailStats(followData.followTrails.get(followData.followPlaybackIndex), true, true);
                    refreshPath(true, followData.followPlaybackIndex);
                    followData.followPlaybackIndex++;
                    ticketsGetHandler.postDelayed(ticketsTimer, 1000);
                }
            }
            else
            {
                updateMapPath(true, true);
                ticketsGetHandler.postDelayed(ticketsTimer, trailRefreshPeriod);
            }
        }
    };

    Runnable trackingTimeTimer = new Runnable()
    {
        @Override
        public void run()
        {
            if (mStatTime != null && mTrackingStarted != null)
            {
                long diff = Calendar.getInstance().getTime().getTime() - mTrackingStarted.getTime();
                long seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
                long hours = TimeUnit.MILLISECONDS.toHours(diff);

                mStatTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            }
            trackTimerHandler.postDelayed(trackingTimeTimer, 1000);
        }
    };

    /**
     * An AsyncTask that calls getFromLocation() in the background.
     * The class uses the following generic types:
     * Location - A {@link android.location.Location} object containing the current location,
     *            passed as the input parameter to doInBackground()
     * Void     - indicates that progress units are not used by this subclass
     * String   - An address passed to onPostExecute()
     */
    protected class GetAddressTask extends AsyncTask<Location, Void, String> {

        // Store the context passed to the AsyncTask when the system instantiates it.
        Context localContext;

        // Constructor called by the system to instantiate the task
        public GetAddressTask(Context context) {

            // Required by the semantics of AsyncTask
            super();

            // Set a Context for the background task
            localContext = context;
        }

        /**
         * Get a geocoding service instance, pass latitude and longitude to it, format the returned
         * address, and return the address to the UI thread.
         */
        @Override
        protected String doInBackground(Location... params) {
            /*
             * Get a new geocoding service instance, set for localized addresses. This example uses
             * android.location.Geocoder, but other geocoders that conform to address standards
             * can also be used.
             */
            Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());

            // Get the current location from the input parameter list
            Location location = params[0];

            // Create a list to contain the result address
            List <Address> addresses = null;

            // Try to get an address for the current location. Catch IO or network problems.
            try {

                /*
                 * Call the synchronous getFromLocation() method with the latitude and
                 * longitude of the current location. Return at most 1 address.
                 */
                addresses = geocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1
                );

                // Catch network or other I/O problems.
            } catch (IOException exception1) {

                // Log an error and return an error message
                Log.e(LocationUtils.APPTAG, getString(R.string.IO_Exception_getFromLocation));

                // print the stack trace
                exception1.printStackTrace();

                // Return an error message
                return (getString(R.string.IO_Exception_getFromLocation));

                // Catch incorrect latitude or longitude values
            } catch (IllegalArgumentException exception2) {

                // Construct a message containing the invalid arguments
                String errorString = getString(
                        R.string.illegal_argument_exception,
                        location.getLatitude(),
                        location.getLongitude()
                );
                // Log the error and print the stack trace
                Log.e(LocationUtils.APPTAG, errorString);
                exception2.printStackTrace();

                //
                return errorString;
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {

                // Get the first address
                Address address = addresses.get(0);

                // Format the first line of address
                String addressText = getString(R.string.address_output_string,

                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(0) : "",

                        // Locality is usually a city
                        address.getLocality(),

                        // The country of the address
                        address.getCountryName()
                );

                // Return the text
                return addressText;

                // If there aren't any addresses, post a message
            } else {
                return getString(R.string.no_address_found);
            }
        }

        /**
         * A method that's called once doInBackground() completes. Set the text of the
         * UI element that displays the address. This method runs on the UI thread.
         */
        @Override
        protected void onPostExecute(String address) {

            // Turn off the progress bar
//            mActivityIndicator.setVisibility(View.GONE);

            // Set the address in the UI
  //          mAddress.setText(address);
        }
    }

    private String getDeviceId()
    {
        if (this.deviceId == null)
        {
            WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            this.deviceId = wm.getConnectionInfo().getMacAddress();
        }
        return this.deviceId;
    }

    /**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode)
    {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                errorCode,
                this,
                LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
        }
    }

    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0)
                return MapFragment.newInstance();
            else
                return StatsFragment.newInstance();
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return "Track";
                case 1:
                    return "Stats";
                case 3:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    private BroadcastReceiver msgReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.hasExtra("state"))
            {
                String msg = intent.getStringExtra("state");
                if (mConnectionState != null)
                    mConnectionState.setText(msg);
            }
/*            if (intent.hasExtra("speed"))
            {
                String speedText = String.format("%.02f mph", intent.getDoubleExtra("speed", 0.0));
                if (mStatSpeed != null)
                    mStatSpeed.setText(speedText);
            }
            if (intent.hasExtra("avgspeed"))
            {
                String speedText = String.format("%.02f mph", intent.getDoubleExtra("avgspeed", 0.0));
                if (mStatAvgSpeed != null)
                    mStatAvgSpeed.setText(speedText);
            }*/
/*            if (intent.hasExtra("time"))
            {
                String timeText = intent.getStringExtra("time");
                if (mStatTime != null)
                    mStatTime.setText(timeText);
            }*/
/*            if (intent.hasExtra("distance"))
            {
                double distance = intent.getDoubleExtra("distance", 0.0);
                if (mStatDistance != null)
                    mStatDistance.setText(String.format("%.02f mi", 0.621371 * (distance/1000.0)));
            }*/
            if (intent.hasExtra("deviceTicketId"))
            {
                deviceTicketId = intent.getStringExtra("deviceTicketId");
                selectMe(null);
            }
            if (intent.hasExtra("trackingStarted"))
            {
                mTrackingStarted = (Date) intent.getSerializableExtra("trackingStarted");
//                trackTimerHandler.removeCallbacks(trackingTimeTimer);
  //              trackTimerHandler.postDelayed(trackingTimeTimer, 1000);
            }
        }
    };

    public static class DataFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static DataFragment newInstance(int sectionNumber) {
            DataFragment fragment = new DataFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public DataFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            ((MainActivity)getActivity()).mConnectionState = (TextView) rootView.findViewById(R.id.text_connection_state);
            ((MainActivity)getActivity()).mServiceButton = (Button) rootView.findViewById(R.id.start_service);

            if (((MainActivity)getActivity()).isServiceRunnning())
                ((MainActivity)getActivity()).mServiceButton.setText("Stop Tracking");
            else
                ((MainActivity)getActivity()).mServiceButton.setText("Start Tracking");


            return rootView;
        }
    }

    public static class StatsFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static StatsFragment newInstance() {
            StatsFragment fragment = new StatsFragment();
            return fragment;
        }

        public StatsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_stats, container, false);

            ((MainActivity)getActivity()).mStatTime = (TextView) rootView.findViewById(R.id.stats_time);
            ((MainActivity)getActivity()).mStatSpeed = (TextView) rootView.findViewById(R.id.stats_speed);
            ((MainActivity)getActivity()).mStatAvgSpeed = (TextView) rootView.findViewById(R.id.stats_avgspeed);
            ((MainActivity)getActivity()).mStatDistance = (TextView) rootView.findViewById(R.id.stats_distance);

            return rootView;
        }
    }

    public static class MapFragment extends Fragment implements OnMapReadyCallback {

        public static MapFragment newInstance()
        {
            MapFragment fragment = new MapFragment();
            return fragment;
        }

        public MapFragment() {
        }

        @Override
        public void onMapReady(GoogleMap map)
        {
            ((MainActivity) getActivity()).googleMap = map;
            map.setMyLocationEnabled(true);
            map.setMapType(((MainActivity) getActivity()).googleMapType);

            map.getUiSettings().setZoomControlsEnabled(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_map, container, false);

            try
            {
                ((MainActivity) getActivity()).mapView = rootView;

                ((MainActivity)getActivity()).mSmallStatTime = (TextView) rootView.findViewById(R.id.sstats_time);
                ((MainActivity)getActivity()).mSmallStatSpeed = (TextView) rootView.findViewById(R.id.sstats_speed);
                ((MainActivity)getActivity()).mSmallStatAvgSpeed = (TextView) rootView.findViewById(R.id.sstats_avgspeed);
                ((MainActivity)getActivity()).mSmallStatDistance = (TextView) rootView.findViewById(R.id.sstats_distance);

                ((MainActivity)getActivity()).mLargeStatTime = (TextView) rootView.findViewById(R.id.stats_time);
                ((MainActivity)getActivity()).mLargeStatSpeed = (TextView) rootView.findViewById(R.id.stats_speed);
                ((MainActivity)getActivity()).mLargeStatAvgSpeed = (TextView) rootView.findViewById(R.id.stats_avgspeed);
                ((MainActivity)getActivity()).mLargeStatDistance = (TextView) rootView.findViewById(R.id.stats_distance);

                final SlidingUpPanelLayout slider = (SlidingUpPanelLayout)rootView.findViewById(R.id.map_slider);

                if (slider != null)
                {
                    slider.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
                        @Override
                        public void onPanelSlide(View panel, float slideOffset) {
                        }

                        @Override
                        public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState)
                        {
                            LinearLayout smallStats = (LinearLayout) rootView.findViewById(R.id.stats_small);
                            LinearLayout largeStats = (LinearLayout) rootView.findViewById(R.id.stats_large);

                            if (newState ==  SlidingUpPanelLayout.PanelState.EXPANDED)
                            {
                                largeStats.setVisibility(View.VISIBLE);
                                smallStats.setVisibility(View.GONE);
                            }
                            else
                            {
                                largeStats.setVisibility(View.GONE);
                                smallStats.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    slider.setFadeOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            slider.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        }
                    });
                }


                if (((MainActivity) getActivity()).googleMap == null)
                {
//                    SupportMapFragment fm = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);

                    SupportMapFragment fm = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

                    // Getting GoogleMap object from the fragment
//                    ((MainActivity) getActivity()).googleMap = fm.getMap();

                    fm.getMapAsync(this);

//                    ((MainActivity) getActivity()).googleMap.setMapType(((MainActivity) getActivity()).googleMapType);


                    // Enabling MyLocation Layer of Google Map
                    //                ((MainActivity) getActivity()).googleMap.setMyLocationEnabled(true);
                }
            }
            catch (Exception ex) {}
            return rootView;
        }

        @Override
        public void onDestroyView()
        {
            super.onDestroyView();
/*            Fragment fragment = (getFragmentManager().findFragmentById(R.id.map));
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.remove(fragment);
            ft.commit();*/
        }
    }

    public interface OnAsyncCompleted{
        void OnAsyncCompleted(Object result);
    }
}
