package com.samratinfosys.myecollege;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.samratinfosys.myecollege.json_classes.Account;
import com.samratinfosys.myecollege.json_classes.College;
import com.samratinfosys.myecollege.json_classes.MyJSON;
import com.samratinfosys.myecollege.tools.DownloadManager;
import com.samratinfosys.myecollege.tools.GeneralConstants;
import com.samratinfosys.myecollege.tools.loader.ImageLoader;
import com.samratinfosys.myecollege.tools.JSONDownloader;
import com.samratinfosys.myecollege.tools.JSONHelper;
import com.samratinfosys.myecollege.tools.MyListAdapter;
import com.samratinfosys.myecollege.utils.Helper;

import java.io.File;
import java.util.List;


public class CollegeSelection extends ActionBarActivity implements DownloadManager.DownloadStatus, SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swipeRefreshLayout=null;

    private ListView lvColleges=null;
    private LinearLayout llHolderUserOptions=null;
    private LinearLayout llHolderUserCollege=null;

    private LinearLayout btnUserProfile=null;
    private LinearLayout btnCollegeProfile=null;

    private ImageView imgUserProfilePic=null;
    private ImageView imgCollegeLogo=null;
    private TextView lblAccountName=null;
    private TextView lblCollegeName=null;

    private ProgressBar pbGeneral=null;

    private Context context;

    //private LoginSession loginSession=null;
    private Account loggedAccount=null;

    private final int REFRESH_COLLEGE=1;
    private final int VERIFY_LOGINSESSION=2;

    private boolean workFetchColleges=false;
    private boolean workValidateIdentity=false;

    private ProgressDialog progressDialog;

    private boolean isItFromBoot=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_college_selection);

        context=this;
        //progressDialog=new ProgressDialog(this);

        progressDialog=progressDialog.show(this,null,"Loading...");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        initComponents();
        initSwipeEvents();
        initClicks();
        initLvColleges();
        progressDialog.dismiss();

        onRefresh(false);
    }

    private void initComponents() {
        Intent intent=getIntent();
        if(intent!=null) {
            isItFromBoot=intent.getBooleanExtra(GeneralConstants.RQ_CODE_COLLEGESELECTION_IsFromBoot,isItFromBoot);
        }

        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_container);

        lvColleges=(ListView)findViewById(R.id.lvColleges);
        llHolderUserCollege=(LinearLayout)findViewById(R.id.sessionControl_holderUserCollege);
        llHolderUserOptions=(LinearLayout)findViewById(R.id.sessionControl_holderUserOptions);

        imgCollegeLogo=(ImageView)findViewById(R.id.imgCollegeLogo);
        imgUserProfilePic=(ImageView)findViewById(R.id.imgUserProfile);
        lblAccountName=(TextView)findViewById(R.id.lblAccountName);
        lblCollegeName=(TextView)findViewById(R.id.lblCollegeName);

        btnUserProfile=(LinearLayout)findViewById(R.id.sessionControl_btnUserProfile);
        btnCollegeProfile=(LinearLayout)findViewById(R.id.sessionControl_btnUserCollege);

        pbGeneral=(ProgressBar)findViewById(R.id.pbGeneral);
        //progressDialog=new ProgressDialog(this);
    }

    private void updateSessionControls() {
        if(loggedAccount==null) return;

        lblAccountName.setText(loggedAccount.name);
        imgUserProfilePic.setImageDrawable(getResources().getDrawable(R.drawable.hourglass));
        ImageLoader.downloadAccountAvtar(imgUserProfilePic,loggedAccount.id, ImageLoader.ImageType.Thumb,9,new ImageLoader.IImageLoader() {
            @Override
            public void imageStatus(int imageId, int percentage) {

            }

            @Override
            public void imageUpdated(int imageId, ImageLoader imageLoader) {
                if(imageLoader.success && imageLoader.bitmap!=null) {
                    imgUserProfilePic.setImageBitmap(imageLoader.bitmap);
                }
            }
        }).startLoader();

        College college=Helper.getMainDatabase(this).getCollege(loggedAccount.college_id);
        if(college!=null) {
            lblCollegeName.setText(college.short_name);
            imgCollegeLogo.setImageDrawable(getResources().getDrawable(R.drawable.hourglass));
            ImageLoader.downloadCollegeLogo(imgCollegeLogo,loggedAccount.college_id, ImageLoader.ImageType.Thumb,9,new ImageLoader.IImageLoader() {
                @Override
                public void imageStatus(int imageId, int percentage) {

                }

                @Override
                public void imageUpdated(int imageId, ImageLoader imageLoader) {
                    if(imageLoader.success && imageLoader.bitmap!=null) {
                        imgCollegeLogo.setImageBitmap(imageLoader.bitmap);
                    }
                }
            }).startLoader();
        }

        toggleUserCollegeOptions(true);
    }

    private void validateLoginSession() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                loggedAccount=Helper.getLoggedAccount(context);
                boolean showLoginForm=true;

                if(loggedAccount!=null) {
                    //more validation may required
                    showLoginForm=false;
                }

                if(showLoginForm) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleUserCollegeOptions(false);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateSessionControls();
                        }
                    });
                    if(Helper.isNetworkConnected(context)) {
                        JSONDownloader.VerifyDevice(VERIFY_LOGINSESSION,loggedAccount,context,(DownloadManager.DownloadStatus)context);
                    }
                }
            }
        }).start();
    }

    private void toggleUserCollegeOptions(boolean showUserCollege) {
        if(showUserCollege) {
            llHolderUserCollege.setVisibility(View.VISIBLE);
            llHolderUserOptions.setVisibility(View.GONE);
        } else {
            llHolderUserCollege.setVisibility(View.GONE);
            llHolderUserOptions.setVisibility(View.VISIBLE);
        }
        invalidateOptionsMenu();
    }

    private void togggleCollegeListView(boolean showColleges) {
        if(showColleges) {
            lvColleges.setVisibility(View.VISIBLE);
            pbGeneral.setVisibility(View.GONE);
        } else {
            lvColleges.setVisibility(View.GONE);
            pbGeneral.setVisibility(View.VISIBLE);
        }
    }

    private void initLvColleges() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<College> colleges=Helper.getMainDatabase(context).getColleges();
                if(colleges.size()==0) {
                    JSONDownloader.GetColleges(REFRESH_COLLEGE,context,(DownloadManager.DownloadStatus)context);
                } else {
                    final MyListAdapter mlaCollegeAdapter = new MyListAdapter(context , R.layout.listview_item1);
                    mlaCollegeAdapter.setAdapterFor(MyListAdapter.AdapterFor.COLLEGES, colleges);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lvColleges.setAdapter(mlaCollegeAdapter);
                            togggleCollegeListView(true);
                        }
                    });
                }
            }
        }).start();
        togggleCollegeListView(false);
    }

    private void initClicks() {

        lvColleges.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVertPos=(lvColleges==null||lvColleges.getChildCount()==0)?0:lvColleges.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem==0 && topRowVertPos>=0);
            }
        });

        llHolderUserOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.ActivityNavigators.showUserLoginActivity(v.getContext(), GeneralConstants.RESULT_FROM_LOGIN);
            }
        });

        btnUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show user activity
                Helper.ActivityNavigators.showUserPanel(context);
            }
        });

        btnCollegeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show college activity
                if(loggedAccount!=null) {
                    Helper.ActivityNavigators.showCollegePanel(context, loggedAccount.college_id);
                } else {
                    Toast.makeText(context,"Oops, something goes unexpected, please try again.",Toast.LENGTH_LONG).show();
                }
            }
        });


        lvColleges.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyListAdapter mla=(MyListAdapter)lvColleges.getAdapter();

                if(position>-1 && position<mla.getCount()) {
                    College clg=(College)mla.getItem(position);
                    if(clg!=null) {
                        long selId=clg.id;
                        clg=null;
                        Helper.ActivityNavigators.showCollegePanel(context, selId);
                    }
                }

            }
        });
    }

    private void initSwipeEvents() {
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        onRefresh(true);
    }

    public void onRefresh(boolean forceToDownloadColleges) {
        if(forceToDownloadColleges) {
            JSONDownloader.GetColleges(REFRESH_COLLEGE,this,this);
        }
        validateLoginSession();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopRefreshing();
            }
        },2000);
    }

    private boolean isWorking() {
        return workFetchColleges || workValidateIdentity;
    }

    public void stopRefreshing() {
        if(!isWorking()) {
            swipeRefreshLayout.setRefreshing(false);
            if(progressDialog.isShowing())
                progressDialog.dismiss();
        }
        isItFromBoot=false;
        TextView lblSwipeDownNote=(TextView)findViewById(R.id.lblSwipeDownNote);
        if(lblSwipeDownNote!=null)
            lblSwipeDownNote.setVisibility(View.GONE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==GeneralConstants.RESULT_FROM_LOGIN && data!=null) {
            boolean validateNow=data.getBooleanExtra("validateNow",false);

            if(validateNow) {
                validateLoginSession();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_college_selection, menu);

        if(llHolderUserCollege.getVisibility()==View.VISIBLE) {
            MenuItem mi=menu.add("Logout");
            try {
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            } catch(Exception ex) {}
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.refresh_colleges) {
            if(!swipeRefreshLayout.isRefreshing()) {
                onRefresh(true);
            }
            return true;
        } else if(item.getTitle().toString().trim().toLowerCase().equals("logout")) {
            deleteLoginSessionAndReload();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void DownloadStarted(int downloadId) {
        if(downloadId==REFRESH_COLLEGE) {
            //toggleLvColleges(false);
            /*progressDialog.setMessage("Preparing colleges...");
            progressDialog.setCancelable(false);
            progressDialog.show();*/
            workFetchColleges=true;
        }

        if(downloadId==VERIFY_LOGINSESSION) {
            /*progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();*/
            workValidateIdentity=true;
        }

        if(isWorking() && !swipeRefreshLayout.isRefreshing()) {
            Helper.Util.SwipeRefresh_StartRefreshing(swipeRefreshLayout, true);
        }
    }

    @Override
    public void DownloadFailed(final int downloadId, Exception ex, int httpResponseCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                if(downloadId==REFRESH_COLLEGE) {
                    workFetchColleges=false;
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    if (Helper.isNetworkConnected(context)) {
                        dialog.setMessage("Unable to refresh colleges, please try again.");
                    } else {
                        dialog.setMessage("No Internet Connection!!");
                    }
                    dialog.show();
                }

                if(downloadId==VERIFY_LOGINSESSION) {
                    workValidateIdentity=false;
                    //showFailedVerificationDialog("Error occured while verifying your identity over server.");
                }

                stopRefreshing();
            }
        });
    }

    private void showFailedVerificationDialog(String message,boolean showIgnore) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);

        dialog.setTitle("Verification failed");
        if (Helper.isNetworkConnected(context)) {
            dialog.setMessage(message);
        } else {
            dialog.setMessage("No Internet Connection!!");
        }

        dialog.setNeutralButton("Retry",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                dialog.cancel();
                validateLoginSession();
            }
        });

        if(showIgnore) {
            dialog.setPositiveButton("Ignore", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    dialog.cancel();
                    updateSessionControls();
                }
            });
        }

        dialog.setNegativeButton("Logout",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                dialog.cancel();
                deleteLoginSessionAndReload();
            }
        });

        dialog.show();
    }

    private void deleteLoginSessionAndReload() {
        JSONDownloader.DeleteLoginSession(context);
        loggedAccount=null;
        validateLoginSession();
    }

    @Override
    public void DownloadProgress(int downloadId, long percentage, long downloadedBytes, long totalBytes) {

    }

    @Override
    public void DownloadCompleted(int downloadId, byte[] data, String filepath, boolean isCancelled) {
        if(downloadId==REFRESH_COLLEGE) {
            workFetchColleges=false;

            if(!isCancelled && filepath!=null) {
                progressDialog.setMessage("Preparing college list...");
                progressDialog.show();
                Thread thread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Helper.getMainDatabase(context).clearColleges();
                        Helper.getMainDatabase(context).addColleges(JSONHelper.getColleges(context));
                        File collegeFile=Helper.getFile(context,JSONDownloader.COLLEGE_FILE);
                        if(collegeFile.exists())
                            collegeFile.delete();
                        progressDialog.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initLvColleges();
                            }
                        });
                    }
                });
                thread.start();
            }

        }

        if(downloadId==VERIFY_LOGINSESSION) {
            workValidateIdentity=false;
            if(!isCancelled && data!=null) {
                MyJSON json=new MyJSON(new String(data));
                if(json.isReady()) {
                    if(!json.success || loggedAccount==null) {
                        showFailedVerificationDialog("Sorry, your authentication failed, please login again.",false);
                    } else {
                        // verified
                        Toast.makeText(context,"Successfully verified.",Toast.LENGTH_SHORT).show();
                        //updateSessionControls();
                    }
                } else {
                    //showFailedVerificationDialog("Server gives invalid response while validating your identity.");
                }
            } else {
                //showFailedVerificationDialog("Error occured while verifying your identity over server.");
            }
        }

        stopRefreshing();
    }

}
