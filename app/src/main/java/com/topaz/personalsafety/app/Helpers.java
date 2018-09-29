package com.topaz.personalsafety.app;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Random;

public class Helpers
{
    public static final long MS_PER_SECOND = 1000L;
    public static final long MS_PER_MINUTE = 60000L;
    public static final long MS_PER_HOUR = 3600000L;
    public static final long MS_PER_DAY = 86400000L;

    public static String formatDuration(long duration) //!! not localized
    {
        if (duration < 0)
        {
            duration = 0;
        }

        int days = (int) (duration / MS_PER_DAY);
        int hours = (int) (duration / MS_PER_HOUR) % 24;
        int minutes = (int) (duration / MS_PER_MINUTE) % 60;
        int seconds = (int) (duration / MS_PER_SECOND) % 60;

        // "10 days"
        if (days >= 10)
        {
            return days + " days";
        }

        // "1 day, 2:33"
        else if (days > 0)
        {
            return days + " day" + ((1 == days) ? ", " : "s, ") + hours + ((minutes < 10) ? ":0" : ":") + minutes;
        }

        // "2:33:44"
        else if (hours > 0)
        {
            return hours + ((minutes < 10) ? ":0" : ":") + minutes + ((seconds < 10) ? ":0" : ":") + seconds;
        }

        // "33:44"
        return ((minutes < 10) ? "0" : "") + minutes + ((seconds < 10) ? ":0" : ":") + seconds;
    }

    public static String Gen6DigitNumber()
    {
        Random rng = new Random();
        int val = rng.nextInt(100000);
        return String.format("%06d", val);
    }

    public static String GetSendDeviceId(Context context)
    {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wm.getConnectionInfo().getMacAddress() + "Send";
    }

    public static String GetRecvDeviceId(Context context)
    {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wm.getConnectionInfo().getMacAddress() + "Recv";
    }

    public static String GetPhoneNumber(Context context)
    {
        return CleanPhoneNumber(((TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE)).getLine1Number());
    }

    public static String CleanPhoneNumber(String number)
    {
        String phoneNum = "";
        for (int i = 0; i < number.length(); i++)
        {
            if (Character.isDigit(number.charAt(i)))
                phoneNum += number.charAt(i);

        }
        if (phoneNum.startsWith("1"))
            phoneNum = phoneNum.substring(1);
        return phoneNum;
    }

    public static void GetContactInfo(Context context, Uri uri, String[] vals)
    {
/*        String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME };
        String selection =  ContactsContract.Contacts.HAS_PHONE_NUMBER + " = '1'";
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        Cursor cursor = context.getContentResolver()
                .query(uri, projection, selection, null, sortOrder);
        cursor.moveToFirst();

        int PhoneId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        vals[1] = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

        String homePhone = "", cellPhone = "", workPhone = "", otherPhone = "";
        Cursor contactPhones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                , null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + PhoneId, null, null);

        while (contactPhones.moveToNext()) {

            String number = contactPhones.getString(contactPhones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            int type = contactPhones.getInt(contactPhones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            switch (type) {
                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:   homePhone = number; break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:  cellPhone = number; break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:   workPhone = number; break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:  otherPhone = number; break;
            }
        }
        vals[0] = ((cellPhone!="") ? cellPhone : ((homePhone!="") ? homePhone : ((workPhone!="") ? workPhone : otherPhone)));
*/
/*        Cursor cursorID = context.getContentResolver().query(uri,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        String contactID = "";
        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            vals[0] = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        if (cursor.moveToFirst()) {

            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.

            vals[1] = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();

        */

        // We only need the NUMBER column, because there will be only one row in the result
        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};

        // Perform the query on the contact to get the NUMBER column
        // We don't need a selection or sort order (there's only one result for the given URI)
        // CAUTION: The query() method should be called from a separate thread to avoid blocking
        // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
        // Consider using CursorLoader to perform the query.
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor.moveToFirst())
        {
            int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            vals[1] = cursor.getString(column);
        }
        cursor.close();

        String[] projection1 = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE};

        String homePhone = "", cellPhone = "", workPhone = "", otherPhone = "";
        cursor = context.getContentResolver().query(uri, projection1, null, null, null);

        while (cursor.moveToNext())
        {

            String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            switch (type)
            {
                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                    homePhone = number;
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                    cellPhone = number;
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                    workPhone = number;
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                    otherPhone = number;
                    break;
            }
        }
        vals[0] = ((cellPhone != "") ? cellPhone : ((homePhone != "") ? homePhone : ((workPhone != "") ? workPhone : otherPhone)));

        // Retrieve the phone number from the NUMBER column
        //        column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        //      vals[0] = cursor.getString(column);
    }

    private static int MAX_TRAILS_APP=100;

    public static String getTrailsAsString(ArrayList<LocationTrail> trails)
    {
        String ret = "";

        int count = 0;
        for (LocationTrail trail : trails)
        {
            String latLong = String.format("%f,%f", trail.Latitude, trail.Longitude);
            if (ret.length() > 0)
            {
                ret = ret.concat("|");
            }
            ret = ret.concat(latLong);
            count++;
            if (count >= MAX_TRAILS_APP)
                break;
        }

        return ret;
    }
}
