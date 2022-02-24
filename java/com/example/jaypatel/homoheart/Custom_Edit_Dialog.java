package com.example.jaypatel.homoheart;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class Custom_Edit_Dialog {

    urlValue urlVal;
    Chk_Network checkNetwork;
    ProgressDialog mProgressDialogue;
    Custom_DialogBox custom_dialogBox;
    RequestParams requestParams = new RequestParams();
    AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    public void showEditDialog(final Context context, String title, final String email) {
        String chkTitle = title;

        checkNetwork = new Chk_Network();
        custom_dialogBox = new Custom_DialogBox();
        mProgressDialogue = new ProgressDialog(context);
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);

        if(chkTitle.matches("Enter OTP")){
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_edit_dialog);

            TextView setTitle = dialog.findViewById(R.id.dilogBoxTitle);
            Button btnResendOtp = dialog.findViewById(R.id.sendOTP);
            ImageButton btnCancel = dialog.findViewById(R.id.cancelbtn);
            Button btnOk = dialog.findViewById(R.id.okbtn);
            final EditText userOtp = dialog.findViewById(R.id.userOTP);
            TextView hintText = dialog.findViewById(R.id.dilogBoxText);

            setTitle.setText(title);
            btnResendOtp.setText("Resend OTP");
            btnOk.setText("0K");
            hintText.setText("check "+email+" for OTP");

            btnResendOtp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(checkNetwork.isNetworkAvailable(context)){
                        custom_dialogBox.sendOtp(context ,email);
                    }
                    else {
                        Toast.makeText(context, "Turn on network!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(checkNetwork.isNetworkAvailable(context)){
                        checkOtp(context, email, userOtp.getEditableText().toString());
                        //Toast.makeText(context, "Send OTP called", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(context, "Turn on network!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }

    public void checkOtp(final Context context, String email, String userOtp){
        urlVal = new urlValue();
        String url = urlVal.getCheckOtpUrl();
        mProgressDialogue.setTitle("Loading...");
        mProgressDialogue.setMessage("Please wait...");
        mProgressDialogue.setIndeterminate(true);
        mProgressDialogue.setCanceledOnTouchOutside(false);
        mProgressDialogue.show();

        requestParams.put("email",email);
        requestParams.put("user_otp",userOtp);

        if(mProgressDialogue.isShowing()){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    mProgressDialogue.dismiss();
                }

            }, 6000);
        }
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
                        //intent to login
                        // Do something on success
                        Intent launchLogin = new Intent(context, login.class);
                        context.startActivity(launchLogin);
                        ((Activity)context).overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                    }
                    else{
                        if(mProgressDialogue.isShowing()){
                            mProgressDialogue.dismiss();
                        }
                        Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(context, "Cheking OTP Unsuccessful", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
