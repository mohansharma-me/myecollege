package com.samratinfosys.myecollege.utils;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;

import com.samratinfosys.myecollege.Activation;
import com.samratinfosys.myecollege.CollegeInfo;
import com.samratinfosys.myecollege.CollegePanel;
import com.samratinfosys.myecollege.CollegeSelection;
import com.samratinfosys.myecollege.Login;
import com.samratinfosys.myecollege.MyFriends;
import com.samratinfosys.myecollege.UserPanel;
import com.samratinfosys.myecollege.json_classes.Account;
import com.samratinfosys.myecollege.tools.Database;
import com.samratinfosys.myecollege.tools.GeneralConstants;
import com.samratinfosys.myecollege.tools.JSONDownloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by iAmMegamohan on 12-04-2015.
 */
public class Helper {

    private static String DEVICE_ID=null;
    private static Database mainDatabase=null;

    public static int NETWORK_READ_TIMEOUT=10000;
    public static int NETWORK_CONNECT_TIMEOUT=10000;
    public static int NETWORK_READ_BUFFER=4096;

    public static Database getMainDatabase(Context context) {
        if(mainDatabase==null && context!=null)
            mainDatabase=new Database(context);
        return mainDatabase;
    }

    public static String getDeviceId(Context context) {
        if(DEVICE_ID==null && context!=null)
            DEVICE_ID=Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return DEVICE_ID;
    }

    public static AlertDialog.Builder showAlertDialog(Context context, String message) {
        AlertDialog.Builder dialog=new AlertDialog.Builder(context);
        dialog.setMessage(message);
        dialog.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return dialog;
    }

    public static AlertDialog.Builder showAlertDialog(Context context, String message, String positiveButton, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder dialog=new AlertDialog.Builder(context);
        dialog.setMessage(message);
        dialog.setPositiveButton(positiveButton,onClickListener);
        return dialog;
    }

    public static final String DATA_DIRECTORY="My-eCollege";
    public static final String COLLEGETIMELINE_DIRECTORY="College-Timelines";

    protected static long getLoggedAccountID(Context context) {
        try {
            File f=getFile(context, JSONDownloader.SESSION_FILE);
            if(f.exists() && f.isFile()) {
                String longString=new String(readFile(f));
                if(longString.trim().length()>0) {
                    return Long.parseLong(longString);
                }
            }
        } catch(Exception ex) {
            Helper.Log(ex,"getLoginSessionID");
        }

        return -1;
    }

    public static Account getLoggedAccount(Context context) {
        long accId=getLoggedAccountID(context);
        if(accId>0) {
            return getMainDatabase(context).getAccount(accId);
        }
        return null;
    }

    public static File getDataFolder(Context context) {
        return Environment.getExternalStorageDirectory();
    }

    public static byte[] readFile(File file) {
        byte[] bytes=new byte[0];
        FileInputStream fis= null;
        try {
            fis = new FileInputStream(file);
            bytes=new byte[fis.available()];
            fis.read(bytes);
        } catch (Exception e) {
            Log(e,"readFile: "+file);
        } finally {
            if(fis!=null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fis=null;
            }
        }
        return bytes;
    }

    public static boolean writeFile(File f, byte[] content) {
        try {
            if(f!=null && f.exists())
                f.delete();
            if(f.createNewFile()) {
                FileOutputStream fos=new FileOutputStream(f);
                fos.write(content);
                fos.flush();
                fos.close();
                return true;
            }
        } catch (Exception ex) {
            Log(ex,"writeFile: "+f);
        }
        return false;
    }

    public  static boolean validateDataFolder(Context context) {
        File dataFolder=new File(getDataFolder(context),"/"+DATA_DIRECTORY);
        if(dataFolder.exists() && dataFolder.isDirectory())
            return true;
        dataFolder.mkdirs();
        dataFolder=null;
        return validateDataFolder(context);
    }

    public static void Log(Exception ex,String msg) {
        System.out.println("----------------------------------------------------------------------------\n");
        System.out.println("Msg: "+msg);
        System.out.println("Exception: "+ex);
        System.out.println("\n----------------------------------------------------------------------------\n");
    }

    public static File getFile(Context context,String file) {
        File f=new File(getDataFolder(context),getFilePath(file));
        return f;
    }

    public static File getFile(Context context,String file, boolean mkdirs) {
        File f=getFile(context,file);
        if(mkdirs)
            f.mkdirs();
        return f;
    }

    public static File getFile(Context context,String folder,String file) {
        File f=new File(getDataFolder(context),getFilePath(folder,file));
        return f;
    }

    public static File getFile(Context context,String folder,String file, boolean mkdirs, boolean mkdirsLeaveFilename) {
        File f=getFile(context,folder,file);
        if(mkdirs) {
            if(mkdirsLeaveFilename) {
                getFile(context,folder,true);
            } else {
                f.mkdirs();
            }
        }
        return f;
    }


