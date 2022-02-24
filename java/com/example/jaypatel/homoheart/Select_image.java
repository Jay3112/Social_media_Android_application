package com.example.jaypatel.homoheart;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

import cz.msebera.android.httpclient.Header;

public class Select_image extends AppCompatActivity{

    TextView setTitle;
    Button chooseBtn, setBtn;
    ImageView setImage;
    String url, imageurl, logMail;

    Chk_Network chk_network;
    RequestParams requestParams;
    AsyncHttpClient asyncHttpClient;
    ProgressDialog mProgressDialogue;

    public Uri filepath;
    public Bitmap bitmap;
    private  static final int PICK_IMAGE_REQUEST =234;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_image);
        getSupportActionBar().hide();

        setTitle = findViewById(R.id.dilogBoxTitle);
        chooseBtn = findViewById(R.id.chooseBtn);
        setBtn = findViewById(R.id.setBtn);
        setImage = findViewById(R.id.custDialogImg);

        SharedPreferences prefs = getSharedPreferences("login_content", MODE_PRIVATE);
        logMail = prefs.getString("logMail", null);

        chk_network = new Chk_Network();
        requestParams = new RequestParams();
        asyncHttpClient = new AsyncHttpClient();
        mProgressDialogue = new ProgressDialog(this);

        setTitle.setText("Select image");
        chooseBtn.setText("Galary");
        setBtn.setText("Set Image");

        if(chk_network.isNetworkAvailable(Select_image.this)){
            getProfileImage();
        }else{
            setImage.setImageResource(R.drawable.logo);
        }

        chooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(chk_network.isNetworkAvailable(Select_image.this)){
                    //showFileChooser
                    showFileChooser();
                }
                else {
                    Toast.makeText(Select_image.this, "Turn on internet!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(chk_network.isNetworkAvailable(Select_image.this)){
                    //send image on server
                    setProfileImage();
                }
                else {
                    Toast.makeText(Select_image.this, "Turn on internet!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void showFileChooser(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent,"Select image"),PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null ){
            filepath = data.getData();
            try{
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filepath);
                setImage.setImageBitmap(bitmap);
            }catch (Exception e){

            }
        }
    }

    public void getProfileImage(){
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
                        if (mProgressDialogue.isShowing()) {
                            mProgressDialogue.dismiss();
                        }
                        imageurl = imageurl.concat("/" + jsonObject.getString("profileimage"));
                        Picasso.get().load(imageurl).into(setImage);
                    } else {
                        if (mProgressDialogue.isShowing()) {
                            mProgressDialogue.dismiss();
                        }
                        Toast.makeText(Select_image.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
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

    public void setProfileImage() {
        urlValue values = new urlValue();
        url = values.setProfileImage();
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

        String path = getPath(filepath);
        requestParams.put("email", logMail);
        try {
            requestParams.put("image", new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
        }
        asyncHttpClient.post(url, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    if (jsonObject.getString("error").matches("false")) {
                        Toast.makeText(Select_image.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                        if (mProgressDialogue.isShowing()) {
                            mProgressDialogue.dismiss();
                        }
                    } else {
                        if (mProgressDialogue.isShowing()) {
                            mProgressDialogue.dismiss();
                        }
                        Toast.makeText(Select_image.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
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
}
