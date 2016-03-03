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

public class College {
    public long id;
    public String short_name;
    public String full_name;
    public String city;
    public String fax_number;
    public String email_address;
    public String telephone_number;
    public String website;
    public String code;

    public String address;
    public String type;
    public String estd_year;
    public String tution_fees;

    public boolean ready=false;

    public College(Cursor cursor) {
        ready=false;
        try {
            id = cursor.getLong(cursor.getColumnIndex("college_id"));
            short_name = cursor.getString(cursor.getColumnIndex("college_short_name"));
            full_name = cursor.getString(cursor.getColumnIndex("college_full_name"));
            city = cursor.getString(cursor.getColumnIndex("college_city"));
            fax_number = cursor.getString(cursor.getColumnIndex("college_fax_number"));
            email_address = cursor.getString(cursor.getColumnIndex("college_email_address"));
            telephone_number = cursor.getString(cursor.getColumnIndex("college_telephone_number"));
            website = cursor.getString(cursor.getColumnIndex("college_website"));
            code = cursor.getString(cursor.getColumnIndex("college_code"));

            address=cursor.getString(cursor.getColumnIndex("college_address"));
            type=cursor.getString(cursor.getColumnIndex("college_type"));
            estd_year=cursor.getString(cursor.getColumnIndex("college_estd_year"));
            tution_fees=cursor.getString(cursor.getColumnIndex("college_tution_fees"));

            ready=true;
        } catch(Exception ex) {
            Helper.Log(ex,"CollegeConstructor:from Cursor");
        }
    }

    public College(MyJSON json) {
        ready=false;
        try {
            if(json.isReady()) {
                id = json.getLong("college_id", 0);
                short_name = json.getString("college_short_name");
                full_name = json.getString("college_full_name");
                city = json.getString("college_city");
                fax_number = json.getString("college_fax_number");
                email_address = json.getString("college_email_address");
                telephone_number = json.getString("college_telephone_number");
                website = json.getString("college_website");
                code = json.getString("college_code");

                address=json.getString("college_address");
                type=json.getString("college_type");
                estd_year=json.getString("college_estd_year");
                tution_fees=json.getString("college_tution_fees");

                ready = true;
            }
        } catch (Exception ex) {
            Helper.Log(ex,"CollegeConstructor:from MyJSON");
        }
    }

}
