package com.matechco.omnisales;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import static android.Manifest.permission.PROCESS_OUTGOING_CALLS;
import static android.Manifest.permission.CALL_PHONE;
import android.widget.Toast;


public class RuntimePermissions extends Activity {

    static final Integer CALL = 0x1;
    static final Integer OUTCALLS = 0x2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runtime_permissions);
        ask("call");
    }

    public void ask(String v){
        switch (v){
            case "call":
                if(askForPermission(CALL_PHONE,CALL)){
                    ask("outcalls");
                }
                break;
            case "outcalls":
                askForPermission(PROCESS_OUTGOING_CALLS,OUTCALLS);
                break;

            default:
                break;
        }
    }

    private boolean askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(RuntimePermissions.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(RuntimePermissions.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(RuntimePermissions.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(RuntimePermissions.this, new String[]{permission}, requestCode);
                return true;
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED){
            switch (requestCode) {
                //Location
                case 1:

                    break;
                //Call
                case 2:

                    break;
                //Write external Storage
                case 3:
                    break;
                //Read External Storage
                case 4:

                    break;
                //Camera
                case 5:

                    break;
                //Accounts
                case 6:

            }

            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
