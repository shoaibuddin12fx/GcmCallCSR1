package com.matechco.omnisales;

import android.content.SharedPreferences;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Mohammad_PC on 2/7/2018.
 */

public class LoginModel {

    private String Email;
    private String Password;
    private String FirstName;
    private String LastName;
    private String LanguageDirection;
    private String Token;
    private String StoreName;
    private String ErrorMessage;
    private Stores[] stores;

    private Integer ErrorCode;
    private Integer LanguageId;
    private Integer StoreId;

    private class Stores{
        private String StoreId;
        private String StoreName;
    }

    public boolean isError(JSONObject jr) throws JSONException{
        return jr.getBoolean("HasError");
    }

    public boolean setModel(JSONObject jr, LoginActivity loginActivity) throws JSONException {

        boolean flag = jr.getBoolean("HasError");
        if(flag){

            this.ErrorCode = jr.getInt("ErrorCode");
            this.ErrorMessage = jr.getString("ErrorMessage");
            return false;

        }else{
            JSONObject data = new JSONObject(jr.getString("Data"));
            this.Token = data.getString("Token");
            this.FirstName = data.getString("FirstName");
            this.LastName = data.getString("LastName");
            this.Email = data.getString("Email");
            this.LanguageDirection = data.getString("LanguageDirection");
            this.LanguageId = data.getInt("LanguageId");
            Log.e("Token", this.Token);


            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(loginActivity);
            sharedPreferences.edit().putString(QuickstartPreferences.LOGIN_TOKEN, this.Token).apply();
            sharedPreferences.edit().putString(QuickstartPreferences.LOGIN_MODEL_JSON_AS_STRING, jr.toString()).apply();




            return true;
        }
    }




}
