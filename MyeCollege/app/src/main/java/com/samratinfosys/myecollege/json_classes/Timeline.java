package com.samratinfosys.myecollege.json_classes;

import android.database.Cursor;

import com.samratinfosys.myecollege.utils.Helper;

import org.json.JSONObject;

/**
 * Created by iAmMegamohan on 16-04-2015.
 */
public class Timeline {

    public enum Types {
        None,  //0
        Text,  //1
        Image, //2
        Audio, //3
        Video  //4
    }

    public long id=0;
    public long profile_id=0;
    public long college_id=0;
    public Types type=Types.None;
    public String timestamp=null;
    public SecurityData security_data=null;
    public Data data=null;

    public boolean ready=false;

    public Timeline(MyJSON json) {
        ready=false;
        try {
            id=json.getLong("timeline_id", 0);
            profile_id=json.getLong("timeline_profile_id", 0);
            college_id=json.getLong("timeline_college_id", 0);
            type= filterToType(json.getInt("timeline_type", 0));
            timestamp=json.getString("timeline_timestamp");
            security_data=null; // no required now
            data=new Data(json.getString("timeline_data"));
            ready=true;
        } catch (Exception ex) {
            Helper.Log(ex,"TimelineConstrcutor: fromJSON");
        }
    }

    public Timeline(Cursor cursor) {
        ready=false;
        try {
            id=cursor.getLong(cursor.getColumnIndex("timeline_id"));
            profile_id=cursor.getLong(cursor.getColumnIndex("timeline_profile_id"));
            college_id=cursor.getLong(cursor.getColumnIndex("timeline_college_id"));
            type= filterToType(cursor.getInt(cursor.getColumnIndex("timeline_type")));
            timestamp=cursor.getString(cursor.getColumnIndex("timeline_timestamp"));
            security_data=null; // not required now
            data=new Data(cursor.getString(cursor.getColumnIndex("timeline_data")));
            ready=true;
        } catch (Exception ex) {
            Helper.Log(ex,"TimelineConstructor: fromCursor");
        }
    }

    public static Types filterToType(int typeCode) {
        switch (typeCode) {
            case 1:
                return Types.Text;
            case 2:
                return Types.Image;
            case 3:
                return Types.Audio;
            case 4:
                return Types.Video;
        }
        return Types.None;
    }

    public static int filterToInt(Types type) {
        if(type==Types.Text)
            return 1;
        if(type==Types.Image)
            return 2;
        if(type==Types.Audio)
            return 3;
        if(type==Types.Video)
            return 4;
        return 0;
    }

    public class Data extends MyJSON {

        public MyJSON rawObject=null;
        //public String data_raw=null;
        public String data_heading =null;

        public Data(String json) {
            super(json);
            init();
        }

        @Override
        protected void init() {
            super.init();
            data_heading =this.getString("data_heading");
            try {
                rawObject=new MyJSON(this.getString("data_raw"));
            } catch (Exception e) {}
            this.success=true;
        }
    }

    public class SecurityData extends MyJSON {

        public SecurityData(String json) {
            super(json);
        }

        public SecurityData(JSONObject obj) {
            super(obj);
        }
    }
}
