package com.samratinfosys.myecollege;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.samratinfosys.myecollege.fragments.OnFragmentActivityResult;
import com.samratinfosys.myecollege.fragments.OnFragmentInteractionListener;
import com.samratinfosys.myecollege.fragments.OnFragmentOptionsItemSelected;
import com.samratinfosys.myecollege.fragments.user_panel.ChatFragment;
import com.samratinfosys.myecollege.fragments.user_panel.MessagesFragment;
import com.samratinfosys.myecollege.fragments.user_panel.MyProfileFragment;
import com.samratinfosys.myecollege.fragments.user_panel.UserTimelineFragment;
import com.samratinfosys.myecollege.json_classes.Account;
import com.samratinfosys.myecollege.json_classes.Profile;
import com.samratinfosys.myecollege.utils.Helper;


public class UserPanel extends ActionBarActivity implements ListView.OnItemClickListener, OnFragmentInteractionListener {

    private final static int MENU_MY_TIMELINE =0;
    private final static int MENU_MY_PROFILE =1;
    private final static int MENU_MESSAGES=2;
    private final static int MENU_FACULTIES=3;
    private final static int MENU_DAILY_NOTES=4;
    private final static int MENU_TIME_TABLES=5;
    private final static int MENU_ATTENDANCE=6;
    private final static int MENU_NOTICE_BOARD=7;
    private final static int MENU_ACTIVITIES=8;
    private final static int MENU_DOWNLOADS=9;
    private final static int MENU_CHAT=10;

    private DrawerLayout drawerLayout=null;
    private ActionBarDrawerToggle actionBarDrawerToggle=null;
    private ListView drawerListView=null;
    private FrameLayout frameLayout=null;
    private ArrayAdapter<String> arrayAdapter=null;

    private String[] menuItems=null;

    // user variables
    private Account loggedAccount=null;
    private Profile loggedProfile=null;

    // variables
    private int currentFragmentID=0, previousFragmentID=-1;

