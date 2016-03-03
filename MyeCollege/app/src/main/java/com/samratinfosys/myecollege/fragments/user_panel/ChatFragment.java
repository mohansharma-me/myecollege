package com.samratinfosys.myecollege.fragments.user_panel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.samratinfosys.myecollege.R;
import com.samratinfosys.myecollege.UserPanel;
import com.samratinfosys.myecollege.fragments.OnFragmentActivityResult;
import com.samratinfosys.myecollege.fragments.OnFragmentInteractionListener;
import com.samratinfosys.myecollege.fragments.OnFragmentOptionsItemSelected;
import com.samratinfosys.myecollege.json_classes.Chat;
import com.samratinfosys.myecollege.json_classes.MyJSON;
import com.samratinfosys.myecollege.tools.GeneralConstants;
import com.samratinfosys.myecollege.tools.MyListAdapter;
import com.samratinfosys.myecollege.tools.loader.JSONLoader;
import com.samratinfosys.myecollege.utils.Helper;

import java.util.List;

public class ChatFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        OnFragmentOptionsItemSelected,
        OnFragmentActivityResult {


    private UserPanel context;
    private static ChatFragment chatFragment =null;

    private OnFragmentInteractionListener mListener;

    // components variables
    private long from_profile_id=0;
    private Chat.Status chatStatus= Chat.Status.New;

    // ui components
    private ScrollView scrollView=null;
    private ListView lvChat=null;
    private EditText txtChatMessage=null;
    private Button btnSendChat=null;

    private Thread lookupThread=null;

    private ProgressDialog progressDialog=null;

    public static ChatFragment newInstance(long profileId) {
        if(chatFragment ==null)
            chatFragment = new ChatFragment();

        chatFragment.from_profile_id=profileId;
        chatFragment.chatStatus= Chat.Status.Old;
        return chatFragment;
    }

    public ChatFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_chat, container, false);
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
            AlertDialog.Builder dialog= Helper.showAlertDialog(context, "Sorry, unable to open messages.", "Back", new DialogInterface.OnClickListener() {
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
        lvChat=(ListView)view.findViewById(R.id.lvChat);
        txtChatMessage=(EditText)view.findViewById(R.id.txtChatMessage);
        btnSendChat=(Button)view.findViewById(R.id.btnSendChat);
    }

    public void updateComponents() {
        btnSendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg=txtChatMessage.getText().toString().trim();
                if(msg.length()>0) {
                    JSONLoader.putChat(context,msg,from_profile_id,new JSONLoader.IJSONLoader() {
                        @Override
                        public void jsonStatus(int jsonId, int percentage) {

                        }

                        @Override
                        public void jsonUpdated(int jsonId, JSONLoader jsonLoader) {
                            if(!jsonLoader.success) {
                                Toast.makeText(context,"Unable to send message, please try again.",Toast.LENGTH_SHORT).show();
                            } else {
                                String str=new String(jsonLoader.downloadedData);
                                Chat chat=new Chat(new MyJSON(jsonLoader.myJSON.getJSONObject("chat")));
                                //Helper.getMainDatabase(context).addChat(chat);
                                //txtChatMessage.setText("");
                            }
                        }
                    });
                }
            }
        });

        lvChat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        //updateChat();

        if(lookupThread!=null) {
            lookupThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (isVisible()) {

                            updateChat();
                            Thread.sleep(2000);
                        }
                    } catch (Exception ex) {
                    }
                }
            });
            lookupThread.start();
        }
    }

    private void updateChat() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Chat> chats=Helper.getMainDatabase(context).getChats(Chat.PropagationType.Unicast, chatStatus, from_profile_id, context.getLoggedProfile().id);
                final MyListAdapter mla=new MyListAdapter(context,R.layout.listview_chat);
                mla.setAdapterFor(MyListAdapter.AdapterFor.CHAT,chats);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lvChat.setAdapter(mla);
                    }
                });
                chatStatus= Chat.Status.New;
            }
        }).start();
    }

    @Override
    public void onRefresh() {
        updateChat();
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
        lookupThread.yield();
    }

    @Override
    public boolean onMenuItemSelected(MenuItem item) {
        return true;
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==GeneralConstants.RESULT_FROM_MYFRIENDS) {
            if(data!=null) {
                long profileId=data.getLongExtra("profileId",0);
                if(profileId>0) {
                    context.showChat(profileId);
                }
            }
        }
    }
}
