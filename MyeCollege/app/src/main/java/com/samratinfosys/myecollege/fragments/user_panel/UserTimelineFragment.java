package com.samratinfosys.myecollege.fragments.user_panel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.samratinfosys.myecollege.R;
import com.samratinfosys.myecollege.UserPanel;
import com.samratinfosys.myecollege.fragments.OnFragmentActivityResult;
import com.samratinfosys.myecollege.fragments.OnFragmentInteractionListener;
import com.samratinfosys.myecollege.fragments.OnFragmentOptionsItemSelected;
import com.samratinfosys.myecollege.json_classes.Account;
import com.samratinfosys.myecollege.json_classes.MyJSON;
import com.samratinfosys.myecollege.json_classes.Profile;
import com.samratinfosys.myecollege.json_classes.Timeline;
import com.samratinfosys.myecollege.tools.JSONHelper;
import com.samratinfosys.myecollege.tools.MyListAdapter;
import com.samratinfosys.myecollege.tools.loader.ImageLoader;
import com.samratinfosys.myecollege.tools.loader.JSONLoader;
import com.samratinfosys.myecollege.utils.Helper;
import com.samratinfosys.myecollege.views.MyScrollView;
import com.samratinfosys.myecollege.views.RoundImage;

import org.json.JSONArray;

import java.util.List;