    // fragment listeners
    private OnFragmentOptionsItemSelected onFragmentOptionsItemSelected=null;
    private OnFragmentActivityResult onFragmentActivityResult=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_panel);

        getSupportActionBar().setTitle("My eCollege");
        initComponents();
        initDrawerListItems();
        initDrawerListView();
        initDrawerLayout();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // call for default fragment
        showMyTimeline();
    }

    /* ----------------------------------- SPECIAL METHODS -------------------------------------- */

    public void showMyTimeline() {
        drawerListView.setSelection(MENU_MY_TIMELINE);
        onItemClick((AdapterView<?>)drawerListView,null, MENU_MY_TIMELINE,0);
        supportInvalidateOptionsMenu();
    }

    public void showMyProfile() {
        drawerListView.setSelection(MENU_MY_PROFILE);
        onItemClick((AdapterView<?>)drawerListView,null, MENU_MY_PROFILE,0);
        supportInvalidateOptionsMenu();
    }

    public void showChat(long profileId) {
        boolean transactionCompleted=false;

        Fragment fragment=null;
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragment= ChatFragment.newInstance(profileId);

        if(fragment!=null) {
            try {
                onFragmentOptionsItemSelected = (OnFragmentOptionsItemSelected) fragment;
            } catch (ClassCastException e) {
                onFragmentOptionsItemSelected = null;
            }

            try {
                onFragmentActivityResult = (OnFragmentActivityResult) fragment;
            } catch (ClassCastException e) {
                onFragmentActivityResult = null;
            }

            fragmentManager.beginTransaction().replace(R.id.frameLayout, fragment).commit();
            transactionCompleted=true;
        }

    }

    public Account getLoggedAccount() {
        return loggedAccount;
    }

    protected void setLoggedAccount(Account loggedAccount) {
        this.loggedAccount = loggedAccount;
    }

    public Profile getLoggedProfile() {
        return loggedProfile;
    }

    protected void setLoggedProfile(Profile loggedProfile) {
        this.loggedProfile = loggedProfile;
    }

    public boolean isProfileExists() {
        return isProfileExists(false);
    }

    public boolean isProfileExists(boolean readAgain) {
        if(!isAccountExists(readAgain)) return false;

        if(loggedProfile==null) readAgain=true;

        if(readAgain) {
            loggedProfile = Helper.getMainDatabase(this).getProfile(0, this.getLoggedAccount().id);
        }

        return getLoggedProfile()!=null;
    }

    public boolean isAccountExists() {
        return isAccountExists(false);
    }

    public boolean isAccountExists(boolean readAgain) {

        if(loggedAccount==null) readAgain=true;

        if(readAgain) {
            loggedAccount=Helper.getLoggedAccount(this);
        }

        return getLoggedAccount()!=null;
    }

    public String getFullName() {
        if(getLoggedProfile()!=null)
            return getLoggedProfile().first_name+" "+getLoggedProfile().last_name;
        else if(getLoggedAccount()!=null)
            return getLoggedAccount().name;
        return "";
    }

    /* ------------------------------------------------------------------------------------------ */

    private void initComponents() {
        drawerLayout=(DrawerLayout)findViewById(R.id.drawerLayout);

        actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout, R.string.drawer_open, R.string.drawer_close)  {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                supportInvalidateOptionsMenu();
            }

        };

        drawerListView=(ListView)findViewById(R.id.draweListView);
        frameLayout=(FrameLayout)findViewById(R.id.frameLayout);
    }

    private void initDrawerListItems() {
        // my timeline, my profile, students(my depa, other depa stus), faculties (my depa, other depa facs), messages(personal, groups),  daily notes, time tables, attendance, notice board,
        // activities, downloads, [colleges], [settings]

        menuItems=new String[]
        {
            "My Timeline",
            "My Profile" ,
            "Messages" ,
            "My Friends" ,
            "Faculties" ,
            "Daily notes" ,
            "Time tables" ,
            "Attendance" ,
            "Notice board",
            "Activities",
            "Downloads"
        };

    }

    private void initDrawerListView() {
        arrayAdapter=new ArrayAdapter<String>(this, R.layout.userpanel_drawer_listitem, menuItems);
        drawerListView.setAdapter(arrayAdapter);
        drawerListView.setOnItemClickListener(this);
    }

    private void initDrawerLayout() {
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        boolean transactionCompleted=false;

        Fragment fragment=null;
        FragmentManager fragmentManager = getSupportFragmentManager();
        int tempId = getFragmentID(menuItems[position]);

        if(tempId>-1) {
            previousFragmentID = currentFragmentID;
            currentFragmentID = tempId;


            switch (currentFragmentID) {
                case MENU_MY_TIMELINE:
                    fragment = (Fragment) UserTimelineFragment.newInstance();
                    break;

                case MENU_MY_PROFILE:
                    fragment = (Fragment) MyProfileFragment.newInstance();
                    break;

                case MENU_MESSAGES:
                    fragment=(Fragment) MessagesFragment.newInstance();
                    break;
            }

        }

        if(fragment!=null) {
            try {
                onFragmentOptionsItemSelected = (OnFragmentOptionsItemSelected) fragment;
            } catch (ClassCastException e) {
                onFragmentOptionsItemSelected = null;
            }

            try {
                onFragmentActivityResult = (OnFragmentActivityResult) fragment;
            } catch (ClassCastException e) {
                onFragmentActivityResult = null;
            }

            fragmentManager.beginTransaction().replace(R.id.frameLayout, fragment).commit();
            transactionCompleted=true;
        }

        if(transactionCompleted) {
            drawerListView.setItemChecked(position, true);
            getSupportActionBar().setTitle(menuItems[position]);
            drawerLayout.closeDrawer(drawerListView);
        } else {
            Toast.makeText(this,"Sorry, this feature isn't implemented yet!!",Toast.LENGTH_SHORT).show();
        }

    }

    private int getFragmentID(String menuTitle) {

        switch (menuTitle) {
            case "My Timeline": return MENU_MY_TIMELINE;
            case "My Profile": return MENU_MY_PROFILE;
            case "Messages": return MENU_MESSAGES;
        }

        return -1;
    }

    public boolean isDrawerOpen() {
        try {
            return drawerLayout.isDrawerOpen(drawerListView);
        } catch (Exception ex) {}
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            if(isDrawerOpen()) {
                drawerLayout.closeDrawer(Gravity.START);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(isDrawerOpen())
            return false;
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if(currentFragmentID== MENU_MY_PROFILE)
            getMenuInflater().inflate(R.menu.menu_my_profile, menu);

        if(currentFragmentID==MENU_MESSAGES)
            getMenuInflater().inflate(R.menu.menu_fragment_messages, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if(onFragmentOptionsItemSelected!=null) {
            onFragmentOptionsItemSelected.onMenuItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(onFragmentActivityResult!=null)
            onFragmentActivityResult.onFragmentResult(requestCode,resultCode,data);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
