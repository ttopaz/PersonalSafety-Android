package com.topaz.personalsafety.app;

/**
 * Created by Tony on 5/18/2014.
 */
public class TrackMsgInfo
{
    public Boolean OneTime;
    public Boolean SendGPS;
    public Boolean SendPhotos;
    public String Message;
    public String Link;
    public String Name;
    public String PhoneNumber;
    public Boolean DoneTrail;
    public Boolean DoneMsg;
    public Boolean Registered;

    public TrackMsgInfo()
    {
        OneTime = true;
        SendGPS = false;
        SendPhotos = false;
        Name = "";
        Message = "";
        PhoneNumber = "";
        Link = "";
        DoneTrail = false;
        DoneMsg = false;
        Registered = false;
    }
}
