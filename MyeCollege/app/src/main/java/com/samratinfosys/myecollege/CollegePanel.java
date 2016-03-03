package com.samratinfosys.myecollege;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.samratinfosys.myecollege.json_classes.College;
import com.samratinfosys.myecollege.json_classes.MyJSON;
import com.samratinfosys.myecollege.json_classes.Timeline;
import com.samratinfosys.myecollege.tools.loader.ImageLoader;
import com.samratinfosys.myecollege.tools.JSONHelper;
import com.samratinfosys.myecollege.tools.loader.JSONLoader;
import com.samratinfosys.myecollege.tools.MyListAdapter;
import com.samratinfosys.myecollege.utils.Helper;

import org.json.JSONArray;

import java.util.List;


public class CollegePanel extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {

    private Context context;

    private SwipeRefreshLayout swipeRefreshLayout=null;

    private ProgressDialog progressDialog;

    private RelativeLayout holderLoader=null;
    private LinearLayout holderCollegeProfile=null;

    private ImageView imgBigPicture=null;
    private RelativeLayout holderTimelineLoader =null;

    private TextView lblCollegeName=null;
    private TextView lblCollegeEmail=null;
    private TextView lblCollegeFax=null;
    private TextView lblCollegeTelephone=null;
    private TextView lblCollegeWebsite=null;
    private TextView lblCollegeCity=null;

    private LinearLayout btnCollegeInfo=null;

    private ProgressBar pbPosterImage=null;

    private ListView lvTimeline=null;

    private long collegeId=-1;
    private College college=null;

    private final int GET_COLLEGE_TIMELINE=1;

    private long currentTimelineOffset=-1;

