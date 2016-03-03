package com.samratinfosys.myecollege.json_classes;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Base64;

import com.samratinfosys.myecollege.utils.Helper;

import org.json.JSONObject;

/**
 * Created by iAmMegamohan on 12-04-2015.
 */
public class Account {

    public Account(MyJSON json) {
        ready=false;
        try {
            if(json.isReady()) {
                id=json.getLong("account_id",0);
                String s=json.getString("account_type");
                type=getAccountType(s);
                name=json.getString("account_name");
                user_id=json.getString("account_user_id");
                email_address=json.getString("account_email_address");
                mobile_number=json.getString("account_mobile_number");
                activation_status=json.getString("account_activation_status");
                college_id=json.getLong("account_college_id",0);
                ready=true;
            }
        } catch (Exception ex) {
            Helper.Log(ex,"initAccount: from MyJSON");
        }
    }

    public Account(Cursor cursor) {
        ready=false;
        try {
            id=cursor.getLong(cursor.getColumnIndex("account_id"));
            String s=cursor.getString(cursor.getColumnIndex("account_type"));
            type=getAccountType(s);
            name=cursor.getString(cursor.getColumnIndex("account_name"));
            user_id=cursor.getString(cursor.getColumnIndex("account_user_id"));
            email_address=cursor.getString(cursor.getColumnIndex("account_email_address"));
            mobile_number=cursor.getString(cursor.getColumnIndex("account_mobile_number"));
            activation_status=cursor.getString(cursor.getColumnIndex("account_activation_status"));
            college_id=cursor.getLong(cursor.getColumnIndex("account_college_id"));
            ready=true;
        } catch (Exception ex) {
            Helper.Log(ex,"initAccount: from Cursor");
        }
    }

    public enum AccountType {
        Student,
        Faculty,
        None
    }

    public boolean ready=false;

    public long id;
    public AccountType type=AccountType.None;
    public String name;
    public String user_id;
    public String email_address;
    public String mobile_number;
    public String activation_status;
    public long college_id;

    public static AccountType getAccountType(String validator) {
        if(validator.equals("student"))
            return AccountType.Student;
        if(validator.equals("faculty"))
            return AccountType.Faculty;
        return AccountType.None;
    }
}
