package com.samratinfosys.myecollege.fragments.user_panel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.samratinfosys.myecollege.R;
import com.samratinfosys.myecollege.UserPanel;
import com.samratinfosys.myecollege.fragments.OnFragmentActivityResult;
import com.samratinfosys.myecollege.fragments.OnFragmentInteractionListener;
import com.samratinfosys.myecollege.fragments.OnFragmentOptionsItemSelected;
import com.samratinfosys.myecollege.json_classes.Account;
import com.samratinfosys.myecollege.json_classes.MyJSON;
import com.samratinfosys.myecollege.json_classes.Profile;
import com.samratinfosys.myecollege.tools.GeneralConstants;
import com.samratinfosys.myecollege.tools.loader.ImageLoader;
import com.samratinfosys.myecollege.tools.loader.JSONLoader;
import com.samratinfosys.myecollege.utils.Helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;
import java.util.Date;

public class MyProfileFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        OnFragmentOptionsItemSelected,
        OnFragmentActivityResult {

    private UserPanel context;
    private static MyProfileFragment myProfileFragment = null;

    private OnFragmentInteractionListener mListener;

    // components variables

    // UI components
    private LinearLayout btnUserProfile=null;

    private ImageView imgUserProfile=null;

    private EditText txtFirstName=null;
    private EditText txtLastName=null;
    private EditText txtMobileNumber=null;
    private EditText txtEmailAddress=null;

    private RadioButton radMale=null;
    private RadioButton radFemale=null;
    private RadioButton radOther=null;

    private DatePicker dtDateOfBirth=null;

    private EditText txtSemester=null;
    private EditText txtAddress=null;

    private ProgressDialog progressDialog=null;

    // initialized variables
    private String first_name=null;
    private String last_name=null;
    private String mobile_number=null;
    private String email_address=null;
    private int gender=-1;
    private Date dateOfBirth=null;
    private int semester=0;
    private String address=null;
    private long realMobileNumber=0;

    // status flags
    private Bitmap selectedBitmap=null;
    private String selectedProfilePicPath =null;
    private boolean isNewProfile=false;


    // components variables

    public static MyProfileFragment newInstance() {
        if(myProfileFragment ==null)
            myProfileFragment = new MyProfileFragment();
        //*.setArguments(null);
        return myProfileFragment;
    }

    public MyProfileFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_my_profile, container, false);
        initComponents(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        progressDialog=new ProgressDialog(context);
        //context.getSupportActionBar().setTitle("My Profile");

        if(context.isAccountExists()) {

            if(context.getLoggedAccount()!=null) {
                context.getSupportActionBar().setSubtitle(context.getLoggedAccount().name);
            }

            // check if profile is already exists or not
            if(context.isProfileExists()) { // old profile

                // some validation if required.

            } else { // new profile

                isNewProfile=true;
                selectedProfilePicPath =null;
                Toast.makeText(context, "Please complete your social profile and click 'Save'.",Toast.LENGTH_LONG).show();

            }

            updateComponents(); // read profile data from either account or profile


        } else {

            AlertDialog.Builder dialog= Helper.showAlertDialog(context,"Sorry, unable to open my profile.","Back",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //finish();
                }
            });
            dialog.setCancelable(false);
            dialog.show();

        }

    }

    private void initComponents(View view) {
        btnUserProfile=(LinearLayout)view.findViewById(R.id.btnUserProfile);
        imgUserProfile=(ImageView)view.findViewById(R.id.imgUserProfile);

        txtFirstName=(EditText)view.findViewById(R.id.txtProfileFirstName);
        txtLastName=(EditText)view.findViewById(R.id.lblProfileLastName);
        txtMobileNumber=(EditText)view.findViewById(R.id.txtProfileMobileNumber);
        txtEmailAddress=(EditText)view.findViewById(R.id.lblProfileEmailAddress);

        radMale=(RadioButton)view.findViewById(R.id.radGenderMale);
        radFemale=(RadioButton)view.findViewById(R.id.radGenderFemale);
        radOther=(RadioButton)view.findViewById(R.id.radGenderOther);

        dtDateOfBirth=(DatePicker)view.findViewById(R.id.dtDateOfBirth);

        txtSemester=(EditText)view.findViewById(R.id.txtProfileSemester);
        txtAddress=(EditText)view.findViewById(R.id.txtProfileAddress);

        initClicks();
    }

    private void initClicks() {
        btnUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);

                getActivity().startActivityForResult(Intent.createChooser(intent,"Select image"), GeneralConstants.RQ_CODE_FOR_SELECTIMAGE);

            }
        });

        btnUserProfile.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                imgUserProfile.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_person));

                if(context.getLoggedAccount()!=null) {
                    ImageLoader.downloadAccountAvtar(imgUserProfile, context.getLoggedAccount().id, ImageLoader.ImageType.Thumb, 9, new ImageLoader.IImageLoader() {
                        @Override
                        public void imageStatus(int imageId, int percentage) {

                        }

                        @Override
                        public void imageUpdated(int imageId, ImageLoader imageLoader) {
                            if (imageLoader.success && imageLoader.bitmap != null) {
                                imgUserProfile.setImageBitmap(imageLoader.bitmap);
                            }
                        }

                    }).startLoader();
                }

                selectedProfilePicPath =null;

                return true;
            }
        });
    }

    private void updateComponents() {
        if(context.getLoggedProfile()!=null) {
            updateComponents(context.getLoggedProfile());
        } else if(context.getLoggedAccount()!=null) {
            updateComponents(context.getLoggedAccount());
        }
        selectedBitmap=null;
        selectedProfilePicPath=null;
    }

    private void updateComponents(Profile profile) {

        imgUserProfile.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_person));
        ImageLoader.downloadProfilePicture(imgUserProfile, ImageLoader.ImageType.Thumb, 9, new ImageLoader.IImageLoader() {
            @Override
            public void imageStatus(int imageId, int percentage) {

            }

            @Override
            public void imageUpdated(int imageId, ImageLoader imageLoader) {
                if(imageLoader.success && imageLoader.bitmap!=null) {
                    imgUserProfile.setImageBitmap(imageLoader.bitmap);
                }
            }

        }).startLoader();

        txtFirstName.setText(profile.first_name);
        txtLastName.setText(profile.last_name);
        txtMobileNumber.setText(profile.mobile_number);
        txtEmailAddress.setText(profile.email_address);

        if(profile.gender== Profile.Gender.Female) {
            radFemale.setChecked(true);
        } else if(profile.gender==Profile.Gender.Male) {
            radMale.setChecked(true);
        } else {
            radOther.setChecked(true);
        }

        if(profile.dateofbirth!=null) {
            Calendar calendar=Calendar.getInstance();
            calendar.setTimeInMillis(profile.dateofbirth.getTime());
            dtDateOfBirth.updateDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        } else {
            Calendar calendar=Calendar.getInstance();
            dtDateOfBirth.updateDate(calendar.getMinimum(Calendar.YEAR),calendar.getMinimum(Calendar.MONTH),calendar.getMinimum(Calendar.DAY_OF_MONTH));
        }

        txtSemester.setText(profile.semester+"");
        txtAddress.setText(profile.address);

    }

    private void updateComponents(Account account) {

        // image loader from profile aspect

        String first_name="";
        String last_name="";

        imgUserProfile.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_person));
        ImageLoader.downloadAccountAvtar(imgUserProfile,account.id, ImageLoader.ImageType.Thumb, 9, new ImageLoader.IImageLoader() {
            @Override
            public void imageStatus(int imageId, int percentage) {

            }

            @Override
            public void imageUpdated(int imageId, ImageLoader imageLoader) {
                if(imageLoader.success && imageLoader.bitmap!=null) {
                    imgUserProfile.setImageBitmap(imageLoader.bitmap);
                }
            }

        }).startLoader();

        if(account.name!=null) {
            int firstSpace=account.name.indexOf(" ");
            if(firstSpace<=-1) {
                first_name=account.name;
            } else {
                first_name=account.name.substring(0,firstSpace);
                last_name=account.name.substring(firstSpace+1,account.name.length());
            }
        }

        txtFirstName.setText(first_name);
        txtLastName.setText(last_name);
        txtMobileNumber.setText(account.mobile_number);
        txtEmailAddress.setText(account.email_address);

        Calendar calendar=Calendar.getInstance();
        dtDateOfBirth.updateDate(calendar.getMinimum(Calendar.YEAR),calendar.getMinimum(Calendar.MONTH),calendar.getMinimum(Calendar.DAY_OF_MONTH));

        txtSemester.setText("0");
        txtAddress.setText("");

    }

    private void saveProfile() {
        if(validateProfileForm()) {

            progressDialog.setMessage("Updating profile...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            final Profile profile=new Profile(new MyJSON("{}"));
            profile.first_name=first_name;
            profile.last_name=last_name;
            profile.mobile_number=realMobileNumber+"";
            profile.email_address=email_address;
            profile.gender=Profile.fromInt(gender);
            profile.dateofbirth=dateOfBirth;
            profile.semester=semester;
            profile.address=address;
            profile.id=0;

            Thread thread=new Thread(new Runnable() {
                @Override
                public void run() {
                    String dataImg=null;

                    if(selectedBitmap!=null) {
                        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
                        if(selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 60,byteArrayOutputStream)) {
                            dataImg= Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
                        }
                    } else if(selectedProfilePicPath !=null) {
                        final File file=new File(selectedProfilePicPath);
                        if(file.exists()) {
                            dataImg=Base64.encodeToString(Helper.readFile(file),Base64.DEFAULT);
                        }
                    }
                    final boolean isImageUploaded=dataImg!=null;
                    JSONLoader.updateProfile(context, profile,dataImg,new JSONLoader.IJSONLoader() {
                        @Override
                        public void jsonStatus(int jsonId, int percentage) {

                        }

                        @Override
                        public void jsonUpdated(int jsonId, final JSONLoader jsonLoader) {

                            if(jsonLoader.success && jsonLoader.myJSON.isReady()) {
                                if(jsonLoader.myJSON.success) {
                                    String profileData=jsonLoader.myJSON.getString("profile");
                                    if(profileData!=null) {
                                        Profile profile1=new Profile(new MyJSON(profileData));
                                        if(profile1.ready) {
                                            Helper.getMainDatabase(context).updateOrAddProfile(profile1);
                                            String msgString="";
                                            if(jsonLoader.myJSON.success_message!=null) {
                                                msgString=jsonLoader.myJSON.success_message;
                                            } else {
                                                if(isNewProfile) {
                                                    msgString="Congratulation, your profile setup is completed.";
                                                } else {
                                                    msgString="Profile Saved.";
                                                }
                                            }

                                            if(isImageUploaded) {
                                                ImageLoader.deleteCache(context, ImageLoader.downloadProfilePicture(imgUserProfile, ImageLoader.ImageType.Thumb, 9, new ImageLoader.IImageLoader() {
                                                    @Override
                                                    public void imageStatus(int imageId, int percentage) {

                                                    }

                                                    @Override
                                                    public void imageUpdated(int imageId, ImageLoader imageLoader) {
                                                        if (imageLoader.success && imageLoader.bitmap != null) {
                                                            imgUserProfile.setImageBitmap(imageLoader.bitmap);
                                                        }
                                                    }

                                                }));
                                            }
                                            context.isProfileExists(true);

                                            final boolean confirmNewProfile=jsonLoader.myJSON.getBoolean("isNewProfile",false);
                                            final String message=msgString;
                                            context.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(context, message,Toast.LENGTH_SHORT).show();
                                                    if(confirmNewProfile) {
                                                        //set flag for new profile confirmation or to reload timeline fragment again
                                                        context.showMyTimeline();
                                                    }
                                                }
                                            });
                                        }
                                    }

                                    // finish this and go back
                                } else {
                                    context.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            String message = jsonLoader.myJSON.error_message != null ? jsonLoader.myJSON.error_message + "\n" : "";
                                            Helper.showAlertDialog(context, message + "Unable to save profile, please try again.", "Ok", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                        }
                                    });
                                }
                            }

                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (progressDialog.isShowing())
                                        progressDialog.dismiss();
                                }
                            });
                        }

                    });
                }
            });
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
        }
    }

    private boolean validateProfileForm() {

        first_name=txtFirstName.getText().toString().trim();
        last_name=txtLastName.getText().toString().trim();
        mobile_number=txtMobileNumber.getText().toString().trim();
        email_address=txtEmailAddress.getText().toString().trim();
        gender=radMale.isChecked()?1:(radFemale.isChecked()?2:(radOther.isChecked()?0:-1));
        dateOfBirth=Helper.Util.toDate(dtDateOfBirth.getYear(),dtDateOfBirth.getMonth(),dtDateOfBirth.getDayOfMonth());
        semester=0;
        try {
            semester=Integer.parseInt(txtSemester.getText().toString());
        } catch(Exception ex) {}
        address=txtAddress.getText().toString().trim();

        realMobileNumber=0;

        if(first_name.length()==0) {
            setEditTextError("Enter First Name!",txtFirstName);
        } else if(last_name.length()==0) {
            setEditTextError("Enter Last Name!",txtLastName);
        } else if(mobile_number.length()==0) {
            setEditTextError("Enter Mobile Number!",txtMobileNumber);
        } else if(mobile_number.length()>0) {
            try {
                realMobileNumber=Long.parseLong(mobile_number);
            } catch (Exception ex) {
                setEditTextError("Invalid Mobile Number!!",txtMobileNumber);
                return false;
            }

            if(email_address.length()==0) {
                setEditTextError("Enter E-mail Address!!", txtEmailAddress);
            } else if(email_address.length()>0) {
                if(!Helper.Util.isEmail(email_address)) {
                    setEditTextError("Invalid E-mail Address!!", txtEmailAddress);
                } else {

                    if(gender==-1) {
                        Toast.makeText(context,"Please select gender.",Toast.LENGTH_SHORT).show();
                    } else if(semester==0) {
                        setEditTextError("Enter Semester!!",txtSemester);
                    } else if(!(semester>0 && semester<10)) {
                        setEditTextError("Invalid Semester, must be between 1 to 9",txtSemester);
                    } else {
                        return true;
                    }

                }
            }

        }

        return false;
    }

    private void setEditTextError(String message, EditText editText) {
        editText.requestFocus();
        editText.setError(message);
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

        int id = item.getItemId();

        if(id==R.id.action_save) {
            saveProfile();
        }

        return false;
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, final Intent data) {
        if(resultCode==context.RESULT_OK) {
            if(requestCode==GeneralConstants.RQ_CODE_FOR_SELECTIMAGE && data!=null) {
                progressDialog.setMessage("Loading image...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                Thread thread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        selectedBitmap=Helper.Util.getBitmapByUriFromMedia(context, data.getData());
                        selectedProfilePicPath =Helper.Util.getFilepathByUriFromMedia(context,data.getData());
                        final Bitmap bitmap=Helper.Images.scaleBitmap(selectedBitmap,256,256,false);
                        if(bitmap!=null) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    imgUserProfile.setImageBitmap(bitmap);
                                    progressDialog.dismiss();
                                }
                            });
                        } else {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Unable to load image, please try again.", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    }
                });
                thread.start();
            }
        }
    }
}
