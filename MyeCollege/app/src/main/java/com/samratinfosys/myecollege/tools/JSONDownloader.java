package com.samratinfosys.myecollege.tools;

import android.content.Context;
import android.util.Base64;

import com.samratinfosys.myecollege.json_classes.Account;
import com.samratinfosys.myecollege.json_classes.LoginSession;
import com.samratinfosys.myecollege.utils.Helper;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by iAmMegamohan on 12-04-2015.
 */
public class JSONDownloader {

    public static final String COLLEGE_FILE="colleges.json";
    public static final String SESSION_FILE="session.bin";
    public static final String COLLEGE_TIMELINE_FILE="college_timeline.json";

    public static DownloadManager.RealDownloader GetColleges(int downloadId,Context context,DownloadManager.DownloadStatus downloadStatus)
    {
        return DownloadManager.DownloadTo(downloadId,context,GeneralConstants.API_COLLEGES, COLLEGE_FILE, downloadStatus);
    }

    public static DownloadManager.RealDownloader Login(int downloadId,String userId, String userPassword, Context context,DownloadManager.DownloadStatus downloadStatus)
    {
        try {
            userId=Base64.encodeToString(userId.getBytes(),Base64.DEFAULT);
            userPassword=Base64.encodeToString(userPassword.getBytes(),Base64.DEFAULT);
            //userId = new String(Base64.encode(userId.getBytes("UTF-8"), Base64.DEFAULT));
            //userPassword = new String(Base64.encode(userPassword.getBytes("UTF-8"), Base64.DEFAULT));
        } catch(Exception ex) {}
        return DownloadManager.DownloadTo(downloadId, context, GeneralConstants.API_LOGIN, "userId=" + userId + "&userPassword=" + userPassword, SESSION_FILE, downloadStatus);
    }

    public static DownloadManager.RealDownloader RequestCode(int downloadId,String userId, Context context, DownloadManager.DownloadStatus downloadStatus) {
        userId=Base64.encodeToString(userId.getBytes(),Base64.DEFAULT);
        return DownloadManager.Download(downloadId,context,GeneralConstants.API_REQUESTCODE,"userId="+userId,downloadStatus);
    }

    public static DownloadManager.RealDownloader ActivateNow(int downloadId,String code, Context context, DownloadManager.DownloadStatus downloadStatus) {
        code=Base64.encodeToString(code.getBytes(),Base64.DEFAULT);
        return DownloadManager.Download(downloadId,context,GeneralConstants.API_ACTIVATENOW,"activationCode="+code,downloadStatus);
    }

    public static DownloadManager.RealDownloader ForgotPassword(int downloadId,String userId, Context context, DownloadManager.DownloadStatus downloadStatus) {
        userId=Base64.encodeToString(userId.getBytes(),Base64.DEFAULT);
        return DownloadManager.Download(downloadId,context,GeneralConstants.API_FORGOT_PASSWORD,"userId="+userId,downloadStatus);
    }

    public static DownloadManager.RealDownloader VerifyDevice(int downloadId,Account loggedAccount, Context context, DownloadManager.DownloadStatus downloadStatus) {
        String userId="userId="+(loggedAccount!=null?loggedAccount.user_id:"");
        return DownloadManager.Download(downloadId,context,GeneralConstants.API_VERIFY_DEVICE,userId,downloadStatus);
    }

    public static DownloadManager.RealDownloader GetFullCollegeLogo(int downloadId,long collegeId, Context context, DownloadManager.DownloadStatus downloadStatus) {
        return DownloadManager.Download(downloadId,context,GeneralConstants.API_GET_COLLEGE_POSTERIMAGE,"collegeId="+collegeId,downloadStatus);
    }

    public static DownloadManager.RealDownloader GetNetworkParameters(int downloadId, Context context, DownloadManager.DownloadStatus downloadStatus) {
        return DownloadManager.Download(downloadId,context,GeneralConstants.API_NETWORK_PARAMETERS ,downloadStatus);
    }

    public static DownloadManager.RealDownloader CollegeTimeline(int downloadId, Context context, long collegeId, long offset, DownloadManager.DownloadStatus downloadStatus) {
        String qs="collegeId="+collegeId+"&timelineOffset="+offset;
        return DownloadManager.DownloadTo(downloadId, context, GeneralConstants.API_TIMELINE, qs, "timeline-updates.tmp", downloadStatus);
    }


    /*****************************************************************************/

    public static boolean WriteDataTo(Context context,String filename, byte[] data) {
        try {
            File f=Helper.getFile(context,filename);
            if(f!=null && f.exists())
                f.delete();
            if(f.createNewFile()) {
                FileOutputStream fos=new FileOutputStream(f);
                fos.write(data);
                fos.flush();
                fos.close();
                return true;
            }
        } catch (Exception ex) {

        }
        return false;
    }

    public static boolean isColleges(Context context) {
        File f=Helper.getFile(context,COLLEGE_FILE);
        if(f!=null)
            return f.exists();
        return false;
    }

    public static boolean isLoginSessionAvailable(Context context) {
        File f=Helper.getFile(context,SESSION_FILE);
        if(f!=null && f.exists())
            return true;
        return false;
    }

    public static boolean DeleteLoginSession(Context context) {
        File f=Helper.getFile(context,SESSION_FILE);
        if(f!=null && f.exists()) {
            if(f.delete())
                return true;
        }
        return false;
    }
}
