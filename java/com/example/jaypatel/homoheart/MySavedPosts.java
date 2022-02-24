package com.example.jaypatel.homoheart;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import org.w3c.dom.Text;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class MySavedPosts extends Fragment {

    String url, profimageurl, eveimagebaseurl, logMail;

    ArrayList<String[]> eventImageUrls = new ArrayList<String[]>();
    String[] profileImage;
    String[] eventUserMail;
    String[] eventId;
    String[] title;
    String[] username;
    String[] desc;
    String[] event_date;
    String[] likedislike;
    int[] saveflag;
    int[] likecnt;
    int[] dislikecnt;
    int[] commentcnt;
    ListView mySavedEventList;
    TabLayout tabLayout;
    TextView hintText;
    EventListAdapter adapter;
    ViewImageAdapterServer adapter2;
    ArrayList<EventDataGetterSetter> arrayList;

    Chk_Network check_Network;
    ProgressDialog mProgressDialogue;
    AsyncHttpClient asyncHttpClient;
    RequestParams requestParams;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.my_saved_posts, container, false);

        mySavedEventList = (ListView)view.findViewById(R.id.savedPostListview);
        hintText = (TextView) view.findViewById(R.id.saveListText);

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
        if(check_Network.isNetworkAvailable(getActivity())){
            getMySavedPosts();
            super.onResume();
        }else{
            Toast.makeText(getActivity(), "Turn on internet", Toast.LENGTH_SHORT).show();
            super.onResume();
        }
    }

    public class EventListAdapter extends BaseAdapter {

        LayoutInflater inflater;
        Context context;
        ArrayList<EventDataGetterSetter> arrayList;

        public EventListAdapter(Context context, ArrayList<EventDataGetterSetter> arrayList) {
            // TODO Auto-generated constructor stub
            this.context = context;
            this.arrayList = arrayList;
        }

        private class ViewHolder{
            TextView eventTitle, eventDesc, eventUsername;
            ImageView eventProfImage;
            ViewPager viewPager;
            TextView likesTag, dislikesTag, commentsTag;
            ImageView eveLike, eveDislike, eveComment, eveSend, eveShare, eveSave;
            LinearLayout imageSection, titleSection, descSection;
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
            inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.custlist_home, null,true);

            final EventListAdapter.ViewHolder viewHolder = new EventListAdapter.ViewHolder();

            viewHolder.imageSection = (LinearLayout) rowView.findViewById(R.id.imageSection);
            viewHolder.titleSection = (LinearLayout) rowView.findViewById(R.id.titlelayout);
            viewHolder.descSection = (LinearLayout) rowView.findViewById(R.id.desclayout);
            viewHolder.eventTitle = (TextView) rowView.findViewById(R.id.eventTitle);
            viewHolder.viewPager = (ViewPager)rowView.findViewById(R.id.view_pager);
            viewHolder.eventUsername = (TextView) rowView.findViewById(R.id.eveUsername);
            viewHolder.eventDesc = (TextView) rowView.findViewById(R.id.eventDescription);
            viewHolder.eventProfImage = (ImageView) rowView.findViewById(R.id.eveUsrProfileImage);
            viewHolder.eveLike = (ImageView)rowView.findViewById(R.id.like);
            viewHolder.eveDislike = (ImageView)rowView.findViewById(R.id.dislike);
            viewHolder.eveComment = (ImageView)rowView.findViewById(R.id.comment);
            viewHolder.eveSend = (ImageView)rowView.findViewById(R.id.send);
            viewHolder.eveShare = (ImageView)rowView.findViewById(R.id.share);
            viewHolder.eveSave = (ImageView)rowView.findViewById(R.id.save);
            viewHolder.likesTag = (TextView)rowView.findViewById(R.id.likesTag);
            viewHolder.dislikesTag = (TextView)rowView.findViewById(R.id.dislikesTag);
            viewHolder.commentsTag = (TextView)rowView.findViewById(R.id.commentsTag);
            tabLayout = (TabLayout)rowView.findViewById(R.id.tab_layout);

            final EventDataGetterSetter p = arrayList.get(position);
            final boolean[] likeflag = new boolean[1];
            final boolean[] dislikeflag = new boolean[1];
            final boolean[] saveflag = new boolean[1];
            final int[] tempLikecnt = {p.getLikecnt()};
            final int[] tempDislikecnt = {p.getDislikecnt()};

            viewHolder.eventTitle.setText(p.getTitle());
            viewHolder.eventDesc.setText(p.getDesc());
            viewHolder.eventUsername.setText(p.getUsername());
            viewHolder.likesTag.setText(tempLikecnt[0]+"  Likes    ");
            viewHolder.dislikesTag.setText(tempDislikecnt[0] +"  Dislikes    ");
            viewHolder.commentsTag.setText(p.getCommentcnt()+"  Comments    ");
            Picasso.get().load(p.getProfileImage()).into(viewHolder.eventProfImage);
            adapter2 = new ViewImageAdapterServer(getActivity(), eventImageUrls.get(position));
            viewHolder.viewPager.setAdapter(adapter2);

            if(p.getLikedislike().matches("like")){
                likeflag[0] = true;
                viewHolder.eveLike.setImageResource(R.drawable.ic_thumb_up_blue_24dp);
            }else if(p.getLikedislike().matches("dislike")){
                dislikeflag[0] = true;
                viewHolder.eveDislike.setImageResource(R.drawable.ic_thumb_down_red_24dp);
            }
            if(p.saveflag > 0 ){
                saveflag[0] = true;
                viewHolder.eveSave.setImageResource(R.drawable.ic_bookmark_black_24dp);
            }

            final String[][] temp = {eventImageUrls.get(position)};
            if(eventImageUrls.get(position).length > 1){
                tabLayout.setupWithViewPager(viewHolder.viewPager, true);
            }
            if(temp[0][0].matches("null")){
                viewHolder.imageSection.removeAllViews();
                viewHolder.imageSection.setVisibility(View.GONE);
            }
            if(p.getTitle().isEmpty()){
                viewHolder.titleSection.removeAllViews();
            }
            if(p.getDesc().isEmpty()){
                viewHolder.descSection.removeAllViews();
            }

            viewHolder.eventProfImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!p.getProfileImage().isEmpty()){
                        ShowCurrentProf showCurrentProf = new ShowCurrentProf();
                        showCurrentProf.showDialog(getActivity(), p.getProfileImage());
                    }else{
                        Toast.makeText(getActivity(), "Profile Image is not available now.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            viewHolder.eventUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                if(p.getEventUserMail().matches(logMail)){
                    ((Main2Activity)getActivity()).ProfIntent();
                }else{
                    Intent intent = new Intent(getActivity(), OtherUserProfile.class);
                    intent.putExtra("userMail",p.getEventUserMail());
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                }
                }
            });

            viewHolder.titleSection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), DispSingleEve.class);
                    intent.putExtra("event_id",p.getEventId());
                    intent.putExtra("user_mail",p.getEventUserMail());
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                }
            });

            viewHolder.descSection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), DispSingleEve.class);
                    intent.putExtra("event_id",p.getEventId());
                    intent.putExtra("user_mail",p.getEventUserMail());
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                }
            });
            viewHolder.eveLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                if(likeflag[0]){
                    updateNotification(p.getEventUserMail(), "none", p.getEventId(), "likedislike");
                    viewHolder.eveLike.setImageResource(R.drawable.ic_thumb_up_black_24dp);
                    Animation animation = AnimationUtils.loadAnimation(getActivity(),R.anim.bounce);
                    viewHolder.eveLike.startAnimation(animation);
                    tempLikecnt[0] = tempLikecnt[0] -1;
                    viewHolder.likesTag.setText(tempLikecnt[0]+"  Likes    ");
                    likeflag[0] = false;
                }else{
                    updateNotification(p.getEventUserMail(), "like", p.getEventId(), "likedislike");
                    viewHolder.eveLike.setImageResource(R.drawable.ic_thumb_up_blue_24dp);
                    Animation animation = AnimationUtils.loadAnimation(getActivity(),R.anim.bounce);
                    viewHolder.eveLike.startAnimation(animation);
                    tempLikecnt[0] = tempLikecnt[0] +1;
                    viewHolder.likesTag.setText(tempLikecnt[0]+"  Likes    ");
                    likeflag[0] = true;
                    if(dislikeflag[0]){
                        viewHolder.eveDislike.setImageResource(R.drawable.ic_thumb_down_black_24dp);
                        Animation animation2 = AnimationUtils.loadAnimation(getActivity(),R.anim.blink_anim);
                        viewHolder.eveDislike.startAnimation(animation2);
                        tempDislikecnt[0] = tempDislikecnt[0] -1;
                        viewHolder.dislikesTag.setText(tempDislikecnt[0]+"  Dislikes    ");
                        dislikeflag[0] = false;
                    }
                }
                }
            });

            viewHolder.eveDislike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                if(dislikeflag[0]){
                    updateNotification(p.getEventUserMail(), "none", p.getEventId(), "likedislike");
                    viewHolder.eveDislike.setImageResource(R.drawable.ic_thumb_down_black_24dp);
                    Animation animation = AnimationUtils.loadAnimation(getActivity(),R.anim.bounce);
                    viewHolder.eveDislike.startAnimation(animation);
                    tempDislikecnt[0] = tempDislikecnt[0] -1;
                    viewHolder.dislikesTag.setText(tempDislikecnt[0]+"  Dislikes    ");
                    dislikeflag[0] = false;
                }else{
                    updateNotification(p.getEventUserMail(), "dislike", p.getEventId(), "likedislike");
                    viewHolder.eveDislike.setImageResource(R.drawable.ic_thumb_down_red_24dp);
                    Animation animation = AnimationUtils.loadAnimation(getActivity(),R.anim.bounce);
                    viewHolder.eveDislike.startAnimation(animation);
                    tempDislikecnt[0] = tempDislikecnt[0] +1;
                    viewHolder.dislikesTag.setText(tempDislikecnt[0]+"  Dislikes    ");
                    dislikeflag[0] = true;
                    if(likeflag[0]){
                        viewHolder.eveLike.setImageResource(R.drawable.ic_thumb_up_black_24dp);
                        Animation animation2 = AnimationUtils.loadAnimation(getActivity(),R.anim.blink_anim);
                        viewHolder.eveLike.startAnimation(animation2);
                        tempLikecnt[0] = tempLikecnt[0] -1;
                        viewHolder.likesTag.setText(tempLikecnt[0]+"  Likes    ");
                        likeflag[0] = false;
                    }
                }
                }
            });

            viewHolder.eveComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CommentsList.class);
                intent.putExtra("event_id",p.getEventId());
                intent.putExtra("user_mail",p.getEventUserMail());
                intent.putExtra("flag","comments");
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                }
            });

            viewHolder.eveSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity(), "Send called :"+position, Toast.LENGTH_SHORT).show();
                }
            });

            viewHolder.eveShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity(), "Share called :"+position, Toast.LENGTH_SHORT).show();
                }
            });

            viewHolder.eveSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                if(saveflag[0]) {
                    updateNotification(p.getEventUserMail(), "unsave", p.getEventId(), "save");
                    viewHolder.eveSave.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                    Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
                    viewHolder.eveSave.startAnimation(animation);
                    saveflag[0] = false;
                    Toast.makeText(getActivity(), "Event removed", Toast.LENGTH_SHORT).show();
                }
                }
            });

            viewHolder.likesTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                if(p.getLikecnt() > 0){
                    Intent intent = new Intent(getActivity(), LikeDislikeListDisp.class);
                    intent.putExtra("event_id",p.getEventId());
                    intent.putExtra("flag","likes");
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                }else{
                    Toast.makeText(getActivity(), "0 likes", Toast.LENGTH_SHORT).show();
                }
                }
            });

            viewHolder.dislikesTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                if(p.getDislikecnt() > 0){
                    Intent intent = new Intent(getActivity(), LikeDislikeListDisp.class);
                    intent.putExtra("event_id",p.getEventId());
                    intent.putExtra("flag","dislikes");
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                }else{
                    Toast.makeText(getActivity(), "0 dislikes", Toast.LENGTH_SHORT).show();
                }
                }
            });

            viewHolder.commentsTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CommentsList.class);
                intent.putExtra("event_id",p.getEventId());
                intent.putExtra("user_mail",p.getEventUserMail());
                intent.putExtra("flag","comments");
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                }
            });

            return rowView;
        }
    }

    public void getMySavedPosts(){
        urlValue values = new urlValue();
        url = values.getHomeDataUrl();
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
        requestParams.put("flag", "saved");
        requestParams.put("user_email","");
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

                        eventUserMail = new String[jsonArray.length()];
                        eventId = new String[jsonArray.length()];
                        profileImage = new String[jsonArray.length()];
                        title = new String[jsonArray.length()];
                        username = new String[jsonArray.length()];
                        desc = new String[jsonArray.length()];
                        event_date = new String[jsonArray.length()];
                        likedislike = new String[jsonArray.length()];
                        likecnt = new int[jsonArray.length()];
                        dislikecnt = new int[jsonArray.length()];
                        commentcnt = new int[jsonArray.length()];
                        saveflag = new int[jsonArray.length()];

                        int arrayLength = jsonArray.length();
                        for(int i = 1; i<arrayLength; i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String[] eventimgUrlAsPerEve = new String[jsonObject.getInt("rows")];
                            String name = null;
                            for(int j=0; j<jsonObject.getInt("rows"); j++){
                                name = "eveImg".concat(Integer.toString(j));
                                String imageurl = jsonObject.getString(name);
                                if(!jsonObject.getString(name).matches("null")){
                                    imageurl = eveimagebaseurl.concat("/"+imageurl);
                                }
                                eventimgUrlAsPerEve[j] = imageurl;
                            }
                            eventImageUrls.add(eventimgUrlAsPerEve);
                            eventUserMail[i] = jsonObject.getString("email");
                            eventId[i] = jsonObject.getString("eventId");
                            profileImage[i] =  profimageurl.concat("/"+jsonObject.getString("profImage"));
                            username[i] = jsonObject.getString("username");
                            title[i] = jsonObject.getString("title");
                            desc[i] = jsonObject.getString("description");
                            event_date[i] = jsonObject.getString("event_date");
                            likedislike[i] = jsonObject.getString("likedislike");
                            likecnt[i] = jsonObject.getInt("likecnt");
                            dislikecnt[i] = jsonObject.getInt("dislikecnt");
                            commentcnt[i] = jsonObject.getInt("commentcnt");
                            saveflag[i] = jsonObject.getInt("saveflag");
                        }

                        //set prof image as shared pref and get here
                        arrayList = new ArrayList<>();

                        for (int i = 1; i <jsonArray.length(); i++) {
                            EventDataGetterSetter p = new EventDataGetterSetter();
                            p.setProfileImage(profileImage[i]);
                            p.setEventUserMail(eventUserMail[i]);
                            p.setEventId(eventId[i]);
                            p.setUsername(username[i]);
                            p.setTitle(title[i]);
                            p.setDesc(desc[i]);
                            p.setLikedislike(likedislike[i]);
                            p.setLikecnt(likecnt[i]);
                            p.setDislikecnt(dislikecnt[i]);
                            p.setCommentcnt(commentcnt[i]);
                            p.setSaveflag(saveflag[i]);
                            arrayList.add(p);
                        }


                        if(arrayLength == 1){
                            hintText.setVisibility(View.GONE);
                        }

                        adapter = new EventListAdapter(getActivity(), arrayList);
                        mySavedEventList.setAdapter(adapter);
                        ListScrollHelper.getListViewSize(mySavedEventList);
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

    public void updateNotification(String eventUserMail,String notificSubtype,String event_id, String notificType){
        SharedPreferences prefs = getActivity().getSharedPreferences("login_content", MODE_PRIVATE);
        String logMail = prefs.getString("logMail", null);
        urlValue values = new urlValue();
        url = values.updateNotification();

        requestParams.put("email", logMail);
        requestParams.put("event_id",event_id);
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
                            //Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                            Fragment fragment = new MySavedPosts();
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
