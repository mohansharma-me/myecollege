package com.samratinfosys.myecollege.tools.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;

import com.samratinfosys.myecollege.json_classes.MyJSON;
import com.samratinfosys.myecollege.json_classes.Profile;
import com.samratinfosys.myecollege.json_classes.Timeline;
import com.samratinfosys.myecollege.tools.DownloadManager;
import com.samratinfosys.myecollege.tools.GeneralConstants;
import com.samratinfosys.myecollege.utils.Helper;

/**
 * Created by iAmMegamohan on 19-04-2015.
 */
public class JSONLoader implements DownloadManager.DownloadStatus {

    /*************************************************************************/

    public static JSONLoader putChat(Context context, String message, long profileId, IJSONLoader ijsonLoader) {
        String data="updateIn=put-chat&msg="+message+"&toId="+profileId;
        return new JSONLoader(context,0,GeneralConstants.API_UPDATE, data,ijsonLoader);
    }

    public static JSONLoader loadUsers(Context context, IJSONLoader ijsonLoader) {
        return new JSONLoader(context,0,GeneralConstants.API_LOADUSERS,"",ijsonLoader);
    }

    public static JSONLoader updateTimeline(Context context, Timeline.Types type, MyJSON timelineData, IJSONLoader ijsonLoader) {
        String data="updateIn=timeline&timelineType="+Timeline.filterToInt(type)+"&timelineData="+timelineData+"&encoded=0";
        return new JSONLoader(context, 0,GeneralConstants.API_UPDATE ,data,ijsonLoader);
    }

    public static JSONLoader loadTimelines(Context context, long collegeId, long profileId, long offset, IJSONLoader ijsonLoader) {
        String data="timelineOffset="+offset;
        if(collegeId>0) {
            data="collegeId="+collegeId+"&"+data;
        } else if(profileId>0) {
            data="profileId="+profileId+"&"+data;
        }
        return new JSONLoader(context,(int)collegeId,GeneralConstants.API_TIMELINE,data,ijsonLoader);
    }

    public static JSONLoader loadLoginSession(Context context, int loginRefId, String userId, String userPassword, IJSONLoader ijsonLoader) {

        try {
            userId = Base64.encodeToString(userId.getBytes(), Base64.DEFAULT);
            userPassword = Base64.encodeToString(userPassword.getBytes(), Base64.DEFAULT);
        } catch(Exception ex) {
            Helper.Log(ex,"loadLoginSession: "+loginRefId+", id:"+userId+", pass:"+userPassword);
        }

        String data="userId="+userId+"&userPassword="+userPassword;
        return new JSONLoader(context, loginRefId, GeneralConstants.API_LOGIN , data,ijsonLoader);

    }

    public static JSONLoader loadForgotPasswordAPI(Context context, int refId, String userId, IJSONLoader ijsonLoader) {
        try {
            userId = Base64.encodeToString(userId.getBytes(), Base64.DEFAULT);
        } catch(Exception ex) {
            Helper.Log(ex,"loadLoginSession: "+refId+", id:"+userId);
        }

        String data="userId="+userId;
        return new JSONLoader(context, refId, GeneralConstants.API_FORGOT_PASSWORD, data,ijsonLoader);
    }

    public static JSONLoader loadCollege(Context context, long collegeId, IJSONLoader ijsonLoader) {
        String data="collegeId="+collegeId;
        return new JSONLoader(context,(int)collegeId,GeneralConstants.API_COLLEGES,data,ijsonLoader);
    }

    public static JSONLoader loadProfile(Context context, long id, boolean throughAccountID, IJSONLoader ijsonLoader) {
        String data=throughAccountID?"accountId=":"profileId=";
        data+=id+"";
        return new JSONLoader(context,(int)id,GeneralConstants.API_PROFILE, data,ijsonLoader);
    }

    public static JSONLoader updateProfile(Context context, Profile profile, String profilePic, IJSONLoader ijsonLoader) {
        String data="updateIn=profile";
        data+="&firstName="+profile.first_name;
        data+="&lastName="+profile.last_name;
        data+="&mobileNumber="+profile.mobile_number;
        data+="&emailAddress="+profile.email_address;
        data+="&gender="+profile.fromGender(profile.gender);
        data+="&dateOfBirth="+Helper.Util.dateToSQLString(profile.dateofbirth);
        data+="&semester="+profile.semester;
        data+="&address="+profile.address;

        if(profilePic!=null) {
            data+="&profilePic="+profilePic;
        }

        return new JSONLoader(context,(int)profile.id,GeneralConstants.API_UPDATE, data,ijsonLoader);
    }

    /*************************************************************************/

    public interface IJSONLoader {
        public void jsonStatus(int jsonId, int percentage);
        public void jsonUpdated(int jsonId, JSONLoader jsonLoader);
    }

    private int jsonId=-1;
    public String url=null;
    private IJSONLoader ijsonLoader=null;
    public MyJSON myJSON=null;
    public boolean success=false, failed=false;

    public byte[] downloadedData=null;
    public DownloadManager.RealDownloader realDownloader=null;

    private JSONLoader(Context context,int jsonId, String url, String queryString, IJSONLoader ijsonLoader) {
        this.jsonId=jsonId;
        this.url=url;
        this.ijsonLoader=ijsonLoader;

        success=failed=false;

        if (this.url != null && this.ijsonLoader != null) {
            realDownloader=DownloadManager.Download(jsonId, context, this.url, queryString, this);
        } else {
            failed=true;
            if (ijsonLoader != null) {
                ijsonLoader.jsonUpdated(jsonId, this);
            }
        }
    }

    public boolean cancelLoader() {
        if(realDownloader!=null)
            return realDownloader.CancelDownload();
        return false;
    }

    @Override
    public void DownloadStarted(int downloadId) {
        if(ijsonLoader!=null)
            ijsonLoader.jsonStatus(downloadId,0);
    }

    @Override
    public void DownloadFailed(int downloadId, Exception ex, int httpResponseCode) {
        failed=true;
        if(ijsonLoader!=null)
            ijsonLoader.jsonUpdated(downloadId,this);
    }

    @Override
    public void DownloadProgress(int downloadId, long percentage, long downloadedBytes, long totalBytes) {
        if(ijsonLoader!=null)
            ijsonLoader.jsonStatus(downloadId,(int)percentage);
    }

    @Override
    public void DownloadCompleted(int downloadId, byte[] data, String filepath, boolean isCancelled) {
        if(!isCancelled && data!=null) {
            downloadDone(downloadId,data);
        }
    }

    private void downloadDone(int downloadId,byte[] data) {
        success=true;
        downloadedData=data;
        myJSON=new MyJSON(new String(downloadedData));
        if(ijsonLoader!=null)
            ijsonLoader.jsonUpdated(downloadId,this);
    }
}

