package com.samratinfosys.myecollege.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.widget.Toast;

import com.samratinfosys.myecollege.json_classes.College;
import com.samratinfosys.myecollege.json_classes.LoginSession;
import com.samratinfosys.myecollege.json_classes.MyJSON;
import com.samratinfosys.myecollege.json_classes.Timeline;
import com.samratinfosys.myecollege.utils.Helper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iAmMegamohan on 12-04-2015.
 */
public class JSONHelper {

    public static boolean updateTimelines(Context context, JSONArray array) {
        boolean flag=false;

        for(int i=0;i<array.length();i++) {
            try {
                MyJSON myJSON = new MyJSON(array.getJSONObject(i));
                if(myJSON.isReady()) {
                    Timeline timeline=new Timeline(myJSON);
                    Helper.getMainDatabase(context).updateOrAddTimeline(timeline);
                }
            } catch (Exception ex) {
                Helper.Log(ex,"updateTimelines: fromJSONHelper="+i+", "+array);
            }
        }

        return flag;
    }

    public static JSONObject getCollegeTimelines_JSON(Context context,long collegeId) {
        File timelineFile=Helper.getCollegeTimelineFile(context,collegeId);
        if(timelineFile.exists()) {
            try {
                byte[] bytes=Helper.readFile(timelineFile);
                if(bytes.length>0) {
                    JSONObject obj= new JSONObject(new String(bytes));
                    return obj;
                }
            } catch (Exception ex) {
                Helper.Log(ex,"getCollegeTimelines_JSON: "+collegeId);
            }
        }
        return null;
    }

    public static List<College> getColleges(Context context) {
        List<College> colleges=new ArrayList<College>();
        if(JSONDownloader.isColleges(context)) {
            File file= Helper.getFile(context,JSONDownloader.COLLEGE_FILE);
            if(file.exists()) {
                try {
                    FileInputStream fis=new FileInputStream(file);
                    byte[] bytes=new byte[fis.available()];
                    fis.read(bytes);
                    fis.close();
                    fis=null;
                    if(bytes.length>0) {
                        MyJSON obj=new MyJSON(new String(bytes));
                        if(obj.success) {
                            JSONArray array=obj.getJSONArray("colleges");
                            if(array!=null) {
                                for (int i = 0; i < array.length(); i++) {
                                    College c = new College(new MyJSON(array.getJSONObject(i)));
                                    if (c.ready)
                                        colleges.add(c);
                                }
                            }
                        }
                    }
                } catch (Exception e) {}
            }
        }
        return colleges;
    }

    public static Bitmap getImageFromJSON(Context context,String json, String name, int width, int height, boolean autoAdjust) {
        try {
            JSONObject obj=new JSONObject(json);
            if(!obj.isNull(name)) {
                return Helper.Images.ReadBitmap(context, Base64.decode(obj.getString(name), Base64.DEFAULT),width,height,autoAdjust);
            }
        } catch (Exception ex) {}
        return null;
    }

    public static Bitmap getImageFromJSON(Context context,JSONObject obj, String name, int width, int height, boolean autoAdjust) {
        try {
            if(!obj.isNull(name)) {
                return Helper.Images.ReadBitmap(context, Base64.decode(obj.getString(name), Base64.DEFAULT),width,height,autoAdjust);
            }
        } catch (Exception ex) {}
        return null;
    }

}
