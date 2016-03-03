package com.samratinfosys.myecollege;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.samratinfosys.myecollege.json_classes.MyJSON;
import com.samratinfosys.myecollege.tools.DownloadManager;
import com.samratinfosys.myecollege.tools.JSONDownloader;
import com.samratinfosys.myecollege.tools.JSONHelper;
import com.samratinfosys.myecollege.utils.Helper;

import java.net.HttpURLConnection;


public class Activation extends ActionBarActivity implements DownloadManager.DownloadStatus {

    private Context context;

    private EditText txtUserID=null;
    private Button btnRequestCode=null;
    private EditText txtActivationCode=null;
    private Button btnActivateNow=null;

    private ProgressDialog dialog;
    private AlertDialog.Builder dialog1;

    private final int REQUEST_CODE=1;
    private final int ACTIVATE_NOW=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation);

        context=this;

        getSupportActionBar().setTitle("Activation");
        getSupportActionBar().setSubtitle("Activate your account - My eCollege");

        initComponents();
    }

    private void initComponents() {
        txtUserID=(EditText)findViewById(R.id.txtUserID);
        btnRequestCode=(Button)findViewById(R.id.btnRequestCode);

        txtActivationCode=(EditText)findViewById(R.id.txtActivationCode);
        btnActivateNow=(Button)findViewById(R.id.btnActivateNow);

        dialog=new ProgressDialog(this);
        dialog1=new AlertDialog.Builder(this);
    }

    public void requestCode(View v) {
        String userId=txtUserID.getText().toString().trim();

        if(userId.length()==0) {
            txtUserID.setError("Invalid User ID!");
        } else {
            JSONDownloader.RequestCode(REQUEST_CODE,userId,this,this);
        }
    }

    public void activateNow(View v) {
        String activationCode = txtActivationCode.getText().toString().trim();

        if (activationCode.length() == 0) {
            txtActivationCode.setError("Invalid Code!");
        } else {
            JSONDownloader.ActivateNow(ACTIVATE_NOW, activationCode, this, this);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void DownloadStarted(int downloadId) {
        if(downloadId==REQUEST_CODE) {
            dialog.setMessage("Sending Activation Code...");
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    @Override
    public void DownloadFailed(int downloadId, final Exception ex, final int httpResponseCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();

                if(Helper.isNetworkConnected(context)) {
                    if (ex != null) {
                        dialog1.setMessage("Unexpected error occured,\n"+ex.getMessage());
                    } else {
                        if (httpResponseCode == HttpURLConnection.HTTP_OK) {
                            dialog1.setMessage("Server sent invalid response, please try again.");
                        } else {
                            dialog1.setMessage("Server not responding, please try again.");
                        }
                    }
                } else {
                    dialog1.setMessage("No Internet Connection!");
                }

                dialog1.setCancelable(true);
                dialog1.show();
            }
        });
    }

    @Override
    public void DownloadProgress(int downloadId, long percentage, long downloadedBytes, long totalBytes) {

    }

    @Override
    public void DownloadCompleted(int downloadId, byte[] data, String filepath, boolean isCancelled) {
        dialog.dismiss();
        if(isCancelled) {
            dialog1.setMessage("Process interuptted, please try again.");
        } else if(data==null) {
            dialog1.setMessage("There is problem in Internet Connectivity.");
        } else {
            MyJSON json= new MyJSON(new String(data));
            if(json.success) {
                if(downloadId==REQUEST_CODE)
                    dialog1.setMessage(json.success_message==null?"Activation code successfully sent.":json.success_message);
                if(downloadId==ACTIVATE_NOW)
                    dialog1.setMessage(json.success_message==null?"Account successfully activated.":json.success_message);
            } else {
                if(json.error_message!=null) {
                    dialog1.setMessage(json.error_message);
                } else {
                    dialog1.setMessage("Oops, unexpected error occured, please try again.");
                }
            }
        }

        dialog1.show();
    }
}
