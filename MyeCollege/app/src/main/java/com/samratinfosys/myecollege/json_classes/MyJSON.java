package com.samratinfosys.myecollege.json_classes;

import com.samratinfosys.myecollege.tools.JSONDownloader;
import com.samratinfosys.myecollege.utils.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by iAmMegamohan on 13-04-2015.
 */
public class MyJSON {

    protected JSONObject jsonObject=null;
    //protected String jsonString=null;

    public boolean success=false;
    public String error_message=null;
    public String success_message=null;
    public String deviceId=null;

    protected boolean _isReady=false;

    public MyJSON(boolean newJson) {
        jsonObject=new JSONObject();
    }

    public MyJSON(String json) {
        try {
            //jsonString=json;
            jsonObject=new JSONObject(json);
        } catch (Exception ex) {}
        init();
    }

    public MyJSON(JSONObject obj) {
        try {
            this.jsonObject=obj;
        } catch(Exception ex) {

        }
        init();
    }

    public boolean isReady() {
        return _isReady;
    }

    protected void init() {
        if(jsonObject!=null)
            _isReady=true;
        success=getBoolean("success",false);
        error_message=getString("error_message");
        success_message=getString("success_message");
        deviceId=getString("deviceId");
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        try {
            if (jsonObject!=null && !jsonObject.isNull(name))
                return jsonObject.getBoolean(name);
        } catch(Exception ex) {

        }
        return defaultValue;
    }

    public String getString(String name) {
        try {
            if (jsonObject!=null && !jsonObject.isNull(name))
                return jsonObject.getString(name);
        } catch(Exception ex) {

        }
        return null;
    }

    public JSONObject getJSONObject(String name) {
        try {
            if (jsonObject!=null && !jsonObject.isNull(name))
                return jsonObject.getJSONObject(name);
        } catch(Exception ex) {

        }
        return null;
    }

    public int getInt(String name, int defaultValue) {
        try {
            if (jsonObject!=null && !jsonObject.isNull(name))
                return jsonObject.getInt(name);
        } catch(Exception ex) {

        }
        return defaultValue;
    }

    public double getDouble(String name,double defaultValue) {
        try {
            if (jsonObject!=null && !jsonObject.isNull(name))
                return jsonObject.getDouble(name);
        } catch(Exception ex) {

        }
        return defaultValue;
    }

    public long getLong(String name,long defaultValue) {
        try {
            if (jsonObject!=null && !jsonObject.isNull(name))
                return jsonObject.getLong(name);
        } catch(Exception ex) {

        }
        return defaultValue;
    }

    public JSONArray getJSONArray(String name) {
        try {
            if (jsonObject!=null && !jsonObject.isNull(name))
                return jsonObject.getJSONArray(name);
        } catch(Exception ex) {

        }
        return null;
    }

    @Override
    public String toString() {
        //if(jsonString!=null)
        //    return jsonString;
        return jsonObject==null?"":jsonObject.toString();
    }


    /////////////////////////////// SET FIELDS ///////////////////////////////////

    public JSONObject put(String name, boolean value) {
        try {
            return jsonObject.put(name,value);
        } catch (Exception ex) {
            Helper.Log(ex,"put:boolean: "+name);
        }
        return null;
    }

    public JSONObject put(String name, String value) {
        try {
            return jsonObject.put(name,value);
        } catch (Exception ex) {
            Helper.Log(ex,"put:string: "+name);
        }
        return null;
    }

    public JSONObject put(String name, Object value) {
        try {
            return jsonObject.put(name,value);
        } catch (Exception ex) {
            Helper.Log(ex,"put:object: "+name);
        }
        return null;
    }

    public JSONObject put(String name, int value) {
        try {
            return jsonObject.put(name,value);
        } catch (Exception ex) {
            Helper.Log(ex,"put:int: "+name);
        }
        return null;
    }

    public JSONObject put(String name, long value) {
        try {
            return jsonObject.put(name,value);
        } catch (Exception ex) {
            Helper.Log(ex,"put:long: "+name);
        }
        return null;
    }

    public JSONObject put(String name, double value) {
        try {
            return jsonObject.put(name,value);
        } catch (Exception ex) {
            Helper.Log(ex,"put:double: "+name);
        }
        return null;
    }
}
