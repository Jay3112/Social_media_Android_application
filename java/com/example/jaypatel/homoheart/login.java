package com.example.jaypatel.homoheart;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class login extends AppCompatActivity {
    Button loginBtn;
    EditText loginMail, loginPass;
    TextView newSignup, tiltType;
    ImageView mailImg, passImg;
    int setType;
    String url;
    Custom_DialogBox custom_dialogBox;
    Chk_Network checkNetwork;
    ProgressDialog mProgressDialogue;
    AsyncHttpClient asyncHttpClient;
    RequestParams requestParams;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        setType = 1;
        loginBtn = (Button)findViewById(R.id.loginbtn);
        loginMail = (EditText)findViewById(R.id.loginMail);
        loginPass = (EditText)findViewById(R.id.loginPass);
        newSignup = (TextView)findViewById(R.id.newUsr);
        tiltType = (TextView)findViewById(R.id.tiltType);
        mailImg = (ImageView)findViewById(R.id.mailImg);
        passImg = (ImageView)findViewById(R.id.passImg);

        checkNetwork = new Chk_Network();
        asyncHttpClient = new AsyncHttpClient();
        requestParams = new RequestParams();
        custom_dialogBox = new Custom_DialogBox();
        mProgressDialogue = new ProgressDialog(this);

        loginMail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    Animation animation = AnimationUtils.loadAnimation(login.this,R.anim.bounce);
                    mailImg.startAnimation(animation);
                }
            }
        });

        loginPass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    Animation animation = AnimationUtils.loadAnimation(login.this,R.anim.bounce);
                    passImg.startAnimation(animation);
                }
            }
        });

        tiltType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(setType == 1){
                    setType = 0;
                    loginPass.setTransformationMethod(null);
                    if(loginPass.getEditableText().length()>0){
                        loginPass.setSelection(loginPass.getEditableText().length());
                    }
                    tiltType.setBackgroundResource(R.drawable.ic_visibility_gray_24dp);
                }
                else{
                    setType = 1;
                    loginPass.setTransformationMethod(new PasswordTransformationMethod());
                    if(loginPass.getEditableText().length()>0){
                        loginPass.setSelection(loginPass.getEditableText().length());
                    }
                    tiltType.setBackgroundResource(R.drawable.ic_visibility_off_gray_24dp);
                }
            }
        });

        SharedPreferences prefs = getSharedPreferences("login_content", MODE_PRIVATE);
        String logMail = prefs.getString("logMail", null);
        String logPass = prefs.getString("logPass",null);
        if(logMail!= null && logPass!=null){
            Intent launchMain= new Intent(login.this,Main2Activity.class);
            startActivity(launchMain);
            finish();
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String loginMailval = loginMail.getEditableText().toString();
                if(loginMailval != null){
                    if(!loginPass.getEditableText().toString().isEmpty())
                    {
                        if(checkNetwork.isNetworkAvailable(login.this)){
                            userLogin();
                        }
                        else{
                            Toast.makeText(login.this, "Turn on internet", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        loginPass.setError("Enter password");
                    }
                }
                else{
                    loginMail.setError("Enter correct mail-Id");
                }
            }
        });

        newSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newSignup.setTextColor(Color.parseColor("#000000"));
                Intent launchSignup= new Intent(login.this,signup.class);
                startActivity(launchSignup);
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    public void userLogin() {
        urlValue values = new urlValue();
        url = values.getLoginUrl();
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
        requestParams.put("email", loginMail.getText().toString());
        requestParams.put("password", loginPass.getText().toString());
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
                        //Toast.makeText(login.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        SharedPreferences.Editor editor = getSharedPreferences("login_content", MODE_PRIVATE).edit();
                        editor.putString("logMail", loginMail.getEditableText().toString());
                        editor.putString("logPass", loginPass.getEditableText().toString());
                        editor.commit();
                        checkProfileIsSet();
                    }else if(jsonObject.getString("message").matches("Verification remaining!")){
                        custom_dialogBox.showDialog(login.this,2,"Need email verification", loginMail.getText().toString());
                    } else {
                        if(mProgressDialogue.isShowing()){
                            mProgressDialogue.dismiss();
                        }
                        Toast.makeText(login.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(login.this, "Login Unsuccessful", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkProfileIsSet() {
        urlValue values = new urlValue();
        url = values.profileCheckUrl();
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
        requestParams.put("email", loginMail.getText().toString());
        asyncHttpClient.post(url, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    if (jsonObject.getString("message").matches("set profile!")) {
                        if(mProgressDialogue.isShowing()){
                            mProgressDialogue.dismiss();
                        }
                        //Toast.makeText(login.this,jsonObject.getString("message") , Toast.LENGTH_SHORT).show();
                        Intent launchEditProfile= new Intent(login.this,Edit_Profile.class);
                        startActivity(launchEditProfile);
                        finish();
                        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    }else if(jsonObject.getString("message").matches("Profile already set")){
                        //Toast.makeText(login.this, "Intent to home", Toast.LENGTH_SHORT).show();
                        Intent launchMain2Activity= new Intent(login.this,Main2Activity.class);
                        startActivity(launchMain2Activity);
                        finish();
                        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    } else {
                        if(mProgressDialogue.isShowing()){
                            mProgressDialogue.dismiss();
                        }
                        Toast.makeText(login.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
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
