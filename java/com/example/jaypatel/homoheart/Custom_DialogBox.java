package com.example.jaypatel.homoheart;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class Custom_DialogBox {

    urlValue urlVal;
    Chk_Network checkNetwork;
    ProgressDialog mProgressDialogue;
    Custom_Edit_Dialog custom_edit_dialog;
    RequestParams requestParams = new RequestParams();
    AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    public void showDialog(final Context context, int buttons, String title, final String email) {
        int btns = buttons;
        String chkTitle = title;

        checkNetwork = new Chk_Network();
        custom_edit_dialog = new Custom_Edit_Dialog();
        mProgressDialogue = new ProgressDialog(context);
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);

        if(btns == 2 && chkTitle.matches("Need email verification")){
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_dialog2);

            TextView setTitle = dialog.findViewById(R.id.dilogBoxTitle);
            Button btnSendOtp = dialog.findViewById(R.id.sendOTP);
            Button btnCancel = dialog.findViewById(R.id.cancelbtn);

            setTitle.setText(title);
            btnSendOtp.setText("Send OTP");
            btnCancel.setText("Cancel");

            btnSendOtp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(checkNetwork.isNetworkAvailable(context)){
                        sendOtp(context ,email);
                        //Toast.makeText(context, "Send OTP called", Toast.LENGTH_SHORT).show();
                        custom_edit_dialog.showEditDialog(context,"Enter OTP",email);
                        dialog.dismiss();
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

    public  void sendOtp(final Context context,String email){
        urlVal = new urlValue();
        String url = urlVal.getSendOtpUrl();
        mProgressDialogue.setTitle("Loading...");
        mProgressDialogue.setMessage("Please wait...");
        mProgressDialogue.setIndeterminate(true);
        mProgressDialogue.setCanceledOnTouchOutside(false);
        mProgressDialogue.show();

        requestParams.put("email",email);

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
                        //Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
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
                Toast.makeText(context, "Send OTP Unsuccessful", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
