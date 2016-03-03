package com.samratinfosys.myecollege;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.samratinfosys.myecollege.json_classes.College;
import com.samratinfosys.myecollege.tools.loader.ImageLoader;
import com.samratinfosys.myecollege.utils.Helper;

import org.w3c.dom.Text;


public class CollegeInfo extends ActionBarActivity {

    private College college=null;

    private ProgressDialog progressDialog=null;

    private ImageView imgCollegeLogo=null;
    private TextView lblCollegeName=null;

    private TextView lblCollegeShortName=null;
    private TextView lblCollegeCode=null;
    private TextView lblCollegeType=null;
    private TextView lblCollegeEstdYear=null;
    private TextView lblCollegeFees=null;

    private TextView lblCollegeFax=null;
    private TextView lblCollegeTelephone=null;
    private TextView lblCollegeEmail=null;

    private TextView lblCollegeAddress=null;

    private TextView lblCollegeWebsite=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_college_info);

        if(initCollege()) {

            if(college!=null) {
                getSupportActionBar().setTitle(college.full_name);
                getSupportActionBar().setSubtitle(college.city);
            }

            initComponents();
            updateComponents();

        } else {
            progressDialog.setMessage("Sorry, can't navigate to college info.");
            progressDialog.setCancelable(false);
            progressDialog.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    finish();
                }
            },1500);
        }
    }

    private boolean initCollege() {
        Intent intent=getIntent();

        if(intent!=null) {
            long collegeId=intent.getLongExtra("collegeId",0);
            if(collegeId>0) {
                college=Helper.getMainDatabase(this).getCollege(collegeId);
                if(college!=null)
                    return true;
            }
        }

        return false;
    }

    private void initComponents() {
        imgCollegeLogo=(ImageView)findViewById(R.id.imgCollegeLogo);
        lblCollegeName=(TextView)findViewById(R.id.lblCollegeName);

        lblCollegeShortName=(TextView)findViewById(R.id.lblCollegeShortName);
        lblCollegeCode=(TextView)findViewById(R.id.lblCollegeCode);
        lblCollegeType=(TextView)findViewById(R.id.lblCollegeType);
        lblCollegeEstdYear=(TextView)findViewById(R.id.lblCollegeEstdYear);
        lblCollegeFees=(TextView)findViewById(R.id.lblCollegeFees);

        lblCollegeFax=(TextView)findViewById(R.id.lblCollegeFax);
        lblCollegeTelephone=(TextView)findViewById(R.id.lblCollegeTelephone);
        lblCollegeEmail=(TextView)findViewById(R.id.lblCollegeEmail);

        lblCollegeAddress=(TextView)findViewById(R.id.lblCollegeAddress);

        lblCollegeWebsite=(TextView)findViewById(R.id.lblCollegeWebsite);
    }

    private void updateComponents() {

        imgCollegeLogo.setImageDrawable(getResources().getDrawable(R.drawable.hourglass));
        ImageLoader.downloadCollegeLogo(imgCollegeLogo,college.id, ImageLoader.ImageType.Poster,5,new ImageLoader.IImageLoader() {
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

        lblCollegeName.setText(college.full_name);

        lblCollegeShortName.setText(college.short_name);
        lblCollegeCode.setText("Code: "+college.code);
        lblCollegeType.setText("Type: "+college.type);
        lblCollegeEstdYear.setText("Estd. Year: "+college.estd_year);
        lblCollegeFees.setText("Fees: "+college.tution_fees);

        lblCollegeFax.setText("Fax: "+college.fax_number);
        lblCollegeTelephone.setText("Phone: "+college.telephone_number);
        lblCollegeEmail.setText("Email: "+college.email_address);

        lblCollegeAddress.setText(college.address);

        lblCollegeWebsite.setText(college.website);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_college_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id==R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
