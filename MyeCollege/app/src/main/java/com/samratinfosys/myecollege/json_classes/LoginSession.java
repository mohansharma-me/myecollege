package com.samratinfosys.myecollege.json_classes;

import android.content.Context;
import android.database.Cursor;

import com.samratinfosys.myecollege.utils.Helper;

/**
 * Created by iAmMegamohan on 12-04-2015.
 */
public class LoginSession {

    private boolean ready=false;

    private String deviceId=null;
    private String errorFor=null;

    public LoginSession(MyJSON json) {

    }

    public LoginSession(Cursor cursor) {

    }

    public enum LoginStatus {
        None,
        NoSuccess,
        Logged,
        InvalidUserId,
        InvalidUserPassword,
        NotActivated, Deactivated,
        Error
    }

    public LoginStatus status=LoginStatus.None;
    public Account account=null;

    public boolean verify() {
        if(ready && account!=null && status==LoginStatus.Logged) {
            return Helper.getDeviceId(null).equals(deviceId);
        }
        return false;
    }

    protected void init(Context context) {
        if(ready) {
            //account=new Account(context,getJSONObject("userData"));

            if(account==null || (account!=null && account.type== Account.AccountType.None)) {
                //return null;
            }

            if(deviceId.equals(Helper.getDeviceId(context))) {
                status=LoginStatus.Logged;
            } else {
                //
            }
        } else {
            if(errorFor==null) {
                status=LoginStatus.NoSuccess;
            } else {
                if(errorFor.equals("userId")) {
                    status=LoginStatus.InvalidUserId;
                } else if(errorFor.equals("userPassword")) {
                    status=LoginStatus.InvalidUserPassword;
                } else if(errorFor.equals("noactivation")) {
                    status=LoginStatus.NotActivated;
                } else if(errorFor.equals("deactivated")) {
                    status=LoginStatus.Deactivated;
                } else {
                    status=LoginStatus.NoSuccess;
                }
            }
        }
    }

}
