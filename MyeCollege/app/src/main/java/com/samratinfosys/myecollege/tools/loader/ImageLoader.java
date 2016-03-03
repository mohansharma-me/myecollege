package com.samratinfosys.myecollege.tools.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.samratinfosys.myecollege.tools.DownloadManager;
import com.samratinfosys.myecollege.tools.GeneralConstants;
import com.samratinfosys.myecollege.utils.HashMaker;
import com.samratinfosys.myecollege.utils.Helper;

import java.io.File;

/**
 * Created by iAmMegamohan on 18-04-2015.
 */
public class ImageLoader implements DownloadManager.DownloadStatus {

    public enum ImageType {
        None,
        Full,
        Original,
        Icon,
        Thumb,
        Poster
    }

    /*******************************************************************/

    public static ImageLoader downloadImage(ImageView imageView, int imageID, String url, String queryString, IImageLoader iImageLoader) {
        return new ImageLoader(imageView,imageID,url,queryString,iImageLoader);
    }

    public static ImageLoader downloadCollegeLogo(ImageView imageView, long collegeId, ImageType imgType, int quality, IImageLoader iImageLoader) {
        String data="imgOf=college&imgId="+collegeId+"&imgType="+ImageTypeToString(imgType)+"&imgQuality="+quality;
        return downloadImage(imageView,(int)collegeId, GeneralConstants.API_IMAGES, data, iImageLoader);
    }

    public static ImageLoader downloadAccountAvtar(ImageView imageView, long accountId, ImageType imgType, int quality, IImageLoader iImageLoader) {
        String data="imgOf=account&imgId="+accountId+"&imgType="+ImageTypeToString(imgType)+"&imgQuality="+quality;
        return downloadImage(imageView,(int)accountId, GeneralConstants.API_IMAGES, data, iImageLoader);
    }

    public static ImageLoader downloadProfilePicture(ImageView imageView, ImageType imgType, int quality, IImageLoader iImageLoader) {
        String data="imgOf=profile&imgType="+ImageTypeToString(imgType)+"&imgQuality="+quality;
        return downloadImage(imageView,quality, GeneralConstants.API_IMAGES, data, iImageLoader);
    }

    public static ImageLoader downloadProfilePicture(ImageView imageView, long profileId, ImageType imgType, int quality, IImageLoader iImageLoader) {
        String data="imgOf=profile&imgId="+profileId+"&imgType="+ImageTypeToString(imgType)+"&imgQuality="+quality;
        return downloadImage(imageView, (int)profileId, GeneralConstants.API_IMAGES, data, iImageLoader);
    }

    public static boolean deleteCache(Context context,ImageLoader imageLoader) {
        if(imageLoader!=null) {
            String filename=imageLoader.getCacheFileName();
            if(filename!=null) {
                File imgFile=Helper.getFile(context,Helper.getFilePath("Images",filename));
                if(imgFile.exists())
                    return imgFile.delete();
            }
        }
        return false;
    }

    public static String ImageTypeToString(ImageType imageType) {

        if(imageType==ImageType.None)
            return "original";

        if(imageType==ImageType.Poster)
            return "poster";

        if(imageType==ImageType.Icon)
            return "icon";

        if(imageType==ImageType.Thumb)
            return "thumb";

        if(imageType==ImageType.Full)
            return "full";


        return "original";
    }

    /*******************************************************************/

    public interface IImageLoader {
        public void imageStatus(int imageId, int percentage);
        public void imageUpdated(int imageId, ImageLoader imageLoader);
    }

    private int imageId=-1;
    public String url=null, queryString=null;
    private IImageLoader iImageLoader=null;
    public String filename=null;
    public Bitmap bitmap=null;
    public boolean success=false, failed=false;
    public ImageView imageView=null;
    public boolean downloadAgain=false;

    public DownloadManager.RealDownloader realDownloader=null;

    private ImageLoader(ImageView imageView,int imgId, String url, String queryString, IImageLoader iImageLoader, boolean downloadAgain, boolean immediateStart) {
        this.imageId=imgId;
        this.url=url;
        this.queryString=queryString;
        this.imageView=imageView;
        this.iImageLoader=iImageLoader;
        this.filename=getCacheFileName();
        this.downloadAgain=downloadAgain;
        success=failed=false;

        if(immediateStart) {
            startLoader();
        }
    }

    private ImageLoader(ImageView imageView,int imgId, String url, String queryString, IImageLoader iImageLoader, boolean downloadAgain) {
        this(imageView,imgId,url,queryString,iImageLoader,downloadAgain, false);
    }

    private ImageLoader(ImageView imageView,int imgId, String url, String queryString, IImageLoader iImageLoader) {
        this(imageView,imgId,url,queryString,iImageLoader, false, false);
    }

    public String getCacheFileName()
    {
        if(url!=null && queryString!=null)
            return HashMaker.SHA1(url+"?"+queryString);
        return null;
    }


    public void startLoader() {
        if(imageView!=null) {
            String tempRealPath = Helper.getFilePath("Images", filename, imageView.getContext(), true);

            File imgFile=Helper.getFile(imageView.getContext(),Helper.getFilePath("Images",filename));
            if(!downloadAgain && imgFile.exists()) {
                downloadDone(imageId,imgFile);
            } else {
                if (this.url != null && this.filename != null) {
                    realDownloader=DownloadManager.DownloadTo(imageId, imageView.getContext(), this.url, queryString, tempRealPath, this);
                } else {
                    failed=true;
                    if (iImageLoader != null) {
                        iImageLoader.imageUpdated(imageId, this);
                    }
                }
            }
        } else {
            failed=true;
            iImageLoader.imageUpdated(imageId, this);
        }
    }

    @Override
    public void DownloadStarted(int downloadId) {
        if(iImageLoader!=null) {
            iImageLoader.imageStatus(downloadId,0);
        }
    }

    @Override
    public void DownloadFailed(int downloadId, Exception ex, int httpResponseCode) {
        failed=true;
        if(iImageLoader!=null) {
            iImageLoader.imageUpdated(downloadId,this);
        }
    }

    @Override
    public void DownloadProgress(int downloadId, long percentage, long downloadedBytes, long totalBytes) {
        if(iImageLoader!=null) {
            iImageLoader.imageStatus(downloadId,(int)percentage);
        }
    }

    @Override
    public void DownloadCompleted(int downloadId, byte[] data, String filepath, boolean isCancelled) {
        if(iImageLoader!=null && filepath!=null && imageView!=null) {
            File imgFile=Helper.getFile(imageView.getContext(),Helper.getFilePath("Images",filename));
            downloadDone(downloadId,imgFile);
        }
    }

    private void downloadDone(int downloadId, File imgFile) {
        success=true;
        bitmap=Helper.Images.ReadBitmap(imageView.getContext(),imgFile,0,0,false);
        iImageLoader.imageUpdated(downloadId,this);
    }
}
