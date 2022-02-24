package com.example.jaypatel.homoheart;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
public class NotificationTab extends Fragment {

    String url, profimageurl, eveimagebaseurl, logMail;
    ListView notificationList;

    String[] senderProfImage;
    String[] eveImage;
    String[] eventType;
    String[] eventSubtype;
    String[] senderName;
    String[] senderEmail;
    String[] notificId;
    String[] eventId;
    NotificListAdapter adapter;
    ArrayList<NotificDataGatterSetter> arrayList;

    Chk_Network check_Network;
    ProgressDialog mProgressDialogue;
    AsyncHttpClient asyncHttpClient;
    RequestParams requestParams;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.notification_tab, container, false);

        notificationList = (ListView) view.findViewById(R.id.notifListView);

        SharedPreferences prefs = getActivity().getSharedPreferences("login_content", MODE_PRIVATE);
        logMail = prefs.getString("logMail", null);

        check_Network = new Chk_Network();
        asyncHttpClient = new AsyncHttpClient();
        requestParams = new RequestParams();
        mProgressDialogue = new ProgressDialog(getActivity());

        return view;
    }

    @Override
    public void onResume() {
        if (check_Network.isNetworkAvailable(getActivity())) {
            getNotifications();
            super.onResume();
        } else {
            Toast.makeText(getActivity(), "Turn on internet", Toast.LENGTH_SHORT).show();
            super.onResume();
        }
    }

    public class NotificListAdapter extends BaseAdapter {

        LayoutInflater inflater;
        Context context;
        ArrayList<NotificDataGatterSetter> arrayList;

        public NotificListAdapter(Context context, ArrayList<NotificDataGatterSetter> arrayList) {
            // TODO Auto-generated constructor stub
            this.context = context;
            this.arrayList = arrayList;
        }

        private class ViewHolder {
            TextView notificationText;
            ImageView eveImage, profImage;
        }

        @Override
        public int getCount() {
            return arrayList.size();
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
            View rowView = inflater.inflate(R.layout.custlist_notif, null, true);

            final ViewHolder viewHolder = new ViewHolder();

            viewHolder.profImage = (ImageView) rowView.findViewById(R.id.userProf);
            viewHolder.eveImage = (ImageView) rowView.findViewById(R.id.eventImage);
            viewHolder.notificationText = (TextView) rowView.findViewById(R.id.notifText);

            final NotificDataGatterSetter p = arrayList.get(position);
            Picasso.get().load(p.getSenderProfImage()).into(viewHolder.profImage);
            Picasso.get().load(p.getEventImage()).into(viewHolder.eveImage);
            if(p.getEventType().matches("likedislike")){
                viewHolder.notificationText.setText(p.getSenderName()+" "+p.getEventSubtype()+"s your post.");
            }else if(p.getEventType().matches("friend")){
                if(p.getEventSubtype().matches("request")){
                    viewHolder.notificationText.setText(p.getSenderName()+" sent you friend "+p.getEventSubtype()+".");
                }else if(p.getEventSubtype().matches("friend")){
                    viewHolder.notificationText.setText(p.getSenderName()+" accepted your friend request.");
                }
            }else if(p.getEventType().matches("comment")){
                viewHolder.notificationText.setText(p.getSenderName()+" commented on your post.");
            }

            viewHolder.notificationText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(p.getEventType().matches("friend")) {
                        Intent intent = new Intent(getActivity(), OtherUserProfile.class);
                        intent.putExtra("userMail",p.getSenderEmail());
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    }else{
                        Intent intent = new Intent(getActivity(), DispSingleEve.class);
                        intent.putExtra("event_id",p.getEventId());
                        intent.putExtra("user_mail",p.getSenderEmail());
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    }
                }
            });

            viewHolder.eveImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), DispSingleEve.class);
                    intent.putExtra("event_id",p.getEventId());
                    intent.putExtra("user_mail",p.getSenderEmail());
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                }
            });

            viewHolder.profImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(p.getSenderEmail().matches(logMail)){
                        ((Main2Activity)getActivity()).ProfIntent();
                    }else {
                        Intent intent = new Intent(getActivity(), OtherUserProfile.class);
                        intent.putExtra("userMail",p.getSenderEmail());
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    }
                }
            });

            return rowView;
        }
    }

    public void getNotifications(){
        urlValue values = new urlValue();
        url = values.getNotifications();
        profimageurl = values.getProfImage();
        eveimagebaseurl = values.geteventImages();

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
            public void onSuccess(int statusCode, Header[] headers, JSONArray array) {
                super.onSuccess(statusCode, headers, array);
                try {
                    JSONArray jsonArray = new JSONArray(String.valueOf(array));
                    if (jsonArray.getString(0).matches("false")) {
                        if(mProgressDialogue.isShowing()){
                            mProgressDialogue.dismiss();
                        }

                        senderProfImage = new String[jsonArray.length()];
                        eveImage = new String[jsonArray.length()];
                        eventType = new String[jsonArray.length()];
                        eventSubtype = new String[jsonArray.length()];
                        senderName = new String[jsonArray.length()];
                        senderEmail = new String[jsonArray.length()];
                        notificId = new String[jsonArray.length()];
                        eventId = new String[jsonArray.length()];

                        int arrayLength = jsonArray.length();
                        for(int i = 1; i<arrayLength; i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            senderEmail[i] = jsonObject.getString("sender_mail");
                            senderProfImage[i] =  profimageurl.concat("/"+jsonObject.getString("senderProfImage"));
                            eveImage[i] = eveimagebaseurl.concat("/"+jsonObject.getString("eventImage"));
                            eventType[i] = jsonObject.getString("type");
                            eventSubtype[i] = jsonObject.getString("subtype");
                            senderName[i] = jsonObject.getString("sender_name");
                            notificId[i] = jsonObject.getString("notific_id");
                            eventId[i] = jsonObject.getString("event_id");
                        }

                        //set prof image as shared pref and get here
                        arrayList = new ArrayList<>();

                        for (int i = 1; i <jsonArray.length(); i++) {
                            NotificDataGatterSetter p = new NotificDataGatterSetter();
                            p.setSenderEmail(senderEmail[i]);
                            p.setSenderProfImage(senderProfImage[i]);
                            p.setEventImage(eveImage[i]);
                            p.setEventType(eventType[i]);
                            p.setEventSubtype(eventSubtype[i]);
                            p.setSenderName(senderName[i]);
                            p.setNotificId(notificId[i]);
                            p.setEventId(eventId[i]);
                            arrayList.add(p);
                        }

                        adapter = new NotificListAdapter(getActivity(), arrayList);
                        notificationList.setAdapter(adapter);
                        ListScrollHelper.getListViewSize(notificationList);
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