    private boolean workFetchTimelines=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_college_panel);

        context=this;

        getSupportActionBar().setTitle("College");

        progressDialog=progressDialog.show(this,null,"Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        initComponents();
        initClicks();

        if(validateCollegeID() && initCollege()) {

            if(college!=null) {
                getSupportActionBar().setTitle(college.short_name);
                getSupportActionBar().setSubtitle(college.city);
            }

            updateComponents();
            updateTimelines();

            toggleHolder(false);

            progressDialog.dismiss();

            //onRefresh();

        } else {
            progressDialog.setMessage("Sorry, can't navigate to college panel.");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    finish();
                }
            },1500);
        }
    }

    private void initClicks() {
        swipeRefreshLayout.setOnRefreshListener(this);

        lvTimeline.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVertPos=(lvTimeline==null||lvTimeline.getChildCount()==0)?0:lvTimeline.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem==0 && topRowVertPos>=0);
            }
        });

        lvTimeline.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyListAdapter mla=(MyListAdapter)lvTimeline.getAdapter();
                Toast.makeText(context,"Position: "+position,Toast.LENGTH_SHORT).show();
            }
        });

        btnCollegeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(collegeId>0)
                    Helper.ActivityNavigators.showCollegeInfo(context, collegeId);
            }
        });
    }

    private boolean initCollege() {
        try {
            college=Helper.getMainDatabase(this).getCollege(collegeId);
            if(college!=null)
                return true;
        } catch(Exception ex) {

        }
        return false;
    }

    private void updateComponents() {

        imgBigPicture.setImageDrawable(getResources().getDrawable(R.drawable.hourglass));
        ImageLoader.downloadCollegeLogo(imgBigPicture,collegeId, ImageLoader.ImageType.Full,5,new ImageLoader.IImageLoader() {
            @Override
            public void imageStatus(int imageId, int percentage) {

            }

            @Override
            public void imageUpdated(int imageId, ImageLoader imageLoader) {
                if(imageLoader.success && imageLoader.bitmap!=null)
                    imgBigPicture.setImageBitmap(imageLoader.bitmap);
            }
        }).startLoader();

        lblCollegeName.setText(college.full_name);
        lblCollegeEmail.setText(college.email_address);
        lblCollegeFax.setText(college.fax_number);
        lblCollegeTelephone.setText(college.telephone_number);
        lblCollegeWebsite.setText(college.website);
        lblCollegeCity.setText(college.city);

        JSONLoader.loadCollege(this,collegeId,new JSONLoader.IJSONLoader() {
            @Override
            public void jsonStatus(int jsonId, int percentage) {
                pbPosterImage.setVisibility(View.VISIBLE);
                pbPosterImage.setProgress(percentage);
            }

            @Override
            public void jsonUpdated(int jsonId, JSONLoader jsonLoader) {
                //pbPosterImage.setVisibility(View.GONE);
                if(jsonLoader.success && jsonLoader.myJSON!=null && jsonLoader.myJSON.isReady()) {
                    String jsonClg=jsonLoader.myJSON.getString("college");
                    if(jsonClg!=null) {
                        jsonLoader.myJSON=new MyJSON(jsonClg);
                        if(jsonLoader.myJSON.isReady()) {
                            College clg=new College(jsonLoader.myJSON);
                            if(clg.ready) {
                                lblCollegeName.setText(clg.full_name);
                                lblCollegeEmail.setText(clg.email_address);
                                lblCollegeFax.setText(clg.fax_number);
                                lblCollegeTelephone.setText(clg.telephone_number);
                                lblCollegeWebsite.setText(clg.website);
                                lblCollegeCity.setText(clg.city);

                                Helper.getMainDatabase(context).updateCollege(clg);
                            }
                        }
                    }
                }
            }
        });

        toggleHolder(false);
    }

    private void updateTimelines() {
        toggleLvTimelines(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Timeline> timelineList=Helper.getMainDatabase(context).getTimelines(collegeId,0); //JSONHelper.getCollegeTimelines(this,collegeId,currentTimelinePage);
                if(timelineList.size()>0) {
                    boolean refresh=currentTimelineOffset==-1;
                    currentTimelineOffset=timelineList.get(0).id;
                    currentTimelineOffset++;
                    if(refresh) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onRefresh();
                            }
                        });
                    }
                }
                final MyListAdapter mla=new MyListAdapter(context, R.layout.listview_timeline);
                mla.setAdapterFor(MyListAdapter.AdapterFor.TIMELINES,timelineList);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lvTimeline.setAdapter(mla);
                        toggleLvTimelines(true);
                    }
                });
            }
        }).start();
    }

    private void toggleLvTimelines(boolean showTimelines) {
        if(showTimelines) {
            lvTimeline.setVisibility(View.VISIBLE);
            holderTimelineLoader.setVisibility(View.GONE);
        } else {
            lvTimeline.setVisibility(View.GONE);
            holderTimelineLoader.setVisibility(View.VISIBLE);
        }
    }

    private void toggleHolder(boolean showLoader) {
        if(showLoader) {
            holderLoader.setVisibility(View.VISIBLE);
            holderCollegeProfile.setVisibility(View.GONE);
        } else {
            holderLoader.setVisibility(View.GONE);
            holderCollegeProfile.setVisibility(View.VISIBLE);
        }
    }

    private void initComponents() {
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_container);

        holderLoader=(RelativeLayout)findViewById(R.id.holderLoader);
        holderCollegeProfile=(LinearLayout)findViewById(R.id.holderCollegeProfile);

        imgBigPicture=(ImageView)findViewById(R.id.imgBigPicture);

        lblCollegeName=(TextView)findViewById(R.id.lblCollegeName);
        lblCollegeEmail=(TextView)findViewById(R.id.lblCollegeEmail);
        lblCollegeFax=(TextView)findViewById(R.id.lblCollegeFax);
        lblCollegeTelephone=(TextView)findViewById(R.id.lblCollegeTelephone);
        lblCollegeWebsite=(TextView)findViewById(R.id.lblCollegeWebsite);
        lblCollegeCity=(TextView)findViewById(R.id.lblCollegeCity);

        pbPosterImage=(ProgressBar)findViewById(R.id.pbPosterImage);
        holderTimelineLoader =(RelativeLayout)findViewById(R.id.holderTimelineLoader);

        lvTimeline=(ListView)findViewById(R.id.lvTimeline);

        btnCollegeInfo=(LinearLayout)findViewById(R.id.btnCollegeInfo);
        //progressDialog=new ProgressDialog(this);
    }

    private boolean validateCollegeID() {
        Intent intent=getIntent();
        if(intent!=null) {
            collegeId=intent.getLongExtra("collegeId",-1);
            if(collegeId>-1) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView lblSwipeDownNote=(TextView)findViewById(R.id.lblSwipeDownNote);
                lblSwipeDownNote.setVisibility(View.GONE);
            }
        },100);
        if(college!=null) {
            JSONLoader.loadTimelines(this, collegeId, 0, currentTimelineOffset, new JSONLoader.IJSONLoader() {
                @Override
                public void jsonStatus(int jsonId, int percentage) {
                    Helper.Util.SwipeRefresh_StartRefreshing(swipeRefreshLayout, true);
                }

                @Override
                public void jsonUpdated(int jsonId, final JSONLoader jsonLoader) {

                    if (jsonLoader.success && jsonLoader.myJSON != null && jsonLoader.myJSON.isReady()) {
                        if (jsonLoader.myJSON.success) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        final JSONArray array = new JSONArray(jsonLoader.myJSON.getString("timelines"));
                                        JSONHelper.updateTimelines(context, array);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                updateTimelines();
                                                Toast.makeText(context, array.length() + " update found.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } catch (Exception ex) {
                                        Helper.Log(ex, "loadTimelines[colleges]: parseTimelines, " + jsonLoader.myJSON);
                                    }
                                }
                            }).start();
                            Toast.makeText(context, "Updating timelines...", Toast.LENGTH_SHORT).show();
                            toggleLvTimelines(false);
                        } else {
                            if (Helper.isNetworkConnected(context)) {
                                Toast.makeText(context, "No new updates.", Toast.LENGTH_SHORT).show();
                            } else {
                                Helper.showAlertDialog(context, "Oops, unable to get latest college timelines.").show();
                            }
                        }
                    } else {
                        if (Helper.isNetworkConnected(context)) {
                            Helper.showAlertDialog(context, "Oops, unable to get latest college timelines.").show();
                        }
                    }

                    Helper.Util.SwipeRefresh_StartRefreshing(swipeRefreshLayout, false);
                }
            });
        }
    }

    private boolean isWorking() {
        return workFetchTimelines;
    }

    private void stopRefreshing() {
        if(!isWorking()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_college_panel, menu);
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