    public static String getFilePath(String filename) {

        if(filename.startsWith("/"+DATA_DIRECTORY+"/"))
            return filename;
        return "/"+DATA_DIRECTORY+"/"+filename;
    }

    public static String getFilePath(String folder, String filename) {
        return getFilePath(folder,filename,null,false);
    }

    public static String getFilePath(String folder, String filename, Context context, boolean mkdirs) {
        if(mkdirs && context!=null) {
            File f=getFile(context,folder);
            if(!f.exists())
                f.mkdirs();
        }
        return getFilePath(folder)+"/"+filename;
    }

    public static File getCollegeTimelineFile(Context context, long collegeId) {
        File clgTimelineFile=Helper.getFile(context,Helper.COLLEGETIMELINE_DIRECTORY,collegeId+".json",true,true);
        return clgTimelineFile;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    public static class Util {

        public static Bitmap getBitmapByUriFromMedia(Context context, Uri uri) {
            if(uri!=null) {
                try {
                    return MediaStore.Images.Media.getBitmap(context.getContentResolver(),uri);
                } catch (Exception ex) {
                    Helper.Log(ex,"getBitmapByUriFromMedia: "+uri);
                }
            }
            return null;
        }

        public static String getFilepathByUriFromMedia(Context context, Uri uri) {
            if(uri!=null) {
                try {
                    String[] columns=new String[] { MediaStore.Images.Media.DATA };
                    ContentResolver contentResolver=context.getContentResolver();
                    Cursor cursor=contentResolver.query(uri, columns, null,null,null);
                    if(cursor.moveToFirst()) {
                        int index=cursor.getColumnIndex(columns[0]);
                        String filepath=cursor.getString(index);

                        return filepath;
                    }
                } catch (Exception ex) {
                    Helper.Log(ex,"getFilepathByUriFromMedia: "+uri);
                }
            }
            return null;
        }

        public static boolean isEmail(String email) {
            try {
                int atIndex=email.indexOf("@");
                int dotIndex=email.indexOf(".", atIndex);
                if(dotIndex>-1) {
                    return email.trim().length()-dotIndex==4;
                }
            } catch (Exception ex) {

            }
            return false;
        }

        public static long toMiliseconds(Date date) {
            if(date!=null) {
                Calendar calendar=Calendar.getInstance();
                calendar.setTime(date);
                return calendar.getTimeInMillis();
            }
            return 0;
        }

        public static Date toDate(long milis) {
            Calendar calendar=Calendar.getInstance();
            calendar.setTimeInMillis(milis);
            return calendar.getTime();
        }

        public static Date toDate(int year, int month, int day) {
            Calendar calendar=Calendar.getInstance();
            calendar.set(year,month,day);
            return calendar.getTime();
        }

        public static String dateToSQLString(Date date) {
            if(date!=null) {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    return simpleDateFormat.format(date);
                } catch(Exception ex) {
                    Helper.Log(ex,"dateToSQLString: "+date);
                }
            }
            return null;
        }

        public static Date parseDate(String date, String format) {
            if(date==null) return null;

            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
                return simpleDateFormat.parse(date, new ParsePosition(0));
            } catch (Exception ex) {
                Helper.Log(ex,"parseDate: "+date+", "+format);
            }

            return null;
        }

