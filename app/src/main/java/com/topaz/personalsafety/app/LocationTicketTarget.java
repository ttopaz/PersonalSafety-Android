package com.topaz.personalsafety.app;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Tony on 6/21/2014.
 */
public class LocationTicketTarget extends LocationTicket
{
    public Date TargetCreated;
    public String TargetUserId;
    public String TargetUserName;
    public String TicketId;

    public LocationTicketTarget(LocationTicket ticket)
    {
        this.UserId = ticket.UserId;
        this.UserName = ticket.UserName;
        this.DeviceId = ticket.DeviceId;
        this.Created = ticket.Created;
        this.Expires = ticket.Expires;
        this.Active = ticket.Active;
        this.Code = ticket.Code;
    }

    public LocationTicketTarget(JSONObject json)
    {
        super(json);
        try {
            super.setId(json.getString("_id"));
            this.TicketId = json.getString("TicketId");
            this.TargetCreated = new Date(json.getString("TargetCreated"));
            this.TargetUserId = json.getString("TargetUserId");
            this.TargetUserName = json.getString("TargetUserName");
        }
        catch (JSONException ex)
        {

        }
    }
}