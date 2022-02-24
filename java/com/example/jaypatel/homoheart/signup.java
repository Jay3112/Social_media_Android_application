package com.example.jaypatel.homoheart;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class signup extends AppCompatActivity implements View.OnFocusChangeListener {

    ImageButton submitBtn, cancelBtn;
    EditText signupMail, signupName, signupPass, signupMob, confPass;
    TextView tiltType1, tiltType2;
    ImageView signNameImg, signPassImg, signMailImg, signMobImg, passConfImg;
    int setType1, setType2;
    String url;
    Chk_Network checkNetwork;
    Custom_DialogBox custom_dialog;
    AsyncHttpClient asyncHttpClient;
    RequestParams requestParams;
    ProgressDialog mProgressDialogue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        setType1 = setType2 = 1;
        final String mailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        final String mailPattern2 = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+\\.+[a-z]+";
        final String passPattern = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";

        submitBtn = (ImageButton) findViewById(R.id.submitbtn);
        cancelBtn = (ImageButton) findViewById(R.id.cancelbtn);
        signupMail = (EditText) findViewById(R.id.signupMail);
        signupName = (EditText) findViewById(R.id.signupName);
        signupPass = (EditText) findViewById(R.id.signupPass);
        confPass = (EditText) findViewById(R.id.signupConfPass);
        signupMob = (EditText) findViewById(R.id.signupMob);
        tiltType1 = (TextView) findViewById(R.id.tiltType1);
        tiltType2 = (TextView) findViewById(R.id.tiltType2);

        custom_dialog = new Custom_DialogBox();
        asyncHttpClient = new AsyncHttpClient();
        requestParams = new RequestParams();
        checkNetwork = new Chk_Network();
        mProgressDialogue = new ProgressDialog(this);

        urlValue values = new urlValue();
        url = values.getRegisterUrl();

        signNameImg = (ImageView) findViewById(R.id.nameImg);
        signMailImg = (ImageView) findViewById(R.id.mailImg);
        signPassImg = (ImageView) findViewById(R.id.passImg);
        passConfImg = (ImageView) findViewById(R.id.passConfImg);
        signMobImg = (ImageView) findViewById(R.id.mobImg);

        signupName.setOnFocusChangeListener(this);
        signupMail.setOnFocusChangeListener(this);
        signupPass.setOnFocusChangeListener(this);
        confPass.setOnFocusChangeListener(this);
        signupMob.setOnFocusChangeListener(this);

        tiltType1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(setType1 == 1){
                    setType1 = 0;
                    signupPass.setTransformationMethod(null);
                    if(signupPass.getEditableText().length()>0){
                        signupPass.setSelection(signupPass.getEditableText().length());
                    }
                    tiltType1.setBackgroundResource(R.drawable.ic_visibility_gray_24dp);
                }
                else{
                    setType1 = 1;
                    signupPass.setTransformationMethod(new PasswordTransformationMethod());
                    if(signupPass.getEditableText().length()>0){
                        signupPass.setSelection(signupPass.getEditableText().length());
                    }
                    tiltType1.setBackgroundResource(R.drawable.ic_visibility_off_gray_24dp);
                }
            }
        });

        tiltType2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(setType2 == 1){
                    setType2 = 0;
                    confPass.setTransformationMethod(null);
                    if(confPass.getEditableText().length()>0){
                        confPass.setSelection(confPass.getEditableText().length());
                    }
                    tiltType2.setBackgroundResource(R.drawable.ic_visibility_gray_24dp);
                }
                else{
                    setType2 = 1;
                    confPass.setTransformationMethod(new PasswordTransformationMethod());
                    if(confPass.getEditableText().length()>0){
                        confPass.setSelection(confPass.getEditableText().length());
                    }
                    tiltType2.setBackgroundResource(R.drawable.ic_visibility_off_gray_24dp);
                }
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String loginMailval = signupMail.getEditableText().toString();
                String mobValue = signupMob.getEditableText().toString();
                String passValue = signupPass.getEditableText().toString();
                String confPassValue = confPass.getEditableText().toString();

                if(loginMailval.matches(mailPattern) || loginMailval.matches(mailPattern2)){
                    if (passValue.matches(passPattern)) {
                        if(passValue.matches(confPassValue)){
                            if (mobValue.length() == 10) {
                                if(checkNetwork.isNetworkAvailable(signup.this)){
                                    insertData();
                                }
                                else{
                                    Toast.makeText(signup.this, "Turn on internet", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                signupMob.setError("Enter correct Mobile No.");
                            }
                        }
                        else{
                            confPass.setError("Password is different");
                        }
                    } else {
                        signupPass.setError("Enter password which contain: 1 cap, num, special char");
                    }
                } else {
                    signupMail.setError("Enter correct mail-Id");
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchLogin = new Intent(signup.this, login.class);
                startActivity(launchLogin);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        switch (view.getId()) {
            case R.id.signupName:
                if (hasFocus) {
                    Animation animation = AnimationUtils.loadAnimation(signup.this, R.anim.bounce);
                    signNameImg.startAnimation(animation);
                }
                break;

            case R.id.signupMail:
                if (hasFocus) {
                    Animation animation2 = AnimationUtils.loadAnimation(signup.this, R.anim.bounce);
                    signMailImg.startAnimation(animation2);
                }
                break;

            case R.id.signupPass:
                if (hasFocus) {
                    Animation animation3 = AnimationUtils.loadAnimation(signup.this, R.anim.bounce);
                    signPassImg.startAnimation(animation3);
                }
                break;

            case R.id.signupConfPass:
                if (hasFocus) {
                    Animation animation3 = AnimationUtils.loadAnimation(signup.this, R.anim.bounce);
                    passConfImg.startAnimation(animation3);
                }
                break;

            case R.id.signupMob:
                if (hasFocus) {
                    Animation animation4 = AnimationUtils.loadAnimation(signup.this, R.anim.bounce);
                    signMobImg.startAnimation(animation4);
                }
                break;
        }
    }
    @Override
    public void onBackPressed() {
        Intent launchLogin = new Intent(signup.this, login.class);
        startActivity(launchLogin);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void insertData(){
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
        requestParams.put("username",signupName.getText().toString());
        requestParams.put("email",signupMail.getText().toString());
        requestParams.put("password",signupPass.getText().toString());
        requestParams.put("contactnumber",signupMob.getText().toString());
        asyncHttpClient.post(url, requestParams, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    if(jsonObject.getString("error").matches("false")){
                        if(mProgressDialogue.isShowing()){
                            mProgressDialogue.dismiss();
                        }
                        //Toast.makeText(signup.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        custom_dialog.showDialog(signup.this,2,"Need email verification", signupMail.getText().toString());
                    }
                    else{
                        if(mProgressDialogue.isShowing()){
                            mProgressDialogue.dismiss();
                        }
                        Toast.makeText(signup.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(signup.this, "Signup Unsuccessful", Toast.LENGTH_SHORT).show();
            }
        });
    }
}