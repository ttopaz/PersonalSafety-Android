package com.topaz.personalsafety.app;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
//import com.google.android.gms.location.LocationClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.Duration;

public class TrackMeService extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener

{
    private String sendToken = null;
    private LocationTicket userTicket = null;
    private String deviceId = null;
    private Date trackingStarted;
    private double totalSpeed;
    private int numberOfTrails;
    private double totalDistance;
    // A request to connect to Location Services
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    // Stores the current instantiation of the location client in this object
//    private LocationClient mLocationClient;
    private boolean sendingPeriodical;
    private final ServiceMessageManager messageManager = new ServiceMessageManager();
    private final static ArrayList<TrackMsgInfo> targets = new ArrayList<TrackMsgInfo>();
    private Location lastGoodLocation = null;
    private LocationTrail lastGoodLocationTrail = null;

    public TrackMeService()
    {
    }

    /** Called when the service is being created. */
    @Override
    public void onCreate()
    {

    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setTicker("Personal Safety")
                .setContentTitle("Personal Safety")
                .setContentText("Service Running")
                .setSmallIcon(R.drawable.ic_launcher);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                msgReceiver, new IntentFilter("msgInfo"));


        Notification noti = builder.build();

        noti.flags = Notification.FLAG_FOREGROUND_SERVICE;
        startForeground(13333, noti);

        sendingPeriodical = false;

        sendToken = null;
        userTicket = null;

        trackingStarted = Calendar.getInstance().getTime();
        totalDistance = 0.0;
        totalSpeed = 0.0;
        numberOfTrails = 0;

        createLocationToken();
//        new Thread(messageManager).start();

/*        mLocationRequest = LocationRequest.create();

        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        mUpdatesRequested = true;

        mLocationClient = new LocationClient(this, this, this);

        createLocationToken();

        mLocationClient.connect();
*/

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        if (sendToken != null && userTicket != null)
        {
            String ticketId = userTicket.getId();
            userTicket = null;
            RESTMgr.getInstance().deactivateTicket(sendToken, ticketId, new RESTMgr.OnTaskCompleted()
            {
                @Override
                public void onTaskCompleted(Object result) {
                    userTicket = null;
                }
            });
        }

        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(msgReceiver);

        deleteLocationRequest();

        messageManager.offer(null);

