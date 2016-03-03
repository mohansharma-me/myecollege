package com.samratinfosys.myecollege.tools;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;

import com.samratinfosys.myecollege.json_classes.College;
import com.samratinfosys.myecollege.utils.Helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by iAmMegamohan on 12-04-2015.
 */
public class DownloadManager {

    public static String bytesName=null;
    public static byte[] bytesToUpload=null;

    public interface DownloadStatus {
        public void DownloadStarted(int downloadId);
        public void DownloadFailed(int downloadId,Exception ex, int httpResponseCode);
        public void DownloadProgress(int downloadId,long percentage, long downloadedBytes, long totalBytes);
        public void DownloadCompleted(int downloadId,byte[] data, String filepath, boolean isCancelled);
    }

    private static Context mainContext;

    private DownloadManager() {}

    public static RealDownloader Download(int id,Context context,String link, DownloadStatus downloadStatus) {
        mainContext=context;
        RealDownloader rd=new RealDownloader(id,downloadStatus);
        rd.execute(link);
        return rd;
    }

    public static RealDownloader Download(int id,Context context,String link,String queryString, DownloadStatus downloadStatus) {
        mainContext=context;
        RealDownloader rd=new RealDownloader(id,downloadStatus);
        rd.execute(link,null,queryString);
        return rd;
    }

    public static RealDownloader DownloadTo(int id,Context context,String link, String filepath, DownloadStatus downloadStatus) {
        mainContext=context;
        RealDownloader rd=new RealDownloader(id,downloadStatus);
        rd.execute(link,filepath);
        return rd;
    }

    public static RealDownloader DownloadTo(int id,Context context,String link, String queryString, String filepath, DownloadStatus downloadStatus) {
        mainContext=context;
        RealDownloader rd=new RealDownloader(id,downloadStatus);
        rd.execute(link,filepath,queryString);
        return rd;
    }

    public static class RealDownloader extends AsyncTask<String, Long, byte[]> {

        private DownloadStatus downloadStatus;

        private boolean _isCancelled=false;

        public int Status=0;

        public String filepath=null;

        private int downloadId=0, httpResponseCode=-1;

        private Exception exception=null;

        private PowerManager.WakeLock wakeLock;

        public RealDownloader(int id,DownloadStatus downloadStatus) {
            this.downloadId=id;
            this.downloadStatus=downloadStatus;
        }

        public boolean CancelDownload() {
            _isCancelled=true;
            if(Status==0)
                return cancel(true);
            else
                return true; // that means download is already completed or failed.
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            _isCancelled=true;
            if(downloadStatus!=null) {
                downloadStatus.DownloadCompleted(downloadId,null,filepath,true);
            }
            //Status=-1;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Status=0;
            PowerManager pm=(PowerManager)mainContext.getSystemService(Context.POWER_SERVICE);
            wakeLock=pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,getClass().getName());
            wakeLock.acquire();
            if(downloadStatus!=null)
                downloadStatus.DownloadStarted(downloadId);
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            wakeLock.release();
            if(downloadStatus!=null) {
                if(Status==0) {
                    downloadStatus.DownloadCompleted(downloadId, bytes, filepath, _isCancelled);
                }
                else if(Status==-3) {
                    downloadStatus.DownloadFailed(downloadId,null,httpResponseCode);
                }
                else if(Status==-2) {
                    downloadStatus.DownloadFailed(downloadId,exception,httpResponseCode);
                }
                else if(Status==-4) {
                    downloadStatus.DownloadCompleted(downloadId,null,filepath,true);
                }
            }
            //Status=1;
        }

        @Override
        protected void onCancelled(byte[] bytes) {
            //super.onCancelled(bytes);
            _isCancelled=true;
            if(downloadStatus!=null) {
                downloadStatus.DownloadCompleted(downloadId,bytes,filepath,true);
            }
            Status=-4;
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);

            if(downloadStatus!=null) {
                downloadStatus.DownloadProgress(downloadId,values[0],values[1],values[2]);
            }
        }

        protected byte[] doInBackground(String... params) {
            byte retObj[]=null;

            HttpURLConnection httpURLConnection=null;
            InputStream inputStream=null;
            ByteArrayOutputStream bos=null;
            FileOutputStream fos=null;

            try {
                URL url=new URL(params[0]);
                httpURLConnection=(HttpURLConnection)url.openConnection();

                httpURLConnection.setReadTimeout(Helper.NETWORK_READ_TIMEOUT /* milliseconds */);
                httpURLConnection.setConnectTimeout(Helper.NETWORK_CONNECT_TIMEOUT /* milliseconds */);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                String outputBytes="deviceId="+Helper.getDeviceId(mainContext);
                if(params.length>=3) {
                    outputBytes+="&"+params[2];
                }

                httpURLConnection.getOutputStream().write(outputBytes.getBytes());

                httpURLConnection.connect();
                httpResponseCode=httpURLConnection.getResponseCode();
                if(httpResponseCode!=HttpURLConnection.HTTP_OK) {
                    Status=-3;
                } else {
                    long contentLength=httpURLConnection.getContentLength();
                    inputStream=httpURLConnection.getInputStream();

                    boolean isBinaryStream=params.length==1 || (params.length>1 && params[1]==null);
                    boolean isFileStream=params.length>=2 && params[1]!=null;

                    if(isBinaryStream) {
                        bos=new ByteArrayOutputStream();
                    } else if(isFileStream) {
                        filepath=params[1];
                        File f= Helper.getFile(mainContext,filepath);
                        if(f.exists() && f.isFile())
                            f.delete();
                        if(!f.createNewFile()) {
                            throw new Exception("File access error for "+f.getPath());
                        }
                        fos=new FileOutputStream(f);
                    }

                    byte data[]=new byte[Helper.NETWORK_READ_BUFFER];
                    long total=0;
                    int count=0;
                    while((count=inputStream.read(data))!=-1) {
                        if(_isCancelled) {
                            Status=-4;
                            break;
                        }
                        total+=count;
                        if(contentLength>-1) {
                            publishProgress((long)(total*100/contentLength),total,contentLength);
                        }

                        if(bos!=null)
                            bos.write(data,0,count);
                        if(fos!=null)
                            fos.write(data,0,count);
                    }

                    if(bos!=null) {
                        bos.flush();
                        retObj=bos.toByteArray();
                        bos.close();
                    }

                    if(fos!=null) {
                        fos.flush();
                        fos.close();
                    }
                }
            } catch(Exception ex) {
                exception=ex;
                Status=-2;
            }
            finally {
                if(fos!=null) {
                    try {
                        fos.close();
                    } catch(Exception ex) {}
                    fos=null;
                }
                if(bos!=null) {
                    try {
                        bos.close();
                    } catch(Exception ex) {}
                    bos=null;
                }
                if(inputStream!=null) {
                    try {
                        inputStream.close();
                    } catch(Exception ex) {}
                    inputStream=null;
                }
                if(httpURLConnection!=null) {
                    httpURLConnection.disconnect();
                    httpURLConnection=null;
                }
            }

            return retObj;
        }
    }
}
