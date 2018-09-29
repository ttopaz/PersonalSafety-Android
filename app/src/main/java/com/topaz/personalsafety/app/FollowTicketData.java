package com.topaz.personalsafety.app;

import java.util.ArrayList;

/**
 * Created by Tony on 3/14/2017.
 */
public class FollowTicketData
{
    public String followTicketId = null;
    public String followTicketName = null;
    public boolean followPlaybackMode = false;
    public int followPlaybackIndex = 0;
    public int followTicketType = 0;
    public ArrayList<LocationTrail> followTrails = new ArrayList<LocationTrail>();
}
