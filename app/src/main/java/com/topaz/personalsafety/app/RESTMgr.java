package com.topaz.personalsafety.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Created by Tony on 4/20/2014.
 */

public class RESTMgr {

    public interface OnTaskCompleted{
        void onTaskCompleted(Object result);
    }

    private static RESTMgr Instance;
    private String serverIP;
    private int serverPort;

    public static RESTMgr getInstance()
    {
        if (Instance == null)
            Instance = new RESTMgr();
        return Instance;
    }

    public void updateSettings(Context context)
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        if (settings.getBoolean("debugMode", false) == true)
            serverIP = settings.getString("debugServerIP", "192.168.1.166");
        else
            serverIP = settings.getString("prodServerIP", "none");
        serverPort = Integer.parseInt(settings.getString("serverPort", "3100"));
    }

    private String getServerAddress()
    {
        return String.format("http://%s:%d", serverIP, serverPort);
    }

    public void login(String user, String pass, OnTaskCompleted listener)
    {
        new GetTask(listener)
                .addHeader("username", user)
                .addHeader("password", pass)
                .execute(getServerAddress() + "/login");
    }
    public void register(String name, String user, String pass, OnTaskCompleted listener)
    {
        JSONObject data = new JSONObject();
        try {
            data.put("name", name);
            data.put("username", user);
            data.put("password", pass);
            new PostTask(listener)
                    .setData(data)
                    .execute(getServerAddress() + "/user");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
    public void getTargetActiveTickets(String token, String userNumber, OnTaskCompleted listener)
    {
        new GetTask(listener)
                .addHeader("token", token)
                .execute(getServerAddress() + "/ticketUsers/" + userNumber + "/active");
    }
    public void getUserTickets(String token, String userNumber, OnTaskCompleted listener)
    {
        new GetTask(listener)
                .addHeader("token", token)
                .execute(getServerAddress() + "/tickets/" + userNumber);
    }
    public void getLastTrail(String token, String tickeId, OnTaskCompleted listener)
    {
        new GetTask(listener)
                .addHeader("token", token)
                .execute(getServerAddress() + "/trail/" + tickeId + "/last");
    }

    public void getSnapToRoad(String trailsAsString, String apiKey, OnTaskCompleted listener)
    {
        try
        {
            String url = String.format("https://roads.googleapis.com/v1/snapToRoads?path=%s&interpolate=true&key=%s",
                    URLEncoder.encode(trailsAsString, "UTF-8"), apiKey);

            new GetTask(listener).execute(url);
        }
        catch (Exception ex)
        {
        }
    }

    public void getTicketTrails(String token, String tickeId, String fromId, OnTaskCompleted listener)
    {
        String query = getServerAddress() + "/trails/" + tickeId;
        if (fromId != null)
        {
            try
            {
                query += "/" + fromId;
            }
            catch (Exception ex)
            {

            }
        }
        new GetTask(listener)
                .addHeader("token", token)
                .execute(query);
    }
    public void getTicket(String token, String ticketId, OnTaskCompleted listener)
    {
        new GetTask(listener)
            .addHeader("token", token)
            .execute(getServerAddress() + "/ticket/" + ticketId);
    }
    public void addTicket(String token, LocationTicket ticket, OnTaskCompleted listener)
    {
        Gson gson = new GsonBuilder().serializeNulls().create();
        try {
            JSONObject data = new JSONObject(gson.toJson(ticket));
            new PostTask(listener)
                    .addHeader("token", token)
                    .setData(data)
                    .execute(getServerAddress() + "/ticket");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void deleteTicket(String token, String ticketId, OnTaskCompleted listener)
    {
        new DeleteTask(listener)
                .addHeader("token", token)
                .execute(getServerAddress() + "/ticket/" + ticketId);
    }
    public void deactivateTicket(String token, String ticketId, OnTaskCompleted listener)
    {
        new DeleteTask(listener)
                .addHeader("token", token)
                .execute(getServerAddress() + "/ticket/" + ticketId + "/deactivate");
    }
    public void deleteTrail(String token, String trailId, OnTaskCompleted listener)
    {
        new DeleteTask(listener)
                .addHeader("token", token)
                .execute(getServerAddress() + "/trail/" + trailId);
    }
    public void addTicketTarget(String token, LocationTicketTarget target, OnTaskCompleted listener)
    {
        Gson gson = new GsonBuilder().serializeNulls().create();
        try {
            JSONObject data = new JSONObject(gson.toJson(target));
            new PostTask(listener)
                    .addHeader("token", token)
                    .setData(data)
                    .execute(getServerAddress() + "/ticketUser");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void addTrail(String token, LocationTrail trail, OnTaskCompleted listener)
    {
        Gson gson = new GsonBuilder().serializeNulls().create();
        try {
            JSONObject data = new JSONObject(gson.toJson(trail));
            new PostTask(listener)
                    .addHeader("token", token)
                    .setData(data)
                    .execute(getServerAddress() + "/trail");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void updateTrail(String token, LocationTrail trail, OnTaskCompleted listener)
    {
        Gson gson = new GsonBuilder().serializeNulls().create();
        try {
            JSONObject data = new JSONObject(gson.toJson(trail));
            new PostTask(listener)
                    .addHeader("token", token)
                    .setData(data)
                    .execute(getServerAddress() + "/trail" + trail.getId());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class GetTask extends AsyncTask<String, String, String> {

        private OnTaskCompleted listener = null;
        private String responseString = null;
        private HashMap<String, String> headers;

        public GetTask(OnTaskCompleted listener)
        {
            this.listener = listener;
            headers = new HashMap<String, String>();
        }

        public GetTask addHeader(String key, String value)
        {
            headers.put(key, value);
            return this;
        }

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            try
            {
                HttpGet get = new HttpGet(uri[0]);
                for (String key : headers.keySet())
                    get.addHeader(key, headers.get(key));
                response = httpclient.execute(get);
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK)
                {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else
                {
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            }
            catch (ClientProtocolException e)
            {
                //TODO Handle problems..
            }
            catch (IOException e)
            {
                //TODO Handle problems..
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            try
            {
                if (this.responseString != null)
                {
                    Object json = new JSONTokener(this.responseString).nextValue();
                    // call callback
                    if (listener != null)
                        listener.onTaskCompleted(json);
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    public class PostTask extends AsyncTask<String, String, String>
    {
        private OnTaskCompleted listener = null;
        private String responseString = null;
        private HashMap<String, String> headers;
        private JSONObject data = null;

        public PostTask(OnTaskCompleted listener)
        {
            this.listener = listener;
            headers = new HashMap<String, String>();
        }

        public PostTask addHeader(String key, String value)
        {
            headers.put(key, value);
            return this;
        }
        public PostTask setData(JSONObject data)
        {
            this.data = data;
            return this;
        }

        @Override
        protected String doInBackground(String... uri)
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            try
            {
                HttpPost post = new HttpPost(uri[0]);
                for (String key : headers.keySet())
                    post.addHeader(key, headers.get(key));
                post.setHeader("Content-type", "application/json");
                if (data != null)
                {
                    StringEntity se = new StringEntity(data.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                }
                response = httpclient.execute(post);
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK)
                {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                }
                else
                {
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            }
            catch (ClientProtocolException e)
            {
                //TODO Handle problems..
            }
            catch (IOException e)
            {
                //TODO Handle problems..
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            try
            {
                if (this.responseString != null)
                {
                    Object json = new JSONTokener(this.responseString).nextValue();
                    // call callback
                    if (listener != null)
                        listener.onTaskCompleted(json);
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }
    public class DeleteTask extends AsyncTask<String, String, String> {

        private OnTaskCompleted listener = null;
        private String responseString = null;
        private HashMap<String, String> headers;

        public DeleteTask(OnTaskCompleted listener)
        {
            this.listener = listener;
            headers = new HashMap<String, String>();
        }

        public DeleteTask addHeader(String key, String value)
        {
            headers.put(key, value);
            return this;
        }
        public DeleteTask setData(JSONObject data)
        {
            return this;
        }

        @Override
        protected String doInBackground(String... uri)
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            try {
                HttpDelete del = new HttpDelete(uri[0]);
                for (String key : headers.keySet())
                    del.addHeader(key, headers.get(key));
                del.setHeader("Content-type", "application/json");
                response = httpclient.execute(del);
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK)
                {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                }
                else
                {
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            }
            catch (ClientProtocolException e)
            {
                //TODO Handle problems..
            }
            catch (IOException e)
            {
                //TODO Handle problems..
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            try
            {
                if (this.responseString != null)
                {
                    Object json = new JSONTokener(this.responseString).nextValue();
                    // call callback
                    if (listener != null)
                        listener.onTaskCompleted(json);
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }
}