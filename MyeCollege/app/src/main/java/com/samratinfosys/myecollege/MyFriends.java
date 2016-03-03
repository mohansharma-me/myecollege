package com.samratinfosys.myecollege;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.samratinfosys.myecollege.json_classes.MyJSON;
import com.samratinfosys.myecollege.tools.GeneralConstants;
import com.samratinfosys.myecollege.tools.MyListAdapter;
import com.samratinfosys.myecollege.tools.loader.JSONLoader;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;


public class MyFriends extends ActionBarActivity {

    private Context context;

    private ListView lvUsers;

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);
        context=this;

        initComponents();
        initList();
    }

    private void initComponents() {
        lvUsers=(ListView)findViewById(R.id.lvUsers);
        progressDialog=new ProgressDialog(this);
    }

    private void initList() {
        progressDialog=ProgressDialog.show(context,"Loading...","Fetching user list...");
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                publishResult(false,0,0);
            }
        });

        lvUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyListAdapter mla=(MyListAdapter)lvUsers.getAdapter();
                MyJSON myJSON= (MyJSON) mla.getItem(position);

                long profile_id=myJSON.getLong("profile_id",0);
                long account_id=myJSON.getLong("account_id",0);

                if(profile_id==0) {
                    Toast.makeText(context,"Error, please try again.",Toast.LENGTH_SHORT).show();
                    publishResult(false,0,0);
                } else {
                    publishResult(true,profile_id,account_id);
                }

            }
        });

        JSONLoader.loadUsers(context,new JSONLoader.IJSONLoader() {
            @Override
            public void jsonStatus(int jsonId, int percentage) {

            }

            @Override
            public void jsonUpdated(int jsonId, JSONLoader jsonLoader) {
                progressDialog.dismiss();
                if(jsonLoader.failed) {
                    Toast.makeText(context,"Sorry, unable to fetch users from server.",Toast.LENGTH_SHORT).show();
                    publishResult(false,0,0);
                } else {
                    if(jsonLoader.success && jsonLoader.myJSON.success) {

                        JSONArray array=jsonLoader.myJSON.getJSONArray("users");
                        if(array!=null) {
                            ArrayList<MyJSON> arrUsers=new ArrayList<MyJSON>();
                            for(int i=0;i<array.length();i++) {
                                try {
                                    MyJSON myJSON=new MyJSON(array.getJSONObject(i));
                                    if(myJSON.isReady()) {
                                        arrUsers.add(myJSON);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            if(arrUsers.size()==0) {
                                Toast.makeText(context,"Sorry, no users at all.",Toast.LENGTH_SHORT).show();
                            } else {
                                MyListAdapter myListAdapter=new MyListAdapter(context,R.layout.listview_message);
                                myListAdapter.setAdapterFor(MyListAdapter.AdapterFor.MYJSON_USERS,arrUsers);
                                lvUsers.setAdapter(myListAdapter);
                            }

                        } else {
                            Toast.makeText(context,"Error#1, please try again.",Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(context,"Error, please try again.",Toast.LENGTH_SHORT).show();
                        publishResult(false,0,0);
                    }
                }
            }
        });
    }

    private void publishResult(boolean accepted, long userId, long accountId){
        Intent intent=new Intent();
        intent.putExtra("accepted",accepted);
        intent.putExtra("userId",userId);
        intent.putExtra("accountId",accountId);
        setResult(GeneralConstants.RESULT_FROM_MYFRIENDS,intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_friends, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
