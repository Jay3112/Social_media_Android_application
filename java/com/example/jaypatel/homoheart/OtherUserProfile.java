package com.example.jaypatel.homoheart;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class OtherUserProfile extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    String userMail;
    Menu menu;
    BottomNavigationView navigation3;

    Button FrndStatus;
    String url, imageurl, logMail, frndStatus;
    ImageView UsrProfImage;
    TextView UsrProfileName, UsrProfileBio, UsrfrndCnt, UsrpostCnt, hintTeaxt;
    ScrollView forScroll;
    FrameLayout frame;

    Chk_Network chk_network;
    ProgressDialog mProgressDialogue;
    RequestParams requestParams;
    AsyncHttpClient asyncHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_user_profile);

        SharedPreferences prefs = getSharedPreferences("login_content", MODE_PRIVATE);
        logMail = prefs.getString("logMail", null);

        final Intent intent = getIntent();
        userMail = intent.getStringExtra("userMail");

        navigation3 = (BottomNavigationView)findViewById(R.id.navigation3);
        navigation3.setOnNavigationItemSelectedListener(this);
        menu = navigation3.getMenu();
        onNavigationItemSelected(menu.getItem(0));

        FrndStatus = (Button)findViewById(R.id.frndStatusBtn);
        UsrProfileName = (TextView)findViewById(R.id.usr_profileName);
        UsrProfileBio = (TextView)findViewById(R.id.usr_profileBio);
        UsrfrndCnt = (TextView)findViewById(R.id.usr_frnd_tv);
        UsrpostCnt = (TextView)findViewById(R.id.usr_post_tv);
        UsrProfImage = (ImageView)findViewById(R.id.usr_prof_image);
        forScroll = (ScrollView) findViewById(R.id.other_usr_scroll);
        frame = (FrameLayout) findViewById(R.id.fragment_container3);
        hintTeaxt = (TextView) findViewById(R.id.hintText);

        mProgressDialogue = new ProgressDialog(OtherUserProfile.this);
        asyncHttpClient = new AsyncHttpClient();
        requestParams = new RequestParams();
        chk_network = new Chk_Network();

        UsrProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show all images
                Intent launchShowAllImg= new Intent(OtherUserProfile.this,ShowAllProfImages.class);
                launchShowAllImg.putExtra("userMail",userMail);
                startActivity(launchShowAllImg);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

        FrndStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(frndStatus.matches("send req")){
                    updateNotification(userMail,"request","friend");
                }else if(frndStatus.matches("accept")){
                    updateNotification(userMail,"friend","friend");
                }else if(frndStatus.matches("requested")){
                    updateNotification(userMail,"none","friend");
                }else if(frndStatus.matches("friend")){
                    updateNotification(userMail,"none","friend");
                }
            }
        });
    }

    @Override
    public void onResume() {
        if(chk_network.isNetworkAvailable(OtherUserProfile.this)){
            UsrProfImage.setClickable(false);
            setProfileData();
            setFrndStatus();
            UsrProfileBio.setText(null);
            super.onResume();
        }else{
            UsrProfImage.setImageResource(R.drawable.logo);
            Toast.makeText(OtherUserProfile.this, "Turn on internet", Toast.LENGTH_SHORT).show();
            super.onResume();
        }
    }

    public void setProfileData(){
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

        requestParams.put("email", userMail);
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
                        UsrProfileName.setText(jsonObject.getString("name"));
                        getSupportActionBar().setTitle(jsonObject.getString("name"));
                        if(!jsonObject.getString("birthdate").isEmpty()){
                            UsrProfileBio.append("Birthdate :"+jsonObject.getString("birthdate")+"\n");
                        }
                        if(jsonObject.getString("bio") != null){
                            UsrProfileBio.append(jsonObject.getString("bio")+"\n");
                        }
                        if(jsonObject.getString("website") != null){
                            UsrProfileBio.append(jsonObject.getString("website"));
                        }
                        if(jsonObject.getString("profileimage").isEmpty()){
                            UsrProfImage.setImageResource(R.drawable.logo);
                        }else{
                            UsrProfImage.setClickable(true);
                            imageurl = imageurl.concat("/"+jsonObject.getString("profileimage"));
                            Picasso.get().load(imageurl).into(UsrProfImage);
                        }
                        UsrfrndCnt.setText(jsonObject.getString("frnds")+"  friends");
                        UsrpostCnt.setText(jsonObject.getString("posts")+"  posts");
                    } else {
                        if(mProgressDialogue.isShowing()){
                            mProgressDialogue.dismiss();
                        }
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

    public void setFrndStatus() {
        urlValue values = new urlValue();
        url = values.getFrndStatus();
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
        requestParams.put("user_mail", userMail);
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
                        frndStatus = jsonObject.getString("frnd_status");
                        if(frndStatus.matches("send req")){
                            FrndStatus.setText("Add friend");
                            FrndStatus.setBackgroundResource(R.drawable.friendlist_button);
                            FrndStatus.setTextColor(Color.parseColor("#ffffff"));
                        }else if(frndStatus.matches("accept")){
                            FrndStatus.setText("Accept");
                            FrndStatus.setTextColor(Color.parseColor("#000000"));
                            FrndStatus.setBackgroundResource(R.drawable.custom_dialog_button);
                        }else if(frndStatus.matches("requested")){
                            FrndStatus.setText("Cancel Req");
                            FrndStatus.setTextColor(Color.parseColor("#000000"));
                            FrndStatus.setBackgroundResource(R.drawable.custom_dialog_button);
                        }else if(frndStatus.matches("friend")){
                            FrndStatus.setText("Unfriend");
                            FrndStatus.setTextColor(Color.parseColor("#000000"));
                            FrndStatus.setBackgroundResource(R.drawable.custom_dialog_button);
                        }

                        if(!frndStatus.matches("friend")){
                            frame.setVisibility(View.GONE);
                            hintTeaxt.setText("Private information"+"\n"+"Send friend request to be friend.");
                        }else{
                            hintTeaxt.setVisibility(View.GONE);
                        }

                    } else {
                        if (mProgressDialogue.isShowing()) {
                            mProgressDialogue.dismiss();
                        }
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

    public void updateNotification(String eventUserMail,String notificSubtype, String notificType){
        SharedPreferences prefs = getSharedPreferences("login_content", MODE_PRIVATE);
        String logMail = prefs.getString("logMail", null);
        urlValue values = new urlValue();
        url = values.updateNotification();

        requestParams.put("email", logMail);
        requestParams.put("event_id","");
        requestParams.put("receiver_mail",eventUserMail);
        requestParams.put("type",notificType);
        requestParams.put("subtype",notificSubtype);
        asyncHttpClient.post(url, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    if (jsonObject.getString("error").matches("false")) {
                        if(jsonObject.getString("error").matches("false")){
                            onResume();
                        }else{
                            Toast.makeText(OtherUserProfile.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(OtherUserProfile.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
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

    public boolean loadFragment (Fragment fragment){
        if(fragment != null){
            Bundle arguments = new Bundle();
            arguments.putString( "userMail" , userMail);
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container3,fragment).commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragment = null;
        menu = navigation3.getMenu();

        switch (menuItem.getItemId()){
            case R.id.posts:
                fragment = new OtherUsrPosts();
                menuItem.setChecked(true);
                break;

            case R.id.friends:
                fragment = new OtherUsrFrndList();
                menuItem.setChecked(false);
                break;
        }
        return loadFragment(fragment);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
