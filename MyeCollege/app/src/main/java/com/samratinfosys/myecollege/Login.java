package com.samratinfosys.myecollege;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.samratinfosys.myecollege.json_classes.Account;
import com.samratinfosys.myecollege.json_classes.MyJSON;
import com.samratinfosys.myecollege.tools.GeneralConstants;
import com.samratinfosys.myecollege.tools.JSONDownloader;
import com.samratinfosys.myecollege.tools.loader.JSONLoader;
import com.samratinfosys.myecollege.utils.Helper;


public class Login extends ActionBarActivity implements JSONLoader.IJSONLoader {

    private Context context;

    private ProgressBar pbLogin=null;
    private LinearLayout formLogin=null;

    private EditText txtUserID=null;
    private EditText txtUserPassword=null;

    private Button btnLogin=null;
    private Button btnForgotPassword=null;
    private Button btnActivation=null;

    private TextView lblProcess=null;

    private final int DOWNLOAD_LOGIN=0;
    private final int FORGOT_PASSWORD=1;

    private AlertDialog alertDialog;
    private AlertDialog.Builder adBuilder;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context=this;

        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setSubtitle("My eCollege");

        initComponents();
        initClicks();

        // send back it if already logged.
    }

    private void initComponents() {
        lblProcess=(TextView)findViewById(R.id.lblProcess);
        pbLogin=(ProgressBar)findViewById(R.id.pbLogin);
        formLogin=(LinearLayout)findViewById(R.id.formLogin);
        txtUserID=(EditText)findViewById(R.id.txtUserID);
        txtUserPassword=(EditText)findViewById(R.id.txtUserPassword);
        btnLogin=(Button)findViewById(R.id.btnLogin);
        btnForgotPassword=(Button)findViewById(R.id.btnForgotPassword);
        btnActivation=(Button)findViewById(R.id.btnActivation);

        progressDialog=new ProgressDialog(this);
    }

    private void initClicks(){
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId=txtUserID.getText().toString().trim();
                String userPassword=txtUserPassword.getText().toString().trim();
                if(userId.length()==0) {
                    txtUserID.requestFocus();
                    txtUserID.setError("Enter valid User ID.");
                } else if(userPassword.length()==0) {
                    txtUserPassword.requestFocus();
                    txtUserPassword.setError("Enter valid User Password.");
                } else {
                    //JSONDownloader.Login(DOWNLOAD_LOGIN, userId, userPassword, context, (DownloadManager.DownloadStatus) context);
                    JSONLoader.loadLoginSession(context,DOWNLOAD_LOGIN,userId,userPassword,(JSONLoader.IJSONLoader)context);
                    //progressDialog.setTitle("Logging");
                    progressDialog.setMessage("Verifying...");
                    progressDialog.setCancelable(false);
                    //progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    //progressDialog.setMax(100);
                    //progressDialog.setProgress(0);
                    progressDialog.show();
                }
            }
        });

        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId=txtUserID.getText().toString().trim();

                if(userId.length()==0) {
                    txtUserID.requestFocus();
                    txtUserID.setError("Enter User ID to get forgotten password!!");
                } else {
                    //JSONDownloader.ForgotPassword(FORGOT_PASSWORD,userId, context,(DownloadManager.DownloadStatus)context);
                    JSONLoader.loadForgotPasswordAPI(context,FORGOT_PASSWORD,userId,(JSONLoader.IJSONLoader)context);
                    progressDialog.setMessage("Sending password...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }

            }
        });

        btnActivation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.ActivityNavigators.showActivationActivity(v.getContext());
            }
        });
    }

    @Override
    public void jsonStatus(int jsonId, int percentage) {
        //if(jsonId==DOWNLOAD_LOGIN) {
            //progressDialog.setProgress(percentage);
        //}
    }

    @Override
    public void jsonUpdated(int jsonId, JSONLoader jsonLoader) {
        if(jsonId==DOWNLOAD_LOGIN) {
            validateLoginAccess(jsonLoader);
        } else if(jsonId==FORGOT_PASSWORD) {
            validateForgotPassword(jsonLoader);
        }

        progressDialog.dismiss();
    }

    private void validateForgotPassword(JSONLoader jsonLoader) {
        String message="";
        if(jsonLoader.failed) {
            message="There is problem in Internet Connectivity.";
        } else {

            MyJSON json=jsonLoader.myJSON;
            if(json!=null && json.success) {
                message=json.success_message==null?"Password is sent to your e-mail address.":json.success_message;
            } else {
                if(json!=null && json.error_message!=null) {
                    message=json.error_message;
                } else {
                    message="Oops, unexpected error occured, please try again.";
                }
            }

        }

        Helper.showAlertDialog(context,message).show();
    }

    private void validateLoginAccess(JSONLoader jsonLoader) {
        if(jsonLoader.success && jsonLoader.myJSON!=null && jsonLoader.myJSON.isReady()) {
            if(jsonLoader.myJSON.success) {
                String userData=jsonLoader.myJSON.getString("userData");
                if(userData!=null) {
                    jsonLoader.myJSON=new MyJSON(userData);
                    if(jsonLoader.myJSON.isReady()) {
                        Account account=new Account(jsonLoader.myJSON);
                        if(account.ready) {
                            Helper.getMainDatabase(this).updateOrAddAccount(account);
                            String sessionData=account.id+"";
                            Helper.writeFile(Helper.getFile(this,JSONDownloader.SESSION_FILE), sessionData.getBytes());
                            Intent intent = new Intent();
                            intent.putExtra("validateNow", true);
                            setResult(GeneralConstants.RESULT_FROM_LOGIN, intent);
                            finish();
                        } else {
                            Helper.showAlertDialog(context,"Sorry, unable to initialize account properties.\nPlease try again.").show();
                        }
                    } else {
                        Helper.showAlertDialog(context,"Sorry, invalid response.\nPlease try again.").show();
                    }
                } else {
                    Helper.showAlertDialog(context,"Sorry, your authentication request is rejected.\nPlease try again.").show();
                }
            } else {
                //progressDialog.dismiss();
                String errorFor=jsonLoader.myJSON.getString("errorFor");
                if(errorFor==null) {
                    //no success
                    String message=jsonLoader.myJSON.error_message==null?"Oops, Unexpected error occured.":jsonLoader.myJSON.error_message;
                    Helper.showAlertDialog(context,message).show();
                } else {
                    if(errorFor.equals("userId")) {
                        //invalid user id
                        txtUserID.requestFocus();
                        txtUserID.setError(jsonLoader.myJSON.error_message==null?"Wrong User ID.":jsonLoader.myJSON.error_message);
                    } else if(errorFor.equals("userPassword")) {
                        //invalid user password
                        txtUserPassword.requestFocus();
                        txtUserPassword.setError(jsonLoader.myJSON.error_message==null?"Wrong User Password.":jsonLoader.myJSON.error_message);
                    } else if(errorFor.equals("noactivation")) {
                        //not activated
                        String message=jsonLoader.myJSON.error_message==null?"Your account isn't activated.\nKindly please activate your account by clicking 'Didn't have password ?' button in Login Screen.":jsonLoader.myJSON.error_message;
                        Helper.showAlertDialog(context,message).show();
                    } else if(errorFor.equals("deactivated")) {
                        //de-activated
                        String message=jsonLoader.myJSON.error_message==null?"Ohh, you account is de-activated by administrator.\nPlease contact to your department for further details.":jsonLoader.myJSON.error_message;
                        Helper.showAlertDialog(context,message).show();
                    } else {
                        //no success
                        String message=jsonLoader.myJSON.error_message==null?"Oops, Unexpected error occured.":jsonLoader.myJSON.error_message;
                        Helper.showAlertDialog(context,message).show();
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }

}
