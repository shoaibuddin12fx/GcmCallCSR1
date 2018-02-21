package com.matechco.omnisales;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.matechco.omnisales.QuickstartPreferences.CALL_LOG_ID;
import static com.matechco.omnisales.QuickstartPreferences.LOGIN_TOKEN;
import static com.matechco.omnisales.QuickstartPreferences.ORDER_ID;
import static com.matechco.omnisales.QuickstartPreferences.STORE_ID;


public class FileUpload {

    public static final String MY_PREF = "MY_PREF";
    public static final String REFERENCE_KEY = "REFERENCE_KEY";
    private int serverResponseCode;
    private Context context;
    private SharedPreferences sharedPreferences;
    public String reference = null;


    public Boolean UploadTheFile(String sourceFileUri, TService self){

        /*
        int incr;
        // Do the "lengthy" operation 20 times
        for (incr = 0; incr <= 100; incr+=15) {
            // Sets the progress indicator to a max value, the
            // current completion percentage, and "determinate"
            // state


            // that takes time
            try {
                // Sleep for 5 seconds
                Thread.sleep(5*1000);
            } catch (InterruptedException e) {
                Log.e("TAG", "sleep failure");
            }
        }*/

        /************* Php script path ****************/
        String upLoadServerUri="http://testapi.omnisales.pk/Orders/SaveAudio";

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);



        if (!sourceFile.isFile()) {

            Log.e("uploadFile", "Source File not exist : " + sourceFileUri);
            return false;

        }else{

            try {

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(self);
                String token = sharedPreferences.getString(LOGIN_TOKEN, "NULL");
                String order_id = sharedPreferences.getString(ORDER_ID, "NULL");
                String store_id = sharedPreferences.getString(STORE_ID, "NULL");
                String call_log_id = sharedPreferences.getString(CALL_LOG_ID, "NULL");

                Log.e("SourceUrl", sourceFileUri);
                String fileName = sourceFileUri.substring( sourceFileUri.lastIndexOf('/')+1, sourceFileUri.length() );
                Log.e("FileName", fileName);


                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Close");
                //conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("Token", token);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"OrderId\""+ lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(order_id);
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"StoreId\""+ lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(store_id);
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"CallLogId\""+ lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(call_log_id);
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + fileName +"\""+ lineEnd);
                dos.writeBytes(lineEnd);

                //Log.e(Tag,"Headers are written");

                // create a buffer of maximum size
                bytesAvailable = fileInputStream.available();

                maxBufferSize = 1024;
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0)
                {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0,bufferSize);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // close streams
                fileInputStream.close();

                dos.flush();
//                dos.close();

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


//                dos.writeBytes(twoHyphens + boundary + lineEnd);
//
//                dos.writeBytes("Content-Disposition: form-data; name='uploaded_file';filename="+ fileName +""+ lineEnd);
//                dos.writeBytes(lineEnd);
//
//                // create a buffer of  maximum size
//                bytesAvailable = fileInputStream.available();
//
//                bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                buffer = new byte[bufferSize];
//
//                // read file and write it into form...
//                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//                while (bytesRead > 0) {
//
//                    dos.write(buffer, 0, bufferSize);
//                    bytesAvailable = fileInputStream.available();
//                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//                }
//
//                // send multipart form data necesssary after file data...
//                dos.writeBytes(lineEnd);
//                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);


                if(serverResponseCode == 200){

                    reference = "http://android.matechco.com/csrcall/recordings/"+fileName;
                    Log.e("upload_success", reference);

                }


                //close the streams //
//                fileInputStream.close();
//                dos.flush();
                dos.close();



            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Upload_Exception", "Exception : " + e.getMessage(), e);
                return false;
            }


            Log.e("SERVER_RESPONSE_CODE",serverResponseCode+"");
            return true;
        }














    }

}
