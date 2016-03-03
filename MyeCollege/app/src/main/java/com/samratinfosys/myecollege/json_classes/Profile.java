package com.samratinfosys.myecollege.json_classes;

import android.database.Cursor;
import android.text.format.DateFormat;

import com.samratinfosys.myecollege.utils.Helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by iAmMegamohan on 20-04-2015.
 */
public class Profile {

    public boolean ready=false;

    public Profile(MyJSON json) {
        try {

            id=json.getLong("profile_id",0);
            if(id>0) {
                account_id=json.getLong("profile_account_id",0);
                if(account_id>0){
                    first_name=json.getString("profile_first_name");
                    last_name=json.getString("profile_last_name");
                    mobile_number=json.getString("profile_mobile_number");
                    email_address=json.getString("profile_email_address");
                    timeline_status=json.getString("profile_timeline_status");
                    dateofbirth=Helper.Util.parseDate(json.getString("profile_dateofbirth"), "yyyy-MM-dd");
                    address=json.getString("profile_address");
                    gender=fromInt(json.getInt("profile_gender",0));
                    semester=json.getInt("profile_semester",-1);
                    ready = true;
                }
            }

        } catch (Exception ex) {
            Helper.Log(ex,"ProfileConstructor: fromJSON");
        }
    }

    public Profile(Cursor cursor) {
        try {

            id=cursor.getLong(cursor.getColumnIndex("profile_id"));
            if(id>0) {
                account_id=cursor.getLong(cursor.getColumnIndex("profile_account_id"));
                if(account_id>0){
                    first_name=cursor.getString(cursor.getColumnIndex("profile_first_name"));
                    last_name=cursor.getString(cursor.getColumnIndex("profile_last_name"));
                    mobile_number=cursor.getString(cursor.getColumnIndex("profile_mobile_number"));
                    email_address=cursor.getString(cursor.getColumnIndex("profile_email_address"));
                    timeline_status=cursor.getString(cursor.getColumnIndex("profile_timeline_status"));
                    dateofbirth=Helper.Util.parseDate(cursor.getString(cursor.getColumnIndex("profile_dateofbirth")), "yyyy-MM-dd");
                    address=cursor.getString(cursor.getColumnIndex("profile_address"));
                    gender=fromInt(cursor.getInt(cursor.getColumnIndex("profile_gender")));
                    semester=cursor.getInt(cursor.getColumnIndex("profile_semester"));
                    ready = true;
                }
            }

        } catch (Exception ex) {
            Helper.Log(ex,"ProfileConstructor: fromCursor");
        }
    }

    public long id;
    public long account_id;
    public String first_name;
    public String last_name;
    public String mobile_number;
    public String email_address;
    public String timeline_status;
    public Date dateofbirth;
    public String address;
    public Gender gender;
    public int semester;


    public enum Gender {
        Male, //1
        Female, //2
        Other // any
    }

    public static Gender fromInt(int gender) {
        switch (gender) {
            case 1:
                return Gender.Male;
            case 2:
                return Gender.Female;
        }
        return Gender.Other;
    }

    public static int fromGender(Gender gender) {
        if(gender==Gender.Male)
            return 1;

        if(gender==Gender.Female)
            return 2;

        return 0;
    }
}
