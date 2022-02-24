package com.example.jaypatel.homoheart;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class Edit_Profile extends AppCompatActivity{

    EditText profName, profDOB, profWeb, profBio, profContactNo, profEmail;
    ImageView profImage;
    final Calendar myCalendar = Calendar.getInstance();
    String url, imageurl;
    ProgressDialog mProgressDialogue;
    Chk_Network checkNetwork;
    AsyncHttpClient asyncHttpClient;
    RequestParams requestParams;

    private static final int REQUEST_PERMISSION = 10;
    private SparseIntArray mErrorString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);
        getSupportActionBar().setTitle("Edit Profile");

        profName = (EditText) findViewById(R.id.profName);
        profDOB = (EditText) findViewById(R.id.profDOB);
        profWeb = (EditText) findViewById(R.id.profWeb);
        profBio = (EditText) findViewById(R.id.profBio);
        profContactNo = (EditText) findViewById(R.id.profContactNumber);
        profEmail = (EditText) findViewById(R.id.profMail);
        profImage = (ImageView)findViewById(R.id.profileImage);

        checkNetwork = new Chk_Network();
        asyncHttpClient = new AsyncHttpClient();
        requestParams = new RequestParams();
        mErrorString = new SparseIntArray();
        mProgressDialogue = new ProgressDialog(this);

        //Date picker
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };
        profDOB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(Edit_Profile.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        profImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //cust dialog to set img
                requestAppPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA},
                                R.string.msg,REQUEST_PERMISSION);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkNetwork.isNetworkAvailable(Edit_Profile.this)) {
            getProfileData();
        } else {
            profImage.setImageResource(R.drawable.logo);
            Toast.makeText(Edit_Profile.this, "Turn on internet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_profile_menu, menu);
        return true;
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        profDOB.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.tickIcon:
                //Validation of Userdata
                if(profName.length() != 0){
                    if(profContactNo.length() == 10){
                        updateProfileData();
                    }else{
                        profContactNo.setError("Invalid mobile number");
                    }
                }else {
                    profName.setError("Username required");
                }
                break;
            default:
                break;
        }
        return true;
    }

    public void getProfileData(){
        SharedPreferences prefs = getSharedPreferences("login_content", MODE_PRIVATE);
        String logMail = prefs.getString("logMail", null);
        urlValue values = new urlValue();
        url = values.getProfileDataUrl();
        imageurl = values.getProfImage();
        mProgressDialogue.setTitle("Loading...");
        mProgressDialogue.setMessage("Please wait...");
        mProgressDialogue.setIndeterminate(true);
        mProgressDialogue.setCanceledOnTouchOutside(false);
        mProgressDialogue.show();
        if(mProgressDialogue.isShowing()){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    mProgressDialogue.dismiss();
                }
            }, 6000);
        }
        requestParams.put("email", logMail);
        asyncHttpClient.post(url, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    if (jsonObject.getString("error").matches("false")) {
                        if(mProgressDialogue.isShowing()){
                            mProgressDialogue.dismiss();
                        }
                        profName.setText(jsonObject.getString("name"));
                        profEmail.setText(jsonObject.getString("email"));
                        profBio.setText(jsonObject.getString("bio"));
                        profWeb.setText(jsonObject.getString("website"));
                        profDOB.setText(jsonObject.getString("birthdate"));
                        profContactNo.setText(jsonObject.getString("contactnumber"));
                        if(jsonObject.getString("profileimage").isEmpty()){
                            profImage.setImageResource(R.drawable.logo);
                        }else{
                            imageurl = imageurl.concat("/"+jsonObject.getString("profileimage"));
                            Picasso.get().load(imageurl).into(profImage);
                        }
                    } else {
                        if(mProgressDialogue.isShowing()){
                            mProgressDialogue.dismiss();
                        }
                        Toast.makeText(Edit_Profile.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    public void updateProfileData() {
        urlValue values = new urlValue();
        url = values.updateProfileDataUrl();
        mProgressDialogue.setTitle("Loading...");
        mProgressDialogue.setMessage("Please wait...");
        mProgressDialogue.setIndeterminate(true);
        mProgressDialogue.setCanceledOnTouchOutside(false);
        mProgressDialogue.show();
        if (mProgressDialogue.isShowing()) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    mProgressDialogue.dismiss();
                }
            }, 6000);
        }

        requestParams.put("email", profEmail.getEditableText().toString());
        requestParams.put("username", profName.getEditableText().toString());
        requestParams.put("DOB", profDOB.getEditableText().toString());
        requestParams.put("website", profWeb.getEditableText().toString());
        requestParams.put("bio", profBio.getEditableText().toString());
        requestParams.put("contactnumber", profContactNo.getEditableText().toString());
        asyncHttpClient.post(url, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    if (jsonObject.getString("error").matches("false")) {
                        if (mProgressDialogue.isShowing()) {
                            mProgressDialogue.dismiss();
                        }
                        Toast.makeText(Edit_Profile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        ProfileTab1 fragment = new ProfileTab1();
                        getSupportFragmentManager()
                                .beginTransaction()
                                .add(R.id.editProf, fragment)
                                .commit();
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    } else {
                        if (mProgressDialogue.isShowing()) {
                            mProgressDialogue.dismiss();
                        }
                        Toast.makeText(Edit_Profile.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    public void requestAppPermissions(final String[]requestedPermissions, final int stringId, final int requestCode){
        mErrorString.put(requestCode,stringId);

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        boolean showRequstPermissions = false;
        for(String permission: requestedPermissions){
            permissionCheck =permissionCheck + ContextCompat.checkSelfPermission(this,permission);
            showRequstPermissions = showRequstPermissions || ActivityCompat.shouldShowRequestPermissionRationale(this,permission);
        }

        if(permissionCheck!=PackageManager.PERMISSION_GRANTED){
            if(showRequstPermissions){
                Snackbar.make(findViewById(android.R.id.content), stringId, Snackbar.LENGTH_INDEFINITE).setAction("GRANT", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(Edit_Profile.this, requestedPermissions, requestCode);
                    }
                }).show();
            }else{
                ActivityCompat.requestPermissions(this, requestedPermissions, requestCode);
            }
        }else{
            Intent launchEditProfile= new Intent(Edit_Profile.this,Select_image.class);
            startActivity(launchEditProfile);
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for(int permission: grantResults){
            permissionCheck = permissionCheck + permission;
        }

        if((grantResults.length > 0 )  && PackageManager.PERMISSION_GRANTED == permissionCheck){
            Intent launchEditProfile= new Intent(Edit_Profile.this,Select_image.class);
            startActivity(launchEditProfile);
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        }else {
            //Display message when contain some dangerous permission not accept
            Snackbar.make(findViewById(android.R.id.content), mErrorString.get(requestCode),
                    Snackbar.LENGTH_INDEFINITE).setAction("ENABLE", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent();
                    i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    i.setData(Uri.parse("package:" + getPackageName()));
                    i.addCategory(Intent.CATEGORY_DEFAULT);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(i);
                }
            }).show();
        }
    }
}