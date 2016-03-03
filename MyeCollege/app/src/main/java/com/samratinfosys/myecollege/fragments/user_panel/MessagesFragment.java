package com.samratinfosys.myecollege.fragments.user_panel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.samratinfosys.myecollege.R;
import com.samratinfosys.myecollege.UserPanel;
import com.samratinfosys.myecollege.fragments.OnFragmentActivityResult;
import com.samratinfosys.myecollege.fragments.OnFragmentInteractionListener;
import com.samratinfosys.myecollege.fragments.OnFragmentOptionsItemSelected;
import com.samratinfosys.myecollege.json_classes.Chat;
import com.samratinfosys.myecollege.json_classes.ChatHead;
import com.samratinfosys.myecollege.json_classes.MyJSON;
import com.samratinfosys.myecollege.json_classes.Profile;
import com.samratinfosys.myecollege.json_classes.Timeline;
import com.samratinfosys.myecollege.tools.GeneralConstants;
import com.samratinfosys.myecollege.tools.JSONHelper;
import com.samratinfosys.myecollege.tools.MyListAdapter;
import com.samratinfosys.myecollege.tools.loader.ImageLoader;
import com.samratinfosys.myecollege.tools.loader.JSONLoader;
import com.samratinfosys.myecollege.utils.Helper;
import com.samratinfosys.myecollege.views.MyScrollView;
import com.samratinfosys.myecollege.views.RoundImage;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessagesFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        OnFragmentOptionsItemSelected,
        OnFragmentActivityResult {

    private UserPanel context;
    private static MessagesFragment messagesFragment =null;

    private OnFragmentInteractionListener mListener;

    // components variables

    // ui components
    private ScrollView scrollView=null;
    private ListView lvMessages=null;
    private LinearLayout holderEmptyView=null;


    private ProgressDialog progressDialog=null;

    public static MessagesFragment newInstance() {
        if(messagesFragment ==null)
            messagesFragment = new MessagesFragment();

        //*.setArguments(null);
        return messagesFragment;
    }

    public MessagesFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_messages, container, false);
        initComponents(view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        progressDialog=new ProgressDialog(context);

        if(context.isProfileExists()) {

            updateComponents();

        } else {
            AlertDialog.Builder dialog= Helper.showAlertDialog(context,"Sorry, unable to open messages.","Back",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    context.showMyTimeline();
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    public void initComponents(View view) {
        scrollView=(ScrollView)view.findViewById(R.id.scrollView);
        holderEmptyView=(LinearLayout)view.findViewById(R.id.holderEmptyView);
        lvMessages=(ListView)view.findViewById(R.id.lvMessages);
        lvMessages.setEmptyView(holderEmptyView);

        lvMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyListAdapter mla=(MyListAdapter)lvMessages.getAdapter();
                ChatHead chatHead=(ChatHead)mla.getItem(position);
                context.showChat(chatHead.fromId);
            }
        });

    }

    public void updateComponents() {

        progressDialog.setMessage("Loading messages...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {

                List<ChatHead> chatHeads=Helper.getMainDatabase(context).getChatHeads(-1);

                final MyListAdapter myListAdapter=new MyListAdapter(context,R.layout.listview_message);
                myListAdapter.setAdapterFor(MyListAdapter.AdapterFor.MESSAGES,chatHeads);

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lvMessages.setAdapter(myListAdapter);
                        progressDialog.dismiss();
                    }
                });

            }
        });
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();

    }

    @Override
    public void onRefresh() {

    }

    /* ------------------------------------------------------------------------------------------ */

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
            //updateComponents();
        } catch (Exception ex) {}
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onMenuItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.action_new_message) {
            Helper.ActivityNavigators.showMyFriends(context, GeneralConstants.RESULT_FROM_MYFRIENDS);
        }

        return true;
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==GeneralConstants.RESULT_FROM_MYFRIENDS) {
            if(data!=null) {
                boolean accepted=data.getBooleanExtra("accepted",false);
                if(accepted) {
                    final long profile_id=data.getLongExtra("profileId",0);
                    long account_id=data.getLongExtra("accountId",0);

                    //start msg screen for this user...
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //getChildFragmentManager().beginTransaction().replace(R.id.frameLayout,ChatFragment.newInstance(profile_id));
                        }
                    });

                }
            }
        }
    }
}