public class UserTimelineFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        OnFragmentOptionsItemSelected,
        OnFragmentActivityResult {

    private UserPanel context;
    private static UserTimelineFragment userTimelineFragment=null;

    private OnFragmentInteractionListener mListener;

    // components variables

    // ui components
    private SwipeRefreshLayout swipeRefreshLayout=null;
    private MyScrollView myScrollView=null;
    private ImageView imgUserProfile=null;
    private TextView lblUserProfileName=null;
    private TextView lblTimelineStatus=null;
    private EditText txtNewPostText=null;
    private Button btnPostNow=null;
    private ListView lvTimeline=null;

    private ProgressDialog progressDialog=null;

    private int countProfileDownload=0;
    private long currentTimelineOffset=-1;


    // components variables

    public static UserTimelineFragment newInstance() {
        if(userTimelineFragment==null)
            userTimelineFragment = new UserTimelineFragment();

        //*.setArguments(null);
        return userTimelineFragment;
    }

    public UserTimelineFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_user_timeline, container, false);

        initComponents(view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(context.isAccountExists()) {

            //Helper.getMainDatabase(context).deleteProfile(0,context.getLoggedAccount().id);
            validateProfile();

        } else {
            AlertDialog.Builder dialog= Helper.showAlertDialog(context, "Your login session expired, please login again.", "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    // show this disabled
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        }

    }

    private void afterAllInitialValidation() {

        progressDialog=new ProgressDialog(context);

        //initComponents();

        updateComponents();

        updateTimelines();

    }

    private void validateProfile() {
        if(context.isProfileExists()) {

            afterAllInitialValidation();

        } else {

            downloadProfile(true);

        }
    }

    private void downloadProfile(final boolean sticky) {
        if(countProfileDownload<5) {
            countProfileDownload++;
        } else {
            AlertDialog.Builder dialog=Helper.showAlertDialog(context,"We have tried 5 times to load profile from data center, please try again after sometime.","Back",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // make this disabled
                }
            });
            dialog.setCancelable(false);
            dialog.show();
            return;
        }

        progressDialog=new ProgressDialog(context);
        progressDialog.setMessage("Loading profile...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JSONLoader.loadProfile(context, context.getLoggedAccount().id, true, new JSONLoader.IJSONLoader() {
            @Override
            public void jsonStatus(int jsonId, int percentage) {

            }

            @Override
            public void jsonUpdated(int jsonId, JSONLoader jsonLoader) {
                String message = null;
                if (jsonLoader.success && jsonLoader.myJSON != null) {
                    if (jsonLoader.myJSON.isReady() && jsonLoader.myJSON.success) {
                        String profileData = jsonLoader.myJSON.getString("profile");
                        MyJSON profileJson = new MyJSON(profileData);
                        if (profileJson.isReady()) {
                            Profile profile = new Profile(profileJson);
                            if (profile.ready) {
                                Helper.getMainDatabase(context).updateOrAddProfile(profile);
                            } else {
                                message = "Unable to parse profile, please try again.";
                            }
                        } else {
                            message = "Unable to initialize profile, please try again.";
                        }
                    } else {
                        boolean noProfile = jsonLoader.myJSON.getBoolean("noProfile", false);
                        if (noProfile && context.getLoggedAccount() != null) {
                            context.showMyProfile();
                            //finish(); make this exit
                            message = "";
                        } else {
                            message = jsonLoader.myJSON.error_message != null ? jsonLoader.myJSON.error_message : "Unable to load profile, please try again.";
                        }
                    }
                } else {
                    message = "Unable to get profile, please try again.";
                }

                progressDialog.dismiss();
                if (message != null && message.trim().length() > 0) {
                    downloadProfileError(message, sticky);
                } else {
                    if (message != null && message.trim().length() == 0) {
                        // nothing to do when message is empty ;)
                    } else {
                        validateProfile();
                    }
                }
            }
        });

    }

    private void downloadProfileError(String message, final boolean sticky) {

        String positiveButton="Retry";
        DialogInterface.OnClickListener positiveCallback=new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                downloadProfile(sticky);
            }
        };

        if(!sticky) { // if dialog is cancellable
            positiveButton="OK";
            positiveCallback=new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };
        }

        AlertDialog.Builder dialog=Helper.showAlertDialog(context,message,positiveButton,positiveCallback);
        dialog.setNegativeButton("Back",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //finish();
            }
        });
        dialog.setCancelable(!sticky);
        dialog.show();
    }

    public void initComponents(View view) {
        swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swipe_container);
        myScrollView=(MyScrollView)view.findViewById(R.id.myScrollView);
        imgUserProfile=(ImageView)view.findViewById(R.id.imgUserProfile);
        lblUserProfileName=(TextView)view.findViewById(R.id.lblUserProfileName);
        lblTimelineStatus=(TextView)view.findViewById(R.id.lblTimelineStatus);
        txtNewPostText=(EditText)view.findViewById(R.id.txtNewPostText);
        btnPostNow=(Button)view.findViewById(R.id.btnPostNow);
        lvTimeline=(ListView)view.findViewById(R.id.lvTimeline);
        initClicks();
    }

    public void initClicks() {
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

        btnPostNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postText=txtNewPostText.getText().toString().trim();
                if(postText.length()==0) {
                    txtNewPostText.requestFocus();
                    txtNewPostText.setError("Hey!!, What are you thinking...");
                } else {

                    MyJSON json=new MyJSON(true);
                    json.put("heading", context.getFullName());
                    json.put("timelineText",postText);

                    final JSONLoader jsonLoader=JSONLoader.updateTimeline(context, Timeline.Types.Text, json, new JSONLoader.IJSONLoader() {
                        @Override
                        public void jsonStatus(int jsonId, final int percentage) {
                            progressDialog.setMessage("Uploading... ["+percentage+"%]");
                        }

                        @Override
                        public void jsonUpdated(int jsonId, JSONLoader jsonLoader) {
                            if(jsonLoader.success && jsonLoader.myJSON!=null) {
                                if(jsonLoader.myJSON.success) {
                                    Toast.makeText(context,"Successfully posted.",Toast.LENGTH_SHORT).show();
                                    onRefresh();
                                } else {
                                    String message="Sorry, can't post right now, please try again after sometime.";
                                    if(jsonLoader.myJSON.error_message!=null) {
                                        message=jsonLoader.myJSON.error_message;
                                    }

                                    Helper.showAlertDialog(context,message,"OK",new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();

                                }
                            } else {
                                Toast.makeText(context,"Unable to post right now, please try again after sometime.",Toast.LENGTH_LONG).show();
                            }
                            progressDialog.dismiss();
                            progressDialog.cancel();
                        }
                    });
                    progressDialog.setTitle("Posting...");
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Initializing...");
                    progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(jsonLoader!=null && !jsonLoader.cancelLoader()) {
                                Toast.makeText(context,"Sorry, your post request is already sent.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    progressDialog.show();
                }
            }
        });
    }

    public void updateComponents() {
        updateProfileComponents();

        //context.getSupportActionBar().setTitle(context.getFullName());
        context.getSupportActionBar().setSubtitle(context.getLoggedAccount().user_id);
    }

    public void updateProfileComponents() {
        if(context.getLoggedProfile()==null) return;
        imgUserProfile.setImageDrawable(getResources().getDrawable(R.drawable.hourglass));
        ImageLoader.downloadProfilePicture(imgUserProfile, ImageLoader.ImageType.Thumb, 9, new ImageLoader.IImageLoader() {
            @Override
            public void imageStatus(int imageId, int percentage) {

            }

            @Override
            public void imageUpdated(int imageId, final ImageLoader imageLoader) {
                RoundImage loadImage = new RoundImage(BitmapFactory.decodeResource(getResources(), R.drawable.hourglass));
                imgUserProfile.setImageDrawable(loadImage);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RoundImage roundImage = null;
                        if (imageLoader.success && imageLoader.bitmap != null) {
                            roundImage = new RoundImage(Helper.Images.scaleBitmap(imageLoader.bitmap, 200, 200, false));
                        } else {
                            roundImage = new RoundImage((BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_person)));
                        }

                        if (roundImage != null) {
                            final RoundImage finalRoundImage = roundImage;
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    imgUserProfile.setImageDrawable(finalRoundImage);
                                }
                            });
                        }
                    }
                });
                thread.start();
            }
        }).startLoader();
        //context.getSupportActionBar().setTitle(context.getFullName());
        lblUserProfileName.setText(context.getFullName());
        lblTimelineStatus.setText(context.getLoggedProfile().timeline_status);
    }

    public void updateTimelines() {
        if(context.getLoggedProfile()==null) return;
        toggleLvTimelines(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Timeline> timelineList=Helper.getMainDatabase(context).getTimelines(0,context.getLoggedProfile().id);
                if(timelineList.size()>0) {
                    boolean refresh=currentTimelineOffset==-1;
                    currentTimelineOffset=timelineList.get(0).id;
                    currentTimelineOffset++;
                    if(refresh) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onRefresh();
                            }
                        });
                    }
                }
                final MyListAdapter mla=new MyListAdapter(context, R.layout.listview_timeline);
                mla.setAdapterFor(MyListAdapter.AdapterFor.TIMELINES,timelineList);

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lvTimeline.setAdapter(mla);
                        toggleLvTimelines(true);
                    }
                });
            }
        }).start();
    }


    @Override
    public void onRefresh() {
        if(context.getLoggedProfile()!=null) {
            JSONLoader.loadTimelines(context, 0, 0, currentTimelineOffset, new JSONLoader.IJSONLoader() {
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
                                        context.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                updateTimelines();
                                                Toast.makeText(context, array.length() + " update found.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } catch (Exception ex) {
                                        Helper.Log(ex, "loadTimelines[logged-profile]: parseTimelines, " + jsonLoader.myJSON);
                                    }
                                }
                            }).start();
                            Toast.makeText(context, "Updating timelines...", Toast.LENGTH_SHORT).show();
                            toggleLvTimelines(false);
                        } else {
                            if (Helper.isNetworkConnected(context)) {
                                Toast.makeText(context, "No new updates.", Toast.LENGTH_SHORT).show();
                            } else {
                                Helper.showAlertDialog(context, "Oops, unable to get latest timeline.").show();
                            }
                        }
                    } else {
                        if (Helper.isNetworkConnected(context)) {
                            Helper.showAlertDialog(context, "Oops, unable to get latest timelines.").show();
                        }
                    }

                    Helper.Util.SwipeRefresh_StartRefreshing(swipeRefreshLayout, false);
                }
            });
        } else {
            Helper.Util.SwipeRefresh_StartRefreshing(swipeRefreshLayout, false);
        }
    }

    /* ------------------------------------------------------------------------------------------ */

    private void toggleLvTimelines(boolean showTimelines) {

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        context=(UserPanel)activity;

        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        try {
            updateComponents();
        } catch (Exception ex) {}
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onMenuItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==context.RESULT_OK) {
            // look for first profile setup...--
        }
    }
}
