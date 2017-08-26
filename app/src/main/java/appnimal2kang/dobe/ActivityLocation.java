package appnimal2kang.dobe;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ActivityLocation extends FragmentActivity implements OnMapReadyCallback {
    /* XML Declare */
    Button btnDistance, btnDogLocation; // Button : distance, current location

    /* JAVA Declare */
    Dialog_distance Dialog_distance; // Dialog_distance.java
    public static Context rContext; // Context : to use these functions in Dialog_distance.java
    float zoomValue;
    private GoogleMap map;
    double pLat, pLon; // petLat, petLon

    /* Php Networking declare */
    String gpslist;
    String petLong, petLati; //현재 반려견의 위치
    phpDown task;

    /* Network checking parameter declare */
    boolean isMobileConnect, isWifiConnect, isMobileAvailable, isWifiAvailable;

    /* TEXTFILE */
    TextFileManager fileM_distance = new TextFileManager("Distance", this); // Registration : distance
    TextFileManager fileM_phone = new TextFileManager("EmergencyNum", this); // Registration : emergency contact number
    TextFileManager fileM_latMap = new TextFileManager("latMap", this); // Registration : petLati
    TextFileManager fileM_lonMap = new TextFileManager("lonMap", this); // Registration : petLong
    TextFileManager fileM_locationCheck = new TextFileManager("LocationRgCheck", this); // Registration : 위치등록했는지 체크

    /* Android life cycle : onCreate() */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dialog_distance = new Dialog_distance(ActivityLocation.this);
        Dialog_distance.requestWindowFeature(Window.FEATURE_NO_TITLE); // To hide title bar on Dialog_distance dialog
        setContentView(R.layout.activity_map);

        /* Init */
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_location);
        btnDistance = (Button)findViewById(R.id.btnDistance);
        btnDogLocation = (Button)findViewById(R.id.btnDogLocation);
        rContext = this;

        /* 거리 등록 후에 멘트 수정 */
        if(fileM_locationCheck.load().equals("true")){
            btnDistance.setText("거리 수정");
        }

        /* Network checking init */
        ConnectivityManager manager = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
        isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();

        /* To connect MapFragment */
        mapFragment.getMapAsync(this); // Set the callback on the fragment by using getMapAsync()

        /* Registration distance into */
        btnDistance.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Dialog_distance.show(); // To connect Dialog_distance.java(custom dialog)
            }
        });

        /* Get dog's current location */
        btnDogLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fileM_locationCheck.load().equals("true")){
                    task = new phpDown();
                    task.execute("http://14.63.225.210/location.php");
                }else{
                    Toast.makeText(getApplicationContext(), "거리 설정을 해야 사용할 수 있습니다", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    /* Notice emergency situation */
    private AlertDialog emergencyCalling() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("긴급상황");
        builder.setMessage("반려견이 현재 반경거리 밖에 위치합니다\n비상연락망으로 연결하겠습니까?");

        /* Calling to saved number*/
        builder.setPositiveButton("통화", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent("android.intent.action.CALL", Uri.parse("tel:" + fileM_phone.load()));
                startActivity(i);
            }
        });

        /* Cancel dialog */
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        return dialog;
    }

    /* Calculate Distance */
    private double calculateDistance(double setLat, double setLon, double dogLat, double dogLon) {
    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
    /* Latitude/petLong spherical geodesy tools                         (c) Chris Veness 2002-2016  */
    /*                                                                                   MIT Licence  */
    /* www.movable-type.co.uk/scripts/latlong.html                                                    */
    /* www.movable-type.co.uk/scripts/geodesy/docs/module-latlon-spherical.html                       */
    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */

        double R = 6371e3;
        double dLat = toRad(dogLat-setLat);
        double dLon = toRad(dogLon-setLon);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(toRad(setLat)) * Math.cos(toRad(dogLat)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return d;
    }

    /* To calculate distance */
    private double toRad(double value) {
        value = value * Math.PI / 180;
        return value;
    }

    /* Google Map Fragment */
    @Override
    public void onMapReady(GoogleMap gm) {
        map = gm;
        Log.i("svt", fileM_latMap.load() + " 그리고 " + fileM_lonMap.load());
//        if(fileM_latMap.load().equals("") && fileM_lonMap.load().equals("")){
        if(fileM_latMap.load().equals("") && fileM_lonMap.load().equals("")){
            /*

               해당 어플은 서울여자대학교 학생 두 명이 만든 어플입니다
               따라서 처음에 지도의 좌표를'서울여대'로 설정해두었습니다
               This application is made by Seoul Women's university students
               So we set the first coordinates to the " Seoul Women's University"

            */
            gm.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.629430, 127.090431), 10));
        }else{
            /* To get location from TEXTFILE */
            LatLng loc;
            loc = new LatLng(Double.parseDouble(fileM_latMap.load()), Double.parseDouble(fileM_lonMap.load()));

            /* Set camera zoom value by distance */
            setCamera();

            /* Set map elements */
            gm.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomValue)); // Camera location
            gm.addMarker(new MarkerOptions().position(loc).title("설정지점")); // Put marker into selected position

            // Draw circle by distance : radius's unit is Meter(m)
            CircleOptions circleOptions = new CircleOptions().center(loc).radius(Double.parseDouble(fileM_distance.load()));
            Circle disCircle = gm.addCircle(circleOptions);
            disCircle.setFillColor(Color.argb(80, 223, 77, 77));
            disCircle.setStrokeWidth(0);
        }
    }

    /* Set camera zoom value by distance : unit is Meter(m) */
    private void setCamera() {
        Double temp = Double.parseDouble(fileM_distance.load());
        if(temp > 0 && temp <= 50)  zoomValue = 18;
        else if(temp > 50 && temp <= 100) zoomValue = 17;
        else if(temp > 100 && temp <= 250) zoomValue = 16;
        else if(temp > 250 && temp <= 500) zoomValue = 15;
        else zoomValue = 14;
    }

    /* Receive Data : php (serverDB) */
    private class phpDown extends AsyncTask<String, Integer, String> {
        ProgressDialog loading = new ProgressDialog(ActivityLocation.this);

        @Override
        protected void onPreExecute() {
            /* Network connecting check*/
            if(!(isMobileConnect && isMobileAvailable) && !(isWifiConnect && isWifiAvailable)) {
                Toast.makeText(ActivityLocation.this, "네트워크 연결이 필요합니다", Toast.LENGTH_LONG).show();
                /* If network is unconnected, finish this activity and then go to MainActivity */
                cancel(true);
//                Intent intent = new Intent(ActivityLocation.this, MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                finish();
//                startActivity(intent);
            }else{
                /* Show loading dialog */
                loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                loading.setMessage("로딩중입니다..");
                loading.show();
            }

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder jsonHtml = new StringBuilder();
            try {
                // 연결 url 설정
                URL url = new URL(urls[0]);
                // 커넥션 객체 생성
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                // 연결되었으면.
                if (conn != null) {
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);
                    // 연결되었음 코드가 리턴되면.
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        for (;;) {
                            // 웹상에 보여지는 텍스트를 라인단위로 읽어 저장.
                            String line = br.readLine();
                            if (line == null) break;
                            // 저장된 텍스트 라인을 jsonHtml에 붙여넣음
                            jsonHtml.append(line + "\n");
                        }
                        br.close();
                    }
                    conn.disconnect();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return jsonHtml.toString();
        }

        protected void onPostExecute(String str){
            gpslist = str;
            setListData();

            /* Dismiss loading dialog */
            loading.dismiss();
        }
    }

    private void toGetDogLocation() {
        /* Get coordinate from textfile */
        double uLat = Double.parseDouble(fileM_latMap.load());
        double uLon = Double.parseDouble(fileM_lonMap.load());

        pLat = Double.parseDouble(petLati);
        pLon = Double.parseDouble(petLong);

        Log.i("tst", pLat + " 그리고 " + pLon);

        /* Get distance between coordinates */
        double temp = calculateDistance(uLat, uLon, pLat, pLon);

        /* Put marker at dog's location */
        map.addMarker(new MarkerOptions().position(new LatLng(pLat, pLon)).title("반려견 위치"));

        /* Notice different ways by distance(temp) */
        if(temp > Double.parseDouble(fileM_distance.load())){
            AlertDialog dialog = emergencyCalling();
            dialog.show();
        }else{
            Toast.makeText(ActivityLocation.this, "현재 반경거리 이내에 위치하고 있습니다", Toast.LENGTH_SHORT).show();
        }
    }

    public void setListData(){
        try{
            JSONObject root = new JSONObject(gpslist);
            //JSONArray 이름 : "results"
            JSONArray ja = root.getJSONArray("results");

            for(int i=0;i<ja.length();i++){
                JSONObject jo = ja.getJSONObject(i);
                petLati = jo.getString("latitude");
                petLong = jo.getString("longitude");
            }

            /* php를 통해 받아온 값 통한 거리 계산*/
            toGetDogLocation();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}
