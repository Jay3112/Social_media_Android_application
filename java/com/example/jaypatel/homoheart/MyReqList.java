package com.example.jaypatel.homoheart;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyReqList extends Fragment {

    String url, profimageurl;

    String[] sugProfileImage;
    String[] sugFriendMail;
    String[] sugFriendName;

    String[] getReqProfileImage;
    String[] getReqMail;
    String[] getReqName;

    String[] sentReqProfileImage;
    String[] sentReqMail;
    String[] sentReqName;

    LinearLayout getList, sentList, suggestionList;
    ListView myFriendsSugList,friendsReqGetList,friendsReqSentList;

    FriendReqListAdapter adapter;

    Chk_Network check_Network;
    ProgressDialog mProgressDialogue;
    AsyncHttpClient asyncHttpClient;
    RequestParams requestParams;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.my_req_list, container, false);

        getList = (LinearLayout) view.findViewById(R.id.pandingList);
        sentList = (LinearLayout) view.findViewById(R.id.sentList);
        suggestionList = (LinearLayout) view.findViewById(R.id.suggestionList);
        myFriendsSugList = (ListView)view.findViewById(R.id.friendReqListview);
        friendsReqGetList = (ListView)view.findViewById(R.id.friendReqGetList);
        friendsReqSentList = (ListView)view.findViewById(R.id.friendReqSentList);

        check_Network = new Chk_Network();
        asyncHttpClient = new AsyncHttpClient();
        requestParams = new RequestParams();
        mProgressDialogue = new ProgressDialog(getActivity());

        return view;
    }

    @Override
    public void onResume() {
        if(check_Network.isNetworkAvailable(getActivity())){
            getMyPandingRequests();
            getMySentRequests();
            getMyFriendsSuggestion();

            super.onResume();
        }else{
            Toast.makeText(getActivity(), "Turn on internet", Toast.LENGTH_SHORT).show();
            super.onResume();
        }
    }

    public class FriendReqListAdapter extends BaseAdapter {

        LayoutInflater inflater;
        Context context;
        String [] frnd_name;
        String [] frnd_mail;
        String [] frnd_profimage;
        String type;

        public FriendReqListAdapter(Context context,String type, String[] frnd_mail,String[] frnd_name,String[] frnd_profimage) {
            // TODO Auto-generated constructor stub
            this.context = context;
            this.type = type;
            this.frnd_mail = frnd_mail;
            this.frnd_name = frnd_name;
            this.frnd_profimage = frnd_profimage;
        }

        private class ViewHolder {
            TextView userName;
            ImageView userProfImage;
            Button friendListBtn;
        }

        @Override
        public int getCount() {
            return frnd_name.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public View getView(final int position, View view, ViewGroup parent) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.custlist_friends, null, true);

            final FriendReqListAdapter.ViewHolder viewHolder = new FriendReqListAdapter.ViewHolder();

            viewHolder.userProfImage = (ImageView)rowView.findViewById(R.id.userProf);
            viewHolder.userName = (TextView)rowView.findViewById(R.id.userName);
            viewHolder.friendListBtn = (Button)rowView.findViewById(R.id.friendlistBtn);

            if(type.matches("suggestion")){
                viewHolder.friendListBtn.setText("Add friend");
                viewHolder.friendListBtn.setBackgroundResource(R.drawable.friendlist_button);
                viewHolder.friendListBtn.setTextColor(Color.parseColor("#ffffff"));
            }else if(type.matches("pandingReq")){
                viewHolder.friendListBtn.setText("Accept");
                viewHolder.friendListBtn.setBackgroundResource(R.drawable.custom_dialog_button);
            }else if(type.matches("sentReq")){
                viewHolder.friendListBtn.setText("Cancel");
                viewHolder.friendListBtn.setBackgroundResource(R.drawable.custom_dialog_button);
            }

            viewHolder.userName.setText(frnd_name[position]);
            if(frnd_profimage[position].isEmpty()){
                Picasso.get().load(R.drawable.logo).into(viewHolder.userProfImage);
            }else{
                Picasso.get().load(frnd_profimage[position]).into(viewHolder.userProfImage);
            }

            viewHolder.friendListBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(type.matches("suggestion")){
                        updateNotification(frnd_mail[position],"request","friend");
                    }else if(type.matches("pandingReq")){
                        updateNotification(frnd_mail[position],"friend","friend");
                    }else if(type.matches("sentReq")){
                        updateNotification(frnd_mail[position],"none","friend");
                    }
                }
            });

            viewHolder.userName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), OtherUserProfile.class);
                    intent.putExtra("userMail",frnd_mail[position]);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                }
            });

            viewHolder.userProfImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!frnd_profimage[position].isEmpty()){
                        ShowCurrentProf showCurrentProf = new ShowCurrentProf();
                        showCurrentProf.showDialog(getActivity(), frnd_profimage[position]);
                    }else{
                        Toast.makeText(getActivity(), "Profile Image is not available now.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            return rowView;
        }
    }

    public void getMyPandingRequests(){
        SharedPreferences prefs = getActivity().getSharedPreferences("login_content", MODE_PRIVATE);
        String logMail = prefs.getString("logMail", null);

        urlValue values = new urlValue();
        url = values.getFriends();
        profimageurl = values.getProfImage();

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
        requestParams.put("flag", "pandingReq");
        asyncHttpClient.post(url, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray array) {
                super.onSuccess(statusCode, headers, array);
                try {
                    JSONArray jsonArray = new JSONArray(String.valueOf(array));
                    if (jsonArray.getString(0).matches("false")) {
                        if(mProgressDialogue.isShowing()){
                            mProgressDialogue.dismiss();
                        }

                        int arrayLength = jsonArray.length();
                        getReqMail = new String[arrayLength - 1];
                        getReqProfileImage = new String[arrayLength - 1];
                        getReqName = new String[arrayLength - 1];

                        for(int i = 1; i<arrayLength; i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int temp = i - 1;
                            getReqMail[temp] = jsonObject.getString("user_mail");
                            getReqName[temp] = jsonObject.getString("user_name");
                            if(jsonObject.getString("userProfImage").isEmpty()){
                                getReqProfileImage[temp] = "";
                            }else{
                                getReqProfileImage[temp] =  profimageurl.concat("/"+jsonObject.getString("userProfImage"));
                            }
                        }

                        if(arrayLength == 1){
                            getList.setVisibility(View.GONE);
                        }

                        adapter = new FriendReqListAdapter(getActivity(),"pandingReq",getReqMail,getReqName,getReqProfileImage);
                        friendsReqGetList.setAdapter(adapter);
                        ListScrollHelper.getListViewSize(friendsReqGetList);
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

    public void getMySentRequests() {
        SharedPreferences prefs = getActivity().getSharedPreferences("login_content", MODE_PRIVATE);
        String logMail = prefs.getString("logMail", null);

        urlValue values = new urlValue();
        url = values.getFriends();
        profimageurl = values.getProfImage();

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
        requestParams.put("flag", "sentReq");
        asyncHttpClient.post(url, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray array) {
                super.onSuccess(statusCode, headers, array);
                try {
                    JSONArray jsonArray = new JSONArray(String.valueOf(array));
                    if (jsonArray.getString(0).matches("false")) {
                        if (mProgressDialogue.isShowing()) {
                            mProgressDialogue.dismiss();
                        }

                        int arrayLength = jsonArray.length();
                        sentReqMail = new String[arrayLength - 1];
                        sentReqProfileImage = new String[arrayLength - 1];
                        sentReqName = new String[arrayLength - 1];

                        for (int i = 1; i < arrayLength; i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int temp = i - 1;
                            sentReqMail[temp] = jsonObject.getString("user_mail");
                            sentReqName[temp] = jsonObject.getString("user_name");
                            if (jsonObject.getString("userProfImage").isEmpty()) {
                                sentReqProfileImage[temp] = "";
                            } else {
                                sentReqProfileImage[temp] = profimageurl.concat("/" + jsonObject.getString("userProfImage"));
                            }
                        }

                        if(arrayLength == 1){
                            sentList.setVisibility(View.GONE);
                        }

                        adapter = new FriendReqListAdapter(getActivity(), "sentReq", sentReqMail, sentReqName, sentReqProfileImage);
                        friendsReqSentList.setAdapter(adapter);
                        ListScrollHelper.getListViewSize(friendsReqSentList);
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

    public void getMyFriendsSuggestion(){
        SharedPreferences prefs = getActivity().getSharedPreferences("login_content", MODE_PRIVATE);
        String logMail = prefs.getString("logMail", null);

        urlValue values = new urlValue();
        url = values.getFriends();
        profimageurl = values.getProfImage();

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
        requestParams.put("flag", "suggestion");
        asyncHttpClient.post(url, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray array) {
                super.onSuccess(statusCode, headers, array);
                try {
                    JSONArray jsonArray = new JSONArray(String.valueOf(array));
                    if (jsonArray.getString(0).matches("false")) {
                        if(mProgressDialogue.isShowing()){
                            mProgressDialogue.dismiss();
                        }

                        int arrayLength = jsonArray.length();
                        sugFriendMail = new String[arrayLength - 1];
                        sugProfileImage = new String[arrayLength - 1];
                        sugFriendName = new String[arrayLength - 1];

                        for(int i = 1; i<arrayLength; i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int temp = i - 1;
                            sugFriendMail[temp] = jsonObject.getString("user_mail");
                            sugFriendName[temp] = jsonObject.getString("user_name");
                            if(jsonObject.getString("userProfImage").isEmpty()){
                                sugProfileImage[temp] = "";
                            }else{
                                sugProfileImage[temp] =  profimageurl.concat("/"+jsonObject.getString("userProfImage"));
                            }
                        }

                        if(arrayLength == 1){
                            suggestionList.setVisibility(View.GONE);
                        }

                        adapter = new FriendReqListAdapter(getActivity(),"suggestion",sugFriendMail,sugFriendName,sugProfileImage);
                        myFriendsSugList.setAdapter(adapter);
                        ListScrollHelper.getListViewSize(myFriendsSugList);
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
        SharedPreferences prefs = getActivity().getSharedPreferences("login_content", MODE_PRIVATE);
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
                            Fragment fragment = new MyReqList();
                            FragmentTransaction tr = getFragmentManager().beginTransaction();
                            tr.replace(R.id.fragment_container2, fragment);
                            tr.commit();
                        }else{
                            Toast.makeText(getActivity(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
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
