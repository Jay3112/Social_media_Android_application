package com.example.jaypatel.homoheart;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import cz.msebera.android.httpclient.Header;

import static android.content.Context.MODE_PRIVATE;

public class ProfileTab1 extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener {

    Menu menu;
    BottomNavigationView navigation2;

    private static final String TAG = "Profile";
    Button EditProf;
    String url, imageurl, logMail;
    ImageView ProfImage;
    TextView ProfileName, ProfileBio, frndCnt, postCnt;
    Chk_Network chk_network;
    ProgressDialog mProgressDialogue;
    RequestParams requestParams;
    AsyncHttpClient asyncHttpClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_tab, container, false);
        setHasOptionsMenu(true);

        SharedPreferences prefs = getActivity().getSharedPreferences("login_content", MODE_PRIVATE);
        logMail = prefs.getString("logMail", null);

        navigation2 = (BottomNavigationView) view.findViewById(R.id.navigation2);
        navigation2.setOnNavigationItemSelectedListener(this);
        menu = navigation2.getMenu();
        onNavigationItemSelected(menu.getItem(0));

        EditProf = (Button)view.findViewById(R.id.editProfBtn);
        ProfileName = (TextView)view.findViewById(R.id.profileName);
        ProfileBio = (TextView)view.findViewById(R.id.profileBio);
        frndCnt = (TextView)view.findViewById(R.id.frnds_tv);
        postCnt = (TextView)view.findViewById(R.id.post_tv);
        ProfImage = (ImageView)view.findViewById(R.id.profTabImage);

        mProgressDialogue = new ProgressDialog(getActivity());
        asyncHttpClient = new AsyncHttpClient();
        requestParams = new RequestParams();
        chk_network = new Chk_Network();

        EditProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchEditProfile= new Intent(getActivity(),Edit_Profile.class);
                startActivity(launchEditProfile);
                getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

        ProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show all images
                Intent launchShowAllImg= new Intent(getActivity(),ShowAllProfImages.class);
                startActivity(launchShowAllImg);
                getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        if(chk_network.isNetworkAvailable(getActivity())){
            ProfImage.setClickable(false);
            setProfileData();
            ProfileBio.setText(null);
            super.onResume();
        }else{
            ProfImage.setImageResource(R.drawable.logo);
            Toast.makeText(getActivity(), "Turn on internet", Toast.LENGTH_SHORT).show();
            super.onResume();
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.logout_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("login_content", MODE_PRIVATE).edit();
                editor.putString("logMail", null);
                editor.putString("logPass", null);
                editor.commit();
                Intent launchLogin= new Intent(getActivity(),login.class);
                startActivity(launchLogin);
                getActivity().overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;

            case R.id.aboutApp:
                return true;

            case R.id.feedback:
                return true;
        }
        return super.onOptionsItemSelected(item);
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
                        ProfileName.setText(jsonObject.getString("name"));
                        if(!jsonObject.getString("birthdate").isEmpty()){
                            ProfileBio.append("Birthdate :"+jsonObject.getString("birthdate")+"\n");
                        }
                        if(jsonObject.getString("bio") != null){
                            ProfileBio.append(jsonObject.getString("bio")+"\n");
                        }
                        if(jsonObject.getString("website") != null){
                            ProfileBio.append(jsonObject.getString("website"));
                        }
                        if(jsonObject.getString("profileimage").isEmpty()){
                            ProfImage.setImageResource(R.drawable.logo);
                        }else{
                            ProfImage.setClickable(true);
                            imageurl = imageurl.concat("/"+jsonObject.getString("profileimage"));
                            Picasso.get().load(imageurl).into(ProfImage);
                            SharedPreferences.Editor editor = getActivity().getSharedPreferences("login_content", MODE_PRIVATE).edit();
                            editor.putString("userProfImage", imageurl);
                            editor.commit();
                        }
                        frndCnt.setText(jsonObject.getString("frnds")+"  friends");
                        postCnt.setText(jsonObject.getString("posts")+"  posts");
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

    public boolean loadFragment (Fragment fragment){
        if(fragment != null){
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container2,fragment).commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragment = null;
        menu = navigation2.getMenu();

        switch (menuItem.getItemId()){
            case R.id.posts:
                fragment = new MyPosts();
                menuItem.setChecked(true);
                break;

            case R.id.savedpost:
                fragment = new MySavedPosts();
                menuItem.setChecked(true);
                break;

            case R.id.friends:
                fragment = new MyFriendList();
                menuItem.setChecked(false);
                break;

            case R.id.requests:
                fragment = new MyReqList();
                menuItem.setChecked(false);
                break;
        }
        return loadFragment(fragment);
    }
}
