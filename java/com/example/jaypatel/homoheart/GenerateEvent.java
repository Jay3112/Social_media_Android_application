package com.example.jaypatel.homoheart;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class GenerateEvent extends AppCompatActivity {

    ArrayList<String> imageUri = new ArrayList<String>();
    ViewPager viewPager;
    ViewImageAdapterGalary adapter;

    private static final int REQUEST_PERMISSION = 10;
    private SparseIntArray mErrorString;

    LinearLayout addImage;
    TabLayout tabLayout;
    EditText addTitle, addDesc, eventDate;
    Button post;

    String url;
    Chk_Network check_Network;
    ProgressDialog mProgressDialogue;
    AsyncHttpClient asyncHttpClient;
    RequestParams requestParams;
    final Calendar myCalendar = Calendar.getInstance();

    public Uri filepath;
    public Bitmap bitmap;
    private  static final int PICK_IMAGE_REQUEST =234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_event);

        viewPager = findViewById(R.id.addevent_viewpager);
        addTitle = (EditText) findViewById(R.id.addTitle);
        addDesc = (EditText)findViewById(R.id.addDesc);
        eventDate = (EditText)findViewById(R.id.eventDate);
        addImage = (LinearLayout) findViewById(R.id.addImage);
        post = (Button)findViewById(R.id.btnPost);

        check_Network = new Chk_Network();
        asyncHttpClient = new AsyncHttpClient();
        requestParams = new RequestParams();
        mErrorString = new SparseIntArray();
        mProgressDialogue = new ProgressDialog(this);

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
        eventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(GenerateEvent.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAppPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA},
                        R.string.msg,REQUEST_PERMISSION);
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(check_Network.isNetworkAvailable(GenerateEvent.this)){
                    uploadEvent();
                }else{
                    Toast.makeText(GenerateEvent.this, "Please turn on internet", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void showFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent,"Select image"),PICK_IMAGE_REQUEST);
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        eventDate.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null){
            // removed && data.getData() != null
            ClipData clipData = data.getClipData();
            if(clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    filepath = item.getUri();
                    try{
                        File file = new File(getPath(filepath));
                        imageUri.add(0,Uri.fromFile(file).toString());
                    }catch (Exception e){

                    }
                }
            }else{
                filepath = data.getData();
                try{
                    File file = new File(getPath(filepath));
                    imageUri.add(0,Uri.fromFile(file).toString());
                }catch (Exception e){

                }
            }
            adapter = new ViewImageAdapterGalary(GenerateEvent.this, imageUri);
            viewPager.setAdapter(adapter);
            tabLayout = (TabLayout) findViewById(R.id.tab_layout);
            tabLayout.setupWithViewPager(viewPager, true);
        }
    }

    private String getPath(Uri uri){
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);

        document_id = document_id.substring(document_id.lastIndexOf(":")+1);
        cursor.close();

        cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,null,
                MediaStore.Images.Media._ID + " = ?", new String[]{document_id},null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();
        return path;
    }

    @Override
    public void onBackPressed() {
        //Custom dialog are you sure back ???
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    public void uploadEvent() {
        SharedPreferences prefs = getSharedPreferences("login_content", MODE_PRIVATE);
        String logMail = prefs.getString("logMail", null);
        urlValue values = new urlValue();
        url = values.addNewEvent();

        File[] files = new File[imageUri.size()];
        for(int i=0; i<imageUri.size(); i++){
            String substr=imageUri.get(i).substring(7);
            files[i] = new File(substr);
        }

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

        requestParams.put("email", logMail);
        requestParams.put("title",addTitle.getEditableText().toString());
        requestParams.put("desc",addDesc.getEditableText().toString());
        requestParams.put("event_date",eventDate.getEditableText().toString());
        try {
            requestParams.put("image[]",files);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "files not found", Toast.LENGTH_SHORT).show();
        }
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
                        Toast.makeText(GenerateEvent.this, "Event posted", Toast.LENGTH_SHORT).show();
                        //intent to home
                        Intent launchMain= new Intent(GenerateEvent.this,Main2Activity.class);
                        startActivity(launchMain);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    } else {
                        if (mProgressDialogue.isShowing()) {
                            mProgressDialogue.dismiss();
                        }
                        Toast.makeText(GenerateEvent.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
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
                        ActivityCompat.requestPermissions(GenerateEvent.this, requestedPermissions, requestCode);
                    }
                }).show();
            }else{
                ActivityCompat.requestPermissions(this, requestedPermissions, requestCode);
            }
        }else{
            showFileChooser();
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
            showFileChooser();
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