        public static void SwipeRefresh_StartRefreshing(final SwipeRefreshLayout swipeRefreshLayout, final boolean isRefreshing) {
            if(swipeRefreshLayout!=null) {
                swipeRefreshLayout.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        swipeRefreshLayout.setRefreshing(isRefreshing);
                    }
                });
            }
        }
    }

    public static class Images {

        public final static double SCALE_FACTOR=0.75f;

        public static Bitmap scaleBitmap(Bitmap bitmap, int width, int height, boolean autoAdjustSize) {
            if(bitmap!=null) {
                int nWidth=width;
                int nHeight=height;
                if(width>0 && height>0) {
                    if(autoAdjustSize) {
                        int h=bitmap.getHeight();
                        int w=bitmap.getWidth();
                        if(w>h) {
                            nHeight = (int) (width * SCALE_FACTOR);
                        } else if(h>w) {
                            nWidth = (int) (height * SCALE_FACTOR);
                        }
                    }
                }
                return Bitmap.createScaledBitmap(bitmap,nWidth,nHeight,true);
            }
            return null;
        }

        public static Bitmap ReadBitmap(Context context,String file) {
            return ReadBitmap(context,file,-1,-1,false);
        }

        public static Bitmap ReadBitmap(Context context, byte[] imgBytes) {
            return ReadBitmap(context,imgBytes,0,0,false);
        }

        public static Bitmap ReadBitmap(Context context,String file,int width,int height,boolean autoAdjustSize) {
            return ReadBitmap(context, file, null, width,height,autoAdjustSize);
        }

        public static Bitmap ReadBitmap(Context context, File file,int width,int height,boolean autoAdjustSize) {
            return ReadBitmap(context, null,file, width,height,autoAdjustSize);
        }

        public static Bitmap ReadBitmap(Context context, String filename, File file,int width,int height,boolean autoAdjustSize) {
            Bitmap bmp=null;
            if(filename==null && file==null) return bmp;
            try {
                BitmapFactory.Options options=new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inDither = true;

                if(filename!=null)
                    BitmapFactory.decodeStream(context.openFileInput(filename),null,options);
                else if(file!=null)
                    BitmapFactory.decodeStream(new FileInputStream(file),null,options);

                if(width>0 && height>0) {
                    if(autoAdjustSize) {
                        int h=options.outHeight;
                        int w=options.outWidth;
                        if(w>h) {
                            height = (int) (width * SCALE_FACTOR);
                        } else if(h>w) {
                            width = (int) (height * SCALE_FACTOR);
                        }
                    }
                    //bmp=Bitmap.createScaledBitmap(bmp,width,height,true);
                }

                if(width>0 && height>0)
                    options.inSampleSize = calculateInSampleSize(options, width, height);
                options.inJustDecodeBounds = false;

                if(filename!=null)
                    bmp=BitmapFactory.decodeStream(context.openFileInput(filename),null,options);
                else if(file!=null)
                    bmp=BitmapFactory.decodeStream(new FileInputStream(file),null,options);

                return bmp;
            } catch (Exception ex) {
                Helper.Log(ex,"ReadBitmap:fromString+File "+filename+", "+file);
                return null;
            } finally {
                try {
                } catch(Exception ex) {}
            }
        }

        public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }

        public static Bitmap ReadBitmap(Context context,byte[] imagebytes,int width,int height,boolean autoAdjustSize){
            Bitmap bmp=null;
            try {
                BitmapFactory.Options options=new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inDither = true;

                //FileInputStream fis=new FileInputStream(outputFile);
                BitmapFactory.decodeByteArray(imagebytes, 0, imagebytes.length, options);

                if(width>0 && height>0) {
                    if(autoAdjustSize) {
                        int h=options.outHeight;
                        int w=options.outWidth;
                        if(w>h) {
                            height = (int) (width * 0.75);
                        } else if(h>w) {
                            width = (int) (height * 0.75);
                        }
                    }
                    //bmp=Bitmap.createScaledBitmap(bmp,width,height,true);
                }

                if(width>0 && height>0)
                    options.inSampleSize = calculateInSampleSize(options, width, height);
                options.inJustDecodeBounds = false;

                //fis.reset();
                //bmp=BitmapFactory.decodeStream(fis,null,options);
                bmp=BitmapFactory.decodeByteArray(imagebytes,0,imagebytes.length,options);
                //fis.close();

                return bmp;
            } catch (Exception ex) {
                Helper.Log(ex,"ReadImage:from bytes");
                return null;
            } finally {
                try {
                } catch(Exception ex) {}
            }
        }
    }

    public static class ActivityNavigators {

        public static void showMyFriends(Context context, int resultCode) {
            Intent intent=new Intent(context, MyFriends.class);
            ((ActionBarActivity)context).startActivityForResult(intent,resultCode);
        }

        public static void showCollegeSelectionActivity(Context context, boolean bootActivity) {
            Intent intent=new Intent(context, CollegeSelection.class);
            intent.putExtra(GeneralConstants.RQ_CODE_COLLEGESELECTION_IsFromBoot,bootActivity);
            context.startActivity(intent);
        }

        public static void showUserLoginActivity(Context context) {
            Intent intent=new Intent(context,Login.class);
            context.startActivity(intent);
        }

        public static void showCollegeInfo(Context context, long collegeId) {
            Intent intent=new Intent(context, CollegeInfo.class);
            intent.putExtra("collegeId",collegeId);
            context.startActivity(intent);
        }

        public static void showUserPanel(Context context) {
            Intent intent=new Intent(context, UserPanel.class);
            context.startActivity(intent);
        }

        public static void showUserLoginActivity(Context context, int resultCode) {
            Intent intent=new Intent(context,Login.class);
            ((ActionBarActivity)context).startActivityForResult(intent,resultCode);
        }

        public static void showCollegePanel(Context context, long collegeId) {
            Intent intent=new Intent(context,CollegePanel.class);
            intent.putExtra("collegeId",collegeId);
            context.startActivity(intent);
        }

        public static void showActivationActivity(Context context) {
            Intent intent=new Intent(context, Activation.class);
            context.startActivity(intent);
        }
    }

}
