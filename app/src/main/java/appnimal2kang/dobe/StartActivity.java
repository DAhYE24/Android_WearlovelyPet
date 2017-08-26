package appnimal2kang.dobe;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class StartActivity extends AppCompatActivity {
    /* XML Declare*/
    ImageButton startBtn; // ImageButton : to register dog's info
    TextView startTxt; // TextView : to show registration text

    /* JAVA Declare */
    Dialog_petInfo registerDialog; // RegisterPetDialog : to connect Dialog_petInfo.java
    public static Context mContext; // Context : to use activity's function in Dialog_petInfo.java
    boolean isMobileConnect, isWifiConnect, isMobileAvailable, isWifiAvailable;

    /* TEXTFILE */
    TextFileManager fileM_rgCheck = new TextFileManager("AppRgCheck", this); // Registration : to check registration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerDialog = new Dialog_petInfo(StartActivity.this); // Declare registration dialog
        registerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // To hide Title layer on dialog
        setContentView(R.layout.activity_start);

        /* Init */
        startBtn = (ImageButton)findViewById(R.id.startBtn);
        startTxt = (TextView)findViewById(R.id.startTxt);
        mContext = this; // to save activity's context

        /* OnClick : dog registration */
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerDialog.show();
                registerDialog.setCancelable(false); // Users must register dog info at the first time
            }
        });

        /* Check Network and registration status */
        startApp();
    }

    private void startApp() {
        /* Load registration status */
        String check = fileM_rgCheck.load();

        /* Network Parameter */
        Handler handler = new Handler();
        ConnectivityManager manager = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
        isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();

        /* Network Checking */
        if(!(isMobileConnect && isMobileAvailable) && !(isWifiConnect && isWifiAvailable)){
            if(check.equals("true")) {
                removeStartMark();
            }
            Toast.makeText(this, "네트워크 연결이 필요합니다", Toast.LENGTH_LONG).show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish(); // Exit app
                }
            }, 1500);
        }else{ // If network connected
            if(check.equals("true")) { // Check registering status
                removeStartMark();
                // Go to MainActivity after 1.5 second
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Intent intent = new Intent(StartActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // To remove this activity, when users click device's back button
                    }
                }, 1500);
            }
        }
    }

    private void removeStartMark() {
        startBtn.setVisibility(View.GONE);
        startTxt.setVisibility(View.GONE);
    }
}
