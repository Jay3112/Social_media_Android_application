package com.example.jaypatel.homoheart;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
public class MyFriendList extends Fragment {

    String url, profimageurl;

    String[] FriendProfileImage;
    String[] FriendMail;
    String[] FriendName;

    ListView myFriendsList;
    TextView frndListText;

    FriendListAdapter adapter;

    Chk_Network check_Network;
    ProgressDialog mProgressDialogue;
    AsyncHttpClient asyncHttpClient;
    RequestParams requestParams;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.my_friend_list, container, false);

        myFriendsList = (ListView)view.findViewById(R.id.friendListview);
        frndListText = (TextView)view.findViewById(R.id.frndListText);

        check_Network = new Chk_Network();
        asyncHttpClient = new AsyncHttpClient();
        requestParams = new RequestParams();
        mProgressDialogue = new ProgressDialog(getActivity());

        return view;
    }

    @Override
    public void onResume() {
        if(check_Network.isNetworkAvailable(getActivity())){
            getMyFriendList();
            super.onResume();
        }else{
            Toast.makeText(getActivity(), "Turn on internet", Toast.LENGTH_SHORT).show();
            super.onResume();
        }
    }

    public class FriendListAdapter extends BaseAdapter {

        LayoutInflater inflater;
        Context context;
        String[] frndMail;
        String[] frndName;
        String[] frndProfImage;

        public FriendListAdapter(Context context, String[] frndMail,String[] frndName,String[] frndProfImage) {
            // TODO Auto-generated constructor stub
            this.context = context;
            this.frndMail = frndMail;
            this.frndName = frndName;
            this.frndProfImage = frndProfImage;
        }

        private class ViewHolder {
            TextView userName;
            ImageView profImage;
            Button listBtn;
        }

        @Override
        public int getCount() {
            return frndMail.length;
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

            final FriendListAdapter.ViewHolder viewHolder = new FriendListAdapter.ViewHolder();

            viewHolder.userName = (TextView) rowView.findViewById(R.id.userName);
            viewHolder.profImage = (ImageView) rowView.findViewById(R.id.userProf);
            viewHolder.listBtn = (Button) rowView.findViewById(R.id.friendlistBtn);

            viewHolder.listBtn.setText("Unfriend");
            viewHolder.listBtn.setBackgroundResource(R.drawable.custom_dialog_button);

            viewHolder.userName.setText(frndName[position]);
            if(frndProfImage[position].isEmpty()){
                Picasso.get().load(R.drawable.logo).into(viewHolder.profImage);
            }else{
                Picasso.get().load(frndProfImage[position]).into(viewHolder.profImage);
            }

            viewHolder.listBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateNotification(frndMail[position],"unfriend","friend");
                }
            });

            viewHolder.userName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), OtherUserProfile.class);
                    intent.putExtra("userMail",frndMail[position]);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                }
            });

            viewHolder.profImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!frndProfImage[position].isEmpty()){
                        ShowCurrentProf showCurrentProf = new ShowCurrentProf();
                        showCurrentProf.showDialog(getActivity(), frndProfImage[position]);
                    }else{
                        Toast.makeText(getActivity(), "Profile Image is not available now.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            return rowView;
        }
    }

    public void getMyFriendList(){
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
        requestParams.put("flag", "friends");
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
                        FriendMail = new String[arrayLength - 1];
                        FriendProfileImage = new String[arrayLength - 1];
                        FriendName = new String[arrayLength - 1];

                        for(int i = 1; i<arrayLength; i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int temp = i - 1;
                            FriendMail[temp] = jsonObject.getString("user_mail");
                            FriendName[temp] = jsonObject.getString("user_name");
                            if(jsonObject.getString("userProfImage").isEmpty()){
                                FriendProfileImage[temp] = "";
                            }else{
                                FriendProfileImage[temp] =  profimageurl.concat("/"+jsonObject.getString("userProfImage"));
                            }
                        }
                        if(arrayLength == 1){
                            frndListText.setVisibility(View.GONE);
                        }

                        adapter = new FriendListAdapter(getActivity(),FriendMail,FriendName,FriendProfileImage);
                        myFriendsList.setAdapter(adapter);
                        ListScrollHelper.getListViewSize(myFriendsList);
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
                            Fragment fragment = new MyFriendList();
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