        stopForeground(true);
    }

    private void createLocationToken()
    {
        try
        {
            String sendId = Helpers.GetSendDeviceId(this);
            RESTMgr.getInstance().login(sendId, sendId, new RESTMgr.OnTaskCompleted()
            {
                @Override
                public void onTaskCompleted(Object result) {
                    try {
                        sendToken = ((JSONObject)result).getString("token");
                        createLocationTicket();
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

    private void createLocationRequest()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

/*        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
*/
        mGoogleApiClient.connect();
/*        mLocationClient = new LocationClient(this, this, this);

        mLocationClient.connect();*/
    }

    private void deleteLocationRequest()
    {
        if (mGoogleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }

/*        if (mLocationClient != null)
        {
            mLocationClient.removeLocationUpdates(this);
            mLocationClient.disconnect();
            mLocationClient = null;
        }*/
    }

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
/*            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
            }*/
            return false;
        }
    }

    private void createLocationTicket()
    {
        if (sendToken == null)
            return;
        if (userTicket != null)
            return;
        userTicket = new LocationTicket();
        Calendar now = Calendar.getInstance();
        userTicket.Created = now.getTime();
        now.add(Calendar.HOUR,1);
        userTicket.Expires = now.getTime();
        userTicket.DeviceId = getDeviceId();
        userTicket.Active = true;

        String [] vals = new String[2];
        Uri dataUri = Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
        Helpers.GetContactInfo(this, dataUri, vals);

        userTicket.UserId = Helpers.CleanPhoneNumber(vals[0]);
        userTicket.UserName = vals[1];

        RESTMgr.getInstance().addTicket(sendToken, userTicket, new RESTMgr.OnTaskCompleted()
        {
            @Override
            public void onTaskCompleted(Object result) {
                try {
                    userTicket.setId(((JSONObject)result).getString("_id"));

                    Intent intent = new Intent("gpsInfo");
                    intent.putExtra("deviceTicketId", userTicket.getId());
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                    if (mGoogleApiClient == null)
                    {
                        sendingPeriodical = true;
                        deleteLocationRequest();
                        createLocationRequest();
                    }

                    new Thread(messageManager).start();
                }
                catch (Exception ex)
                {

                }
            }
        });
    }

    private static final int CHANGE_TIME = 1000 * 60;
    private static final double MIN_CHANGE_DIST = 2.0;

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        double distance = location.distanceTo(currentBestLocation);

        totalDistance += distance;

        if (distance < MIN_CHANGE_DIST)
            return false;

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > CHANGE_TIME;
        boolean isSignificantlyOlder = timeDelta < -CHANGE_TIME;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private void addTrail(Location loc)
    {
        if (sendToken == null || userTicket == null)
            return;
/*        if (userToken == null)
            return;
        if (userTicketId == null)
            return;*/
/*        if (curTrackInfo != null)
        {
            String smsBody = "https://www.google.com/maps";
            String locStr =  loc.getLatitude() + "," + loc.getLongitude();
            smsBody += "/place/" + locStr + "/@" + locStr + ",20z";

            curTrackInfo.Link = smsBody.toString();
            messageManager.offer(curTrackInfo);
        }*/
        int sentTrails = 0;

        for(final TrackMsgInfo info : this.targets)
        {
            if ((info.OneTime && !info.DoneTrail) || !info.OneTime)
            {
                if (!info.Registered)
                {
                    LocationTicketTarget target = new LocationTicketTarget(userTicket);
                    Calendar now = Calendar.getInstance();
                    target.TicketId = userTicket.getId();
                    target.TargetCreated  = now.getTime();
                    target.TargetUserId = info.PhoneNumber;
                    target.TargetUserName = info.Name;
                    RESTMgr.getInstance().addTicketTarget(sendToken, target, new RESTMgr.OnTaskCompleted()
                    {
                        @Override
                        public void onTaskCompleted(Object result)
                        {
                            info.Registered = true;
                            SmsManager smsManager = SmsManager.getDefault();
                            String msg = info.Message + "\r\n" + info.Link;
                            smsManager.sendTextMessage(info.PhoneNumber, null, msg, null, null);
                            info.DoneMsg = true;
                        }
                    });
                }
                else if (!info.DoneMsg)
                {
                    SmsManager smsManager = SmsManager.getDefault();
                    String msg = info.Message + "\r\n" + info.Link;
                    smsManager.sendTextMessage(info.PhoneNumber, null, msg, null, null);
                    info.DoneMsg = true;
                }
                info.DoneTrail = true;
            }
        }
        Intent intent = new Intent("gpsInfo");
        if (isBetterLocation(loc, lastGoodLocation))
        {
            LocationTrail trail = new LocationTrail(loc);
            trail.TicketId = userTicket.getId();

            lastGoodLocation = loc;

            messageManager.offer(trail);

            double speed = trail.getMPH();

            intent.putExtra("speed", speed);
            totalSpeed += speed;
            numberOfTrails++;
            intent.putExtra("avgspeed", totalSpeed / (double)numberOfTrails);
        }


        long diff = Calendar.getInstance().getTime().getTime() - this.trackingStarted.getTime();

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

/*        long timeDiffSecs = diff/1000;

        int hours = (int) (timeDiffSecs / 3600);
        int minutes = (int) (timeDiffSecs % 3600) /60;
        long seconds = (int) (timeDiffSecs % 3600) % 60;
*/
//        intent.putExtra("time", String.format("%02d:%02d:%02d", hours, minutes, seconds));
        intent.putExtra("distance", totalDistance);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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

    /*
 * Called by Location Services when the request to connect the
 * client finishes successfully. At this point, you can
 * request the current location or start periodic updates
 */
    @Override
    public void onConnected(Bundle bundle)
    {
        Intent intent = new Intent("gpsInfo");
        intent.putExtra("state", "Connected");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        if (mGoogleApiClient != null)
        {
            if (sendingPeriodical)
            {
                mLocationRequest = LocationRequest.create();

                mLocationRequest.setInterval(1000 * 10);

                // Use high accuracy
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                // Set the interval ceiling to one minute
                mLocationRequest.setFastestInterval(1000 * 5);

                startPeriodicUpdates();
            }
            else
            {
                Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//                Location currentLocation = mLocationClient.getLastLocation();
                addTrail(currentLocation);
            }
        }
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
/*    @Override
    public void onDisconnected()
    {
        Intent intent = new Intent("gpsInfo");
        intent.putExtra("state", "Disconneted");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
*/
    @Override
    public void onConnectionSuspended(int i) {
        Intent intent = new Intent("gpsInfo");
        intent.putExtra("state", "Disconneted");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
/*        if (connectionResult.hasResolution())
        {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            }
            catch (IntentSender.SendIntentException e)
            {

                // Log the error
                e.printStackTrace();
            }
        }
        else
        {
            // If no resolution is available, display a dialog to the user with the error.
        //    showErrorDialog(connectionResult.getErrorCode());
        }*/
    }

    /**
     * Report location updates to the UI.
     *
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location location)
    {
        addTrail(location);
    }

    private void startPeriodicUpdates()
    {
        if (mGoogleApiClient != null)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
/*            mLocationClient.requestLocationUpdates(mLocationRequest, this);*/
            if (sendToken == null)
                createLocationToken();
        }
    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates()
    {
        if (mGoogleApiClient != null)
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//            mLocationClient.removeLocationUpdates(this);
//        if (userToken != null && userTicketId != null)
  //          RESTMgr.getInstance().deleteTicket(userToken, userTicketId, null);
    //    userTicketId = null;
    }

    private TrackMsgInfo findTarget(TrackMsgInfo target)
    {
        for(TrackMsgInfo ctarget : targets)
        {
            if (ctarget.PhoneNumber == target.PhoneNumber)
                return ctarget;
        }
        return null;
    }

    private int getOneTimeTickets()
    {
        int oneTime = 0;
        for(TrackMsgInfo info : targets)
        {
            if (info.OneTime)
                oneTime++;
        }
        return oneTime;
    }

    private BroadcastReceiver msgReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.hasExtra("start"))
            {
                boolean needsRestart = false;
                TrackMsgInfo target = new Gson().fromJson(intent.getStringExtra("start"), TrackMsgInfo.class);
                TrackMsgInfo exTarget = findTarget(target);

                if (exTarget == null)
                {
                    targets.add(target);
/*                    if (userToken != null && userTicketId != null)
                    {
                        RESTMgr.getInstance().addTicketTarget(userToken, userTicketId, target.PhoneNumber, new RESTMgr.OnTaskCompleted()
                        {
                            @Override
                            public void onTaskCompleted(Object result)
                            {
                            }
                        });
                    }*/
                }
                else
                {
                    exTarget.DoneTrail = false;
                    exTarget.DoneMsg = false;
                    exTarget.OneTime = target.OneTime;
                }

                int oneTimes = getOneTimeTickets();
                boolean needsPeriodical = oneTimes < targets.size();
                if (mGoogleApiClient == null || sendingPeriodical != needsPeriodical)
                {
                    sendingPeriodical = needsPeriodical;
                    deleteLocationRequest();
                    createLocationRequest();
                }
            }
            if (intent.hasExtra("stop"))
            {
                TrackMsgInfo track = new Gson().fromJson(intent.getStringExtra("stop"), TrackMsgInfo.class);
                TrackMsgInfo exTrack = findTarget(track);
                if (exTrack != null)
                    targets.remove(exTrack);
                if (targets.size() == 0)
                    deleteLocationRequest();
            }
            if (intent.hasExtra("deviceTicketId"))
            {
                if (userTicket != null)
                {
                    Intent sendIntent = new Intent("gpsInfo");
                    sendIntent.putExtra("deviceTicketId", userTicket.getId());
                    sendIntent.putExtra("trackingStarted", trackingStarted);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendIntent);
                }
            }
        }
    };

    /**
     * Manages a thread-safe message queue using a Looper worker thread to complete blocking tasks.
     */
    public class ServiceMessageManager implements Runnable
    {
        public Handler messageHandler;
        private final BlockingQueue<LocationTrail> messageQueue = new LinkedBlockingQueue<LocationTrail>();
        private Boolean isMessagePending = false;

        @Override
        public void run()
        {
            Looper.prepare();
            messageHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg)
                {
                }
            };
            Looper.loop();
        }

        private void consumeAsync()
        {
            try {
                messageHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        synchronized (isMessagePending)
                        {
                            if (isMessagePending)
                            {
                                return;
                            }

                            synchronized (messageQueue)
                            {
                                if (messageQueue.size() == 0)
                                {
                                    return;
                                }
                                LocationTrail trail = messageQueue.peek();

                                if (trail.getId() == null)
                                {
                                    RESTMgr.getInstance().addTrail(sendToken, trail, new RESTMgr.OnTaskCompleted()
                                    {
                                        @Override
                                        public void onTaskCompleted(Object result)
                                        {
                                            JSONObject trailObj = (JSONObject)result;
                                            lastGoodLocationTrail = new LocationTrail(trailObj);
                                        }
                                    });
                                }
                                else
                                {
                                    RESTMgr.getInstance().updateTrail(sendToken, trail, new RESTMgr.OnTaskCompleted()
                                    {
                                        @Override
                                        public void onTaskCompleted(Object result)
                                        {
                                            JSONObject trailObj = (JSONObject)result;
                                            lastGoodLocationTrail = new LocationTrail(trailObj);
                                        }
                                    });
                                }
/*                                SmsManager smsManager = SmsManager.getDefault();
                                String msg = info.Message + "\r\n" + info.Link;
                                smsManager.sendTextMessage(info.PhoneNumber, null, msg, null, null);*/
                                messageQueue.remove();
                            }
                            isMessagePending = true;
                        }
                    }
                });
            }
            catch (Exception ex)
            {
            }
        }

        public boolean offer(final LocationTrail data)
        {
            if (data == null)
            {
                if (messageHandler != null)
                    messageHandler.getLooper().quit();
                return false;
            }
            final boolean success = messageQueue.offer(data);

            if (success)
            {
                isMessagePending = false;
                consumeAsync();
            }
            return success;
        }
    }
}
