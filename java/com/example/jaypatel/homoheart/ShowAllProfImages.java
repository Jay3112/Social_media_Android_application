package com.example.jaypatel.homoheart;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ShowAllProfImages extends AppCompatActivity{

    public String[] imageUrls;
    ViewPager viewPager;
    TabLayout tabLayout;
    ViewImageAdapterServer adapter;
    Chk_Network check_newtwork;
    RequestParams requestParams;
    AsyncHttpClient asyncHttpClient;
    ProgressDialog mProgressDialogue;

    String url, imageurl, logMail, imagebaseurl, userMail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_all_prof_images);
        getSupportActionBar().hide();

        check_newtwork = new Chk_Network();
        requestParams = new RequestParams();
        asyncHttpClient = new AsyncHttpClient();
        mProgressDialogue = new ProgressDialog(this);

        SharedPreferences prefs = getSharedPreferences("login_content", MODE_PRIVATE);
        logMail = prefs.getString("logMail", null);

        Intent intent = getIntent();
        userMail = intent.getStringExtra("userMail");

        if(check_newtwork.isNetworkAvailable(ShowAllProfImages.this)){
            //send image on server
            getAllProfileImage();
        }
        else {
            Toast.makeText(ShowAllProfImages.this, "Turn on internet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void getAllProfileImage(){
        urlValue values = new urlValue();
        url = values.getProfileAllImages();
        imagebaseurl = values.getProfImage();
        mProgressDialogue.setTitle("Loading...");
        mProgressDialogue.setMessage("Please wait...");
        mProgressDialogue.setIndeterminate(true);
        mProgressDialogue.setCanceledOnTouchOutside(false);
        mProgressDialogue.show();
        if(userMail != null){
            logMail = userMail;
        }
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
                        imageUrls = new String[jsonObject.getInt("rows")];
                        String name = null;
                        for(int i=0; i<jsonObject.getInt("rows"); i++){
                            name = "profileimage".concat(Integer.toString(i));
                            imageurl = jsonObject.getString(name);
                            imageurl = imagebaseurl.concat("/"+imageurl);
                            imageUrls[i] = imageurl;
                        }
                        viewPager = findViewById(R.id.view_pager);
                        adapter = new ViewImageAdapterServer(ShowAllProfImages.this, imageUrls);
                        viewPager.setAdapter(adapter);
                        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
                        tabLayout.setupWithViewPager(viewPager, true);
                    } else {
                        if (mProgressDialogue.isShowing()) {
                            mProgressDialogue.dismiss();
                        }
                        Toast.makeText(ShowAllProfImages.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
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
}
