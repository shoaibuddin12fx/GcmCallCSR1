package com.matechco.omnisales;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static com.matechco.omnisales.QuickstartPreferences.LOGIN_TOKEN;

/**
 * Created by Mohammad_PC on 2/7/2018.
 */

public class JsonUtil {

    public static boolean IsTokenExpire(MyGcmListenerService self) throws JSONException {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(self);
        String token = sharedPreferences.getString(LOGIN_TOKEN, "NULL");

        String urlAdress="http://testapi.omnisales.pk/Account/CheckTokenExpire";

        JSONObject jr = postTokenToUrl( urlAdress ,token);
        LoginModel loginModel = new LoginModel();
        return loginModel.isError(jr);

    }

    private static JSONObject postTokenToUrl(String urlAdress, String token){
        try {

            URL url = new URL(urlAdress);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            conn.setRequestProperty("Token", token);
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());


//            wr.write( jsonParam.toString() );
//            wr.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while((line = reader.readLine()) != null)
            {
                sb.append(line);
                break;
            }



            Log.e("MSG_RETURN", sb.toString());
            JSONObject jr = new JSONObject(sb.toString());
            return jr;



        }catch (Exception e) {
            e.printStackTrace();
            JSONObject jr = null;
            return jr;
        }



    }

    public static boolean postLogin(JSONObject jsonParam, LoginActivity loginActivity) throws JSONException {

        //String link="http://android.matechco.com/gcm/insert.php";
        String urlAdress="http://testapi.omnisales.pk/Account/LoginMobile";

        JSONObject jr = postDataToUrl(urlAdress, jsonParam);
        LoginModel loginModel = new LoginModel();
        return loginModel.setModel(jr, loginActivity);

    }

    private static JSONObject postDataToUrl(String urlAdress, JSONObject jsonParam){
        try {

            URL url = new URL(urlAdress);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            conn.setRequestProperty("Token", "12345678");
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());


            wr.write( jsonParam.toString() );
            wr.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while((line = reader.readLine()) != null)
            {
                sb.append(line);
                break;
            }



            Log.e("MSG_RETURN", sb.toString());
            JSONObject jr = new JSONObject(sb.toString());
            return jr;



        }catch (Exception e) {
            e.printStackTrace();
            JSONObject jr = null;
            return jr;
        }



    }

}
