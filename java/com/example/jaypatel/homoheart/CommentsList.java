package com.example.jaypatel.homoheart;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import cz.msebera.android.httpclient.Header;

public class CommentsList extends AppCompatActivity {

    String url, profimageurl, event_id, flag, user_mail, logMail;

    String[] FriendProfileImage;
    String[] FriendMail;
    String[] FriendName;
    String[] FriendComment;
    String[] eventId;

    ListView CommentsList;
    TextView cmtListText;
    ImageView comProfImage;

    CommentListAdapter adapter;

    EditText addCmtText;
    Button postCmt;

    Chk_Network check_Network;
    ProgressDialog mProgressDialogue;
    AsyncHttpClient asyncHttpClient;
    RequestParams requestParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_list);
        getSupportActionBar().hide();

        final Intent intent = getIntent();
        event_id = intent.getStringExtra("event_id");
        user_mail = intent.getStringExtra("user_mail");
        flag = intent.getStringExtra("flag");

        CommentsList = (ListView)findViewById(R.id.commentListview);
        cmtListText = (TextView)findViewById(R.id.commentHint);
        addCmtText = (EditText)findViewById(R.id.commentTextView);
        postCmt= (Button)findViewById(R.id.postBtn);
        comProfImage = (ImageView)findViewById(R.id.profImageComment);

        check_Network = new Chk_Network();
        asyncHttpClient = new AsyncHttpClient();
        requestParams = new RequestParams();
        mProgressDialogue = new ProgressDialog(CommentsList.this);

        postCmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(check_Network.isNetworkAvailable(CommentsList.this)){
                    if(addCmtText.getEditableText().toString().isEmpty()){
                        addCmtText.setError("please write something ...");
                    }else if(addCmtText.getEditableText().toString().length() > 50){
                        addCmtText.setError("More than 50 charecters not allowed!");
                        Toast.makeText(CommentsList.this, "Count is : "+addCmtText.length(), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        updateNotification(user_mail, event_id, "comment","comment");
                    }
                }else{
                    Toast.makeText(CommentsList.this,"Turn on internet",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        if(check_Network.isNetworkAvailable(CommentsList.this)){
            SharedPreferences prefs = getSharedPreferences("login_content", MODE_PRIVATE);
            logMail = prefs.getString("logMail", null);
            String userProfImage = prefs.getString("userProfImage", null);
            Picasso.get().load(userProfImage).into(comProfImage);
            getCommentsList();
            super.onResume();
        }else{
            Toast.makeText(CommentsList.this, "Turn on internet", Toast.LENGTH_SHORT).show();
            super.onResume();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    public class CommentListAdapter extends BaseAdapter {

        LayoutInflater inflater;
        Context context;
        String[] frndMail;
        String[] frndName;
        String[] frndProfImage;
        String[] frndComment;
        String[] eventId;

        public CommentListAdapter(Context context, String[] frndMail,String[] frndName,String[] frndProfImage,String[] frndComment,String[] eventId) {
            // TODO Auto-generated constructor stub
            this.context = context;
            this.frndMail = frndMail;
            this.frndName = frndName;
            this.frndComment = frndComment;
            this.eventId = eventId;
            this.frndProfImage = frndProfImage;
        }

        private class ViewHolder {
            TextView userName, comment;
            ImageView profImage;
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
            View rowView = inflater.inflate(R.layout.custlist_comment, null, true);

            final CommentListAdapter.ViewHolder viewHolder = new CommentListAdapter.ViewHolder();

            viewHolder.userName = (TextView) rowView.findViewById(R.id.userName);
            viewHolder.profImage = (ImageView) rowView.findViewById(R.id.userProfImage);
            viewHolder.comment = (TextView) rowView.findViewById(R.id.cmtText);

            viewHolder.userName.setText(frndName[position]);
            if(frndProfImage[position].isEmpty()){
                Picasso.get().load(R.drawable.logo).into(viewHolder.profImage);
            }else{
                Picasso.get().load(frndProfImage[position]).into(viewHolder.profImage);
            }
            viewHolder.comment.setText(FriendComment[position]);

            viewHolder.userName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(frndMail[position].matches(logMail)){
                        //proftab intent
                    }else {
                        Intent intent = new Intent(CommentsList.this, OtherUserProfile.class);
                        intent.putExtra("userMail", frndMail[position]);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                }
            });

            viewHolder.profImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowCurrentProf showCurrentProf = new ShowCurrentProf();
                    showCurrentProf.showDialog(CommentsList.this, frndProfImage[position]);
                }
            });

            return rowView;
        }
    }

    public void getCommentsList(){
        urlValue values = new urlValue();
        url = values.getLikeDislikelist();
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

        requestParams.put("event_id", event_id);
        requestParams.put("flag", flag);
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
                        FriendComment = new String[arrayLength - 1];

                        for(int i = 1; i<arrayLength; i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int temp = i - 1;
                            FriendMail[temp] = jsonObject.getString("user_mail");
                            FriendName[temp] = jsonObject.getString("user_name");
                            FriendComment[temp] = jsonObject.getString("comment");
                            if(jsonObject.getString("userProfImage").isEmpty()){
                                FriendProfileImage[temp] = "";
                            }else{
                                FriendProfileImage[temp] =  profimageurl.concat("/"+jsonObject.getString("userProfImage"));
                            }
                        }
                        if(arrayLength == 1){
                            cmtListText.setVisibility(View.GONE);
                        }else {
                            cmtListText.setText("Comments("+(arrayLength-1)+")");
                        }

                        adapter = new CommentListAdapter(CommentsList.this,FriendMail,FriendName,FriendProfileImage,FriendComment,eventId);
                        CommentsList.setAdapter(adapter);
                        ListScrollHelper.getListViewSize(CommentsList);
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

    public void updateNotification(String eventUserMail,String event_id,String notificSubtype, String notificType){
        urlValue values = new urlValue();
        url = values.updateNotification();

        requestParams.put("email", logMail);
        requestParams.put("event_id",event_id);
        requestParams.put("receiver_mail",eventUserMail);
        requestParams.put("type",notificType);
        requestParams.put("subtype",notificSubtype);
        requestParams.put("comment",addCmtText.getEditableText().toString());

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
                            Toast.makeText(CommentsList.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CommentsList.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
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
