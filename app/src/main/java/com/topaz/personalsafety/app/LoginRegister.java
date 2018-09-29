package com.topaz.personalsafety.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginRegister extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_register);
    }

    public void onLogin(View v)
    {
        final Dialog dialog = new Dialog(this);
        dialog.setTitle("Login");
        dialog.setContentView(R.layout.login);
        Button button = (Button)dialog.findViewById(R.id.login_button);
        final EditText user = (EditText)dialog.findViewById(R.id.login_user);
        final EditText pass = (EditText)dialog.findViewById(R.id.login_pass);
        final TextView stat = (TextView)dialog.findViewById(R.id.login_status);

        dialog.show();

        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                stat.setText("");
                try
                {
                    RESTMgr.getInstance().login(user.getText().toString(), pass.getText().toString(), new RESTMgr.OnTaskCompleted()
                    {
                        @Override
                        public void onTaskCompleted(Object result)
                        {
                            try
                            {
                                JSONObject obj = (JSONObject)result;
                                if (obj.has("token"))
                                {
                                    String authToken = obj.getString("token");
                                    SharedPreferences prefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("authToken", authToken);
                                    editor.commit();
                                    dialog.hide();
                                    finish();
                                }
                                else
                                    stat.setText("Invalid User or Password");
                            }
                            catch (JSONException ex)
                            {
                                stat.setText(ex.getMessage());
                            }
                        }
                    });
                }
                catch (Exception ex)
                {
                    stat.setText(ex.getMessage());
                }
            }
        });
    }
    public void onRegister(View v)
    {
        final Dialog dialog = new Dialog(this);
        dialog.setTitle("Register");
        dialog.setContentView(R.layout.register);
        Button button = (Button)dialog.findViewById(R.id.register_button);
        final EditText name = (EditText)dialog.findViewById(R.id.login_name);
        final EditText user = (EditText)dialog.findViewById(R.id.login_user);
        final EditText pass = (EditText)dialog.findViewById(R.id.login_pass);
        final TextView stat = (TextView)dialog.findViewById(R.id.reg_status);

        dialog.show();

        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                stat.setText("");
                if (name.getText().toString().trim().length() == 0
                    || user.getText().toString().trim().length() == 0
                        || pass.getText().toString().trim().length() == 0)
                {
                    stat.setText("Please fill all the fields");
                    return;
                }
                else if (pass.getText().toString().trim().length() < 6)
                {
                    stat.setText("Password must be at least 6 characters long");
                    return;
                }
                try
                {
                    RESTMgr.getInstance().register(name.getText().toString(), user.getText().toString(), pass.getText().toString(), new RESTMgr.OnTaskCompleted()
                    {
                        @Override
                        public void onTaskCompleted(Object result)
                        {
                            try
                            {
                                JSONObject obj = (JSONObject)result;
                                if (obj.has("token"))
                                {
                                    String authToken = obj.getString("token");
                                    SharedPreferences prefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("authToken", authToken);
                                    editor.commit();
                                    dialog.hide();
                                    finish();
                                }
                                else if (obj.has("error"))
                                    stat.setText(obj.getString("error"));
                            }
                            catch (JSONException ex)
                            {
                                stat.setText(ex.getMessage());
                            }
                        }
                    });
                }
                catch (Exception ex)
                {
                    stat.setText(ex.getMessage());
                }
            }
        });
    }
}
