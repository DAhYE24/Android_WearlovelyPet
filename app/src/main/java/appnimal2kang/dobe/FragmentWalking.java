package appnimal2kang.dobe;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/*

 * Created by KANG DAHYE and LEE SOYOUNG / Seoul Women's University / Multimedia major

*/
public class FragmentWalking extends Fragment implements OnMapReadyCallback{
    /* XML declare */
    TextView txtTimer, txtProperWalking; // TextView : timer content, proper quantity of walking
    Button btnTimer, btnShowRecord, btnTimerPause, btnTimerStop, btnCapture; // Button : timer starting, record of walking, timer pausing, timer stopping, walking path capturing
    ImageButton btnRefresh; // ImageButton : refreshing mapFragment
    LinearLayout layout_timerBtn; // Layout : timer pause & stop button

    /* Timer declare & init */
    Handler timerhandler = new Handler();
    long start_time = 0L, updated_time = 0L, timeInMilliseconds = 0L, timeSwapBuff = 0L;
    int hours = 0, secs = 0, mins = 0, milliseconds = 0;

    /* Map element declare */
    private GoogleMap mapValue; // Get map's value
    PolylineOptions walkingPath; // Set options for draw line : coordinate, color and so on
    Polyline drawPath; // Parameter for drawing polyline

    /* Java declare */
    String strDate, strCompareDate; // String : today's date, to compare date with strDate
    Boolean walkChk = false, pauseChk = false; // Boolean : check today's first walking, check timer pause
    SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Dateformat : date
    Bitmap tempPic; // Bitmap : map capturing data
    Double dLat, dLon; // 지도에 그리기 위해서 받아오는 값
    String strTime;
    public static StringBuffer latBuffer, lonBuffer;

    /* SharedPrefereneces */
    SharedPreferences walkInfo; // SharedPrefernces : information about walking
    SharedPreferences.Editor editor; // SharedPrefernces : information about walking

    /* TEXT MANAGER */
    TextFileManager fileM_name = new TextFileManager("DogName", ((StartActivity)StartActivity.mContext)); // Get : dog name
    TextFileManager fileM_properWalking = new TextFileManager("ProperWalking", ((StartActivity)StartActivity.mContext)); // Registration : dog birthday
    TextFileManager fileM_lon = new TextFileManager("LonValues", ((StartActivity)StartActivity.mContext)); // 위치 값
    TextFileManager fileM_lat = new TextFileManager("LatValues", ((StartActivity)StartActivity.mContext)); // 위치 값

    /* 일정시간마다 데이터 불러오는 핸들러 */
    public final static int REPEAT_DELAY = 60000; // 현재는 1분으로 설정, 3분 180000
    public Handler repeatGetData;

    /* Php Networking declare */
    String gpslist;
    phpDown task;
    phpUpdate task_update;
    boolean isMobileConnect, isWifiConnect, isMobileAvailable, isWifiAvailable; //네트워크 체크 요소
    String drawLong, drawLati;

    /* 데이터 통신 후에 실행되도록 */
    int testphp = 0;
    int timephp = 0; // 1일 때는 지난 값 저장후, 2일 때 종료 버튼 누르고

    /* Android life cycle : OnCreateView() -> Same as OnCreate(), but fragment use onCreateView() to show UI */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View walkView = inflater.inflate( R.layout.walking_fragment, container, false ); // Connect with walking_fragment.xml

        /* Throughout SupportMapFragment, connect with MapFragment*/
        final SupportMapFragment mapFragment = (SupportMapFragment)this.getChildFragmentManager().findFragmentById(R.id.map_walking); // this.getChildFragmentManger
        mapFragment.getMapAsync(this); // Set the 'CallBack' on the fragment with using getMapAsync()

        /* Init XML elements */
        btnTimer = (Button)walkView.findViewById(R.id.btnTimer);
        btnShowRecord = (Button)walkView.findViewById(R.id.btnShowRecord);
        btnTimerPause = (Button)walkView.findViewById(R.id.btnTimerPause);
        btnTimerStop = (Button)walkView.findViewById(R.id.btnTimerStop);
        txtTimer = (TextView)walkView.findViewById(R.id.txtTimer);
        layout_timerBtn = (LinearLayout)walkView.findViewById(R.id.layout_timerBtn);
        btnCapture = (Button)walkView.findViewById(R.id.btnCapture);
        btnRefresh = (ImageButton)walkView.findViewById(R.id.btnRefresh);
        txtProperWalking = (TextView)walkView.findViewById(R.id.txtProperWalking);
        latBuffer = new StringBuffer();
        lonBuffer = new StringBuffer();

        /* 네트워크 체크 */
        ConnectivityManager manager = (ConnectivityManager) getContext().getSystemService (Context.CONNECTIVITY_SERVICE);
        isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();

        /* SharedPreferences's data */
        walkInfo = getActivity().getSharedPreferences("walkInfo", Context.MODE_PRIVATE);
        walkChk = walkInfo.getBoolean("walkChk", false); // Check first walking
        pauseChk = walkInfo.getBoolean("pauseChk", false); // Check pausing state
        strDate = walkInfo.getString("walkDate", "error"); // Walking's date

        /* Handler setting */
        repeatGetData = new Handler(){
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                Log.i("jjang", "핸들러가 시작");
                testphp = 2;
                findLocation();
                this.sendEmptyMessageDelayed(0, REPEAT_DELAY);
            }
        };

        /* UI */
        layout_timerBtn.setVisibility(View.GONE); // Hide timer pausing & stopping button

        /* Check whether it is first time or not*/
        if(walkChk == false) { // if it is first time
            pauseChk = false;
        }else{
            /* Compare with today's date */
            Date dateChk = new Date(System.currentTimeMillis());
            strCompareDate = CurDateFormat.format(dateChk);
            if(!strCompareDate.equals(strDate)){
                /* 종료를 안하고는 다음 날에 산책을 시작한 경우 */
                AlertDialog dialog = exitCheck();
                dialog.show();
            }
        }

        /* Check state of pausing */
        if(pauseChk == false){
            txtTimer.setText("00:00:00"); // set the time like "00:00:00"
        }else{
            timeSwapBuff = walkInfo.getLong("pauseTime", 0);
            start_time = SystemClock.uptimeMillis();
            timeInMilliseconds = SystemClock.uptimeMillis() - start_time;
            updated_time = walkInfo.getLong("updateTime", 0);
            secs = (int) (updated_time / 1000);
            mins = secs / 60;
            hours = mins / 60;
            secs = secs % 60;
            //txtTimer.setText("" + String.format("%02d", hours) + ":" + String.format("%02d", mins) + ":"+ String.format("%02d", secs));
            txtTimer.setText(String.format("%02d:%02d:%02d", hours, mins, secs));
        }

        /* Show proper walking time for dog */
        showProperWalking();

        /* Button : walking start */
        btnTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pauseChk == true) {
            /* 버퍼값도 가져오기 */
                    Log.i("DHJJ", "버퍼 값 가져오자 : " + fileM_lat.load());

                    if (!fileM_lat.load().equals("")) {
                        latBuffer.append(fileM_lat.load());
                        lonBuffer.append(fileM_lon.load());
                    }
                }
                testphp = 1;
                findLocation(); //서버에서 받아오기
            }
        });

        /* Button : walking pause */
        btnTimerPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timerPause();
            }
        });

        /* Button : walking exit */
        btnTimerStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timerExit();
            }
        });

        /* Button : show walking record fragment */
        btnShowRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fg = new FragmentWalkRecord();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.add(R.id.fg_forWalk, fg);
                transaction.addToBackStack(null); // Add last fragment into BACK STACK
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE); // Animation
                transaction.commit();
            }
        });

        /* Button : refresh walking path */
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testphp = 2;
                findLocation();
            }
        });

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturePath();
            }
        });
        return walkView;
    }

    /* Android life cycle : onPause() */
    @Override
    public void onPause() {
        super.onPause();
        pauseChk = true;
        timeSwapBuff += timeInMilliseconds;
        // ♥save 그 함수 쓰기

        Log.i("hollys", " ONPAUSE timeSwapBuff : " + timeSwapBuff + ", start_time : " + start_time + ", updated_time : " + updated_time);
         //♥위경도 값 받아오는 핸들러 중지
        repeatGetData.removeMessages(0);

        /* Save pausing time and notice*/
        walkInfo = getActivity().getSharedPreferences("walkInfo", Context.MODE_PRIVATE);
        editor = walkInfo.edit();
        editor.putBoolean("pauseChk", pauseChk);
        editor.putLong("pauseTime", timeSwapBuff);
        editor.putLong("updateTime", updated_time);
        editor.commit();
    }

    /* 어제 기록 저장*/
    private AlertDialog exitCheck() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("산책저장");
        builder.setMessage("지난 산책 기록을 저장하고 다시 시작하겠습니다");

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                walkChk = false; // 첫 산책으로 변경
                pauseChk = false; // 일시정지도 초기화
                strDate = String.valueOf(timeSwapBuff); // 지난 산책량 저장
                strTime = String.valueOf(walkInfo.getLong("updateTime", 0)); // 지난 산책 시간 받아오기
                // ♥timeSwapBuff = 0;
                // ♥testPhp = 3; 해야하는지?
                timephp = 1;

                saveWalkingDateTime(strDate, strTime);

                /* ♥경로 캡쳐해서 저장 */
                /* ♥walkingPath.remove(); 지난 모든 경로 지우기 */

                /* Save data */
                walkInfo = getActivity().getSharedPreferences("walkInfo", Context.MODE_PRIVATE);
                editor = walkInfo.edit();
                editor.putBoolean("walkChk", walkChk);
                editor.putBoolean("pauseChk", pauseChk);
                editor.putLong("pauseTime", timeSwapBuff);
                editor.commit();

                /* 해당 액티비티 재시작 */
                Intent intent = new Intent(getContext(), Walking.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        AlertDialog dialog = builder.create();
        return dialog;
    }

    /* -----------------------------------------FUNCTION-------------------------------------------- */
    /* Show proper walking time */
    private void showProperWalking() {
        txtProperWalking.setText("\"" + fileM_name.load() + "\"의\n         적정 산책량은 " + fileM_properWalking.load() + "분입니다");
    }

    /* ------------------------------------FUNCTION FOR TIMER-------------------------------------- */
    /* Timer starting */
    private void timerStart() {
        /* Change xml elements */
        btnTimer.setVisibility(View.GONE); // Hide start button
        layout_timerBtn.setVisibility(View.VISIBLE); // Show pause & stop button

        /* Save today's date */
        if(walkChk == false){
            Date date = new Date(System.currentTimeMillis());
            strDate = CurDateFormat.format(date); // today's date
            walkChk = true;

            /* Save data */
            walkInfo = getActivity().getSharedPreferences("walkInfo", Context.MODE_PRIVATE);
            editor = walkInfo.edit();
            editor.putString("walkDate", strDate);
            editor.putBoolean("walkChk", walkChk);
            editor.commit();
        }
        start_time = SystemClock.uptimeMillis();
        timerhandler.postDelayed(updateTimer, 0);
        Log.i("hollys", "TIMER START timeSwapBuff : " + timeSwapBuff + ", start_time : " + start_time + ", updated_time : " + updated_time);
    }

    /* Timer pausing */
    private void timerPause() {
        pauseChk = true;

        timeSwapBuff += timeInMilliseconds;
        layout_timerBtn.setVisibility(View.GONE);
        btnTimer.setVisibility(View.VISIBLE);
        timerhandler.removeCallbacks(updateTimer);
        Log.i("hollys", " TIMER PAUSE timeSwapBuff : " + timeSwapBuff + ", start_time : " + start_time + ", updated_time : " + updated_time);

        //♥위경도 값 받아오는 핸들러 중지
        repeatGetData.removeMessages(0);

        // 지금까지 그려진 값 받아오기
        toSaveWalkingPath();

        SharedPreferences walkInfo = getActivity().getSharedPreferences("walkInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = walkInfo.edit();
        editor.putBoolean("pauseChk", pauseChk);
        editor.putLong("pauseTime", timeSwapBuff);
        editor.commit();
    }

    /* Timer exiting */
    private void timerExit() {
        AlertDialog dialog = exitWalking();
        dialog.show();
    }

    /* Dialog of timer exiting */
    private AlertDialog exitWalking() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("산책종료");
        builder.setMessage("오늘의 산책을 종료하겠습니까?");

        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), "산책이 종료됩니다", Toast.LENGTH_SHORT).show();

                /* Init parameters */
                walkChk = false; // 첫 산책으로 초기화
                pauseChk = false; // 일시정지 초기화
                timeSwapBuff += timeInMilliseconds;
                strTime = String.valueOf(updated_time); // 현재까지 시간 저장

                // ♥위경도 값 받아오는 핸들러 중지
                repeatGetData.removeMessages(0);

                testphp = 3; // Down php에서 경로 받아오기
                findLocation(); // 경로 받아오기
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), "산책을 계속합니다", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();
        return dialog;
    }

    /* Timer handler */
    public Runnable updateTimer = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - start_time;
            updated_time = timeSwapBuff + timeInMilliseconds;
            secs = (int) (updated_time / 1000);
            mins = secs / 60;
            hours = mins / 60;
            secs = secs % 60;
            txtTimer.setText("" + String.format("%02d", hours) + ":" + String.format("%02d", mins) + ":"
                    + String.format("%02d", secs));
            timerhandler.postDelayed(this, 0);
        }
    };

    /* ------------------------------------FUNCTION FOR MAP----------------------------------------- */
    /* (1) Map setting */
    @Override
    public void onMapReady(GoogleMap gm) {
        mapValue =  gm;
        if(pauseChk == false) { // 처음 실행을 한 경우
            LatLng loc = new LatLng(37.629472, 127.090484); // 위치 좌표 loc 설정(현재의 서울여대로 설정해둔 상황)
            gm.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15)); //loc 다음 숫자 15는 확대 정도
        }else{ // 일시정지 후 들어온 경우
            Log.i("DHJJ", "일시정지 후에 지도 다시 그리기");
            /* ♥if문으로 저장된 경로 있는지 확인후, 처음 화면에 접속했을 때 경로가 지도에 그려지도록 하기 */
            String [] sLatTemp = String.valueOf(fileM_lat.load()).split(",");
            String [] sLonTemp = String.valueOf(fileM_lon.load()).split(",");
            double sLat = Double.parseDouble(sLatTemp[0]);
            double sLon = Double.parseDouble(sLonTemp[0]);
            gm.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(sLat, sLon), 15)); //아까 위치로 받아오기
        }
    }

    /* (2) Draw start coordinate */
    private void markStarting() {
        /* 경로 그리기*/
        walkingPath = new PolylineOptions()
                .add(new LatLng(dLat, dLon))
                .color(Color.rgb(255, 178, 245)) // 경로 색상
                .width(25);
        drawPath = mapValue.addPolyline(walkingPath);

        String [] sLatTemp = String.valueOf(latBuffer).split(",");
        String [] sLonTemp = String.valueOf(lonBuffer).split(",");
        double sLat = Double.parseDouble(sLatTemp[0]);
        double sLon = Double.parseDouble(sLonTemp[0]);

        /* ♥ 첫 시작한 부분으로 카메라 이동 */
        mapValue.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(sLat, sLon), 15));

        /* ♥ 산책 시작, 끝 위치를 표시하는 마커 */
        mapValue.addMarker(new MarkerOptions().position(new LatLng(sLat, sLon)).title("시작지점")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mark_start))); //첫 시작 부분 Marker 추가

        /* ♥일정시간마다 데이터 값 불러오기 시작 */
        repeatGetData.sendEmptyMessage(0);
    }

    /* (3) 마지막 마커 찍기 */
    private void markEnding(){
        /* 버퍼 불러오기 */
        String [] sLatTemp = String.valueOf(latBuffer).split(",");
        String [] sLonTemp = String.valueOf(lonBuffer).split(",");

        /* 마지막 경로 연결하기*/
        List<LatLng> points = drawPath.getPoints();
        points.add(new LatLng(dLat, dLon));
        drawPath.setPoints(points);

        int last = sLatTemp.length - 1;
        /* 종료 지점 마커 찍기 */
        mapValue.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(sLatTemp[last]), Double.parseDouble(sLonTemp[last]))).title("종료지점")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.mark_end))); //마지막 위치 Marker 추가

        /* 그리고는 시간 저장하기 */
        timephp = 2;
        saveWalkingDateTime(strDate, strTime);

//        // 경로값도 저장
//        toSaveWalkingPath();
    }

    /* (4) 새로운 좌표를 그린 후에 연결시켜서 경로 만들기*/
    private void drawNewCoordinate(double lat, double lon) {
        /* ♥지도 다 지우기 */
        drawPath.remove();
        /* ♥스트링 버퍼 값 기반으로 지도 그리기 */
        String [] sLatTemp = String.valueOf(latBuffer).split(",");
        String [] sLonTemp = String.valueOf(lonBuffer).split(",");

        walkingPath = new PolylineOptions()
                .color(Color.rgb(255, 178, 245)) // 경로 색상
                .width(25);

        for(int i = 0; i < sLatTemp.length; i++){
            walkingPath.add(new LatLng(Double.parseDouble(sLatTemp[i]),Double.parseDouble(sLonTemp[i])));
        }

        /* ♥다시 그려주기 */
        drawPath = mapValue.addPolyline(walkingPath);

//        List<LatLng> points = drawPath.getPoints();
//        points.add(new LatLng(lat, lon));
//        drawPath.setPoints(points);
    }

    /* (5) 경로 저장 */
    private void toSaveWalkingPath() {
//        List<LatLng> savePath = drawPath.getPoints();
        /* ♥List<LatLng>인 savePath 값은 저장하는 법 연구 */
        /* ♥stringbuffer 저장 */
        fileM_lon.save(String.valueOf(lonBuffer));
        fileM_lat.save(String.valueOf(latBuffer));
        lonBuffer.setLength(0);
        latBuffer.setLength(0);
    }

    /* (6) Capture map */
    private void capturePath() {
        if (mapValue == null)   return;

        /* Save image data into tempPic */
        final GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                if(snapshot != null) { // First time click 'capture button', it returns 'null' data
                    tempPic = snapshot;
                    saveMapOnDevice();
                }else{
                    capturePath();
                }
            }
        };

        /* Get image data from main thread */
        mapValue.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mapValue.snapshot(callback);
            }
        });
    }

    /* Save map's picture into Device */
    private void saveMapOnDevice() {
        String folder = "Pictures/WearLovelyPet";
        if (tempPic == null)    return;
        else {
            SimpleDateFormat forDatetime = new SimpleDateFormat("yyyyMMddHHmmss_산책기록");

            Date getTime = new Date();
            String dateString = forDatetime.format(getTime);
            File sdCardPath = Environment.getExternalStorageDirectory();
            File dir = new File(Environment.getExternalStorageDirectory(), folder);

            /* Checking the folder path exist */
            if (!dir.exists()) dir.mkdirs();
            FileOutputStream fos;
            String picTitle = sdCardPath.getPath() + "/" + folder + "/" + dateString + ".jpg";

            try {
                fos = new FileOutputStream(picTitle);
                tempPic.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                /* Update all media list bu MEDIA SCANNER */
                getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file:" + picTitle)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Toast.makeText(getContext(), dateString + ".jpg 저장되었습니다", Toast.LENGTH_LONG).show();
        }
    }

    /* -----------------------------------------SERVER-DATA----------------------------------------- */
    /* Save Stroll(Walking) DateTime to Server DB */
    public void saveWalkingDateTime(String date, String time){
        task_update = new phpUpdate();
        task_update.execute("http://14.63.225.210/walking.php?date=" + date + "&time=" +time);
    }

    /* Get GPS from Server DB */
    public void findLocation(){
        task = new phpDown();
        task.execute("http://14.63.225.210/location.php");
//        item = new ListItem();
    }

    /* Send & Update Data : php (serverDB) */
    public class phpUpdate extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder resultText = new StringBuilder();
            try{
                // 연결 url 설정
                URL url = new URL(urls[0]);
                // 커넥션 객체 생성
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                // 연결되었으면.
                if(conn != null){
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);
                    // 연결되었음 코드가 리턴되면.
                    if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        for(;;){
                            // 웹상에 보여지는 텍스트를 라인단위로 읽어 저장.
                            String line = br.readLine();
                            if(line == null) break;
                            // 저장된 텍스트 라인을 jsonHtml에 붙여넣음
                            resultText.append(line);
                        }
                        br.close();
                    }
                    conn.disconnect();
                }
            } catch(Exception ex){
                ex.printStackTrace();
            }
            return resultText.toString();

        }

        protected void onPostExecute(String str){
            if(str.equals("1")){
                Toast.makeText(getContext(), "산책 시간이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                if(timephp == 1) { //지난 종료
                }else if(timephp == 2){ //현재종료
                    /* Save data */
                    walkInfo = getActivity().getSharedPreferences("walkInfo", Context.MODE_PRIVATE);
                    timeSwapBuff = 0; //시간 초기화
                    editor = walkInfo.edit();
                    editor.putBoolean("walkChk", walkChk);
                    editor.putBoolean("pauseChk", pauseChk);
                    editor.putLong("pauseTime", timeSwapBuff);
                    editor.commit();

                /* Init timer */
                    start_time = 0L;
                    timeInMilliseconds = 0L;
                    timeSwapBuff = 0L;
                    updated_time = 0L;
                    secs = 0;
                    mins = 0;
                    hours = 0;
                    milliseconds = 0;
                    timerhandler.removeCallbacks(updateTimer);
                    layout_timerBtn.setVisibility(View.GONE);
                    btnTimer.setVisibility(View.VISIBLE);
                    txtTimer.setText("00:00:00");

                    // 오늘 산책경로 저장
                    capturePath();

                    // ♥버퍼 값 초기화, 그리고 모두 삭제하기
                    Log.i("DHJJ", "버퍼를 초기화 시킵니다.");

                    lonBuffer.setLength(0);
                    latBuffer.setLength(0);
                    fileM_lat.delete();
                    fileM_lon.delete();
                }
            }
            else{
                Toast.makeText(getContext(),"네트워크 연결이 불안정합니다. ",Toast.LENGTH_LONG).show();
            }
        }
    }
    /* Receive Data : php (serverDB) */
    private class phpDown extends AsyncTask<String, Integer, String> {
        ProgressDialog loading = new ProgressDialog(getContext());

        @Override
        protected void onPreExecute() {
            /* Network connecting check*/
            if(!(isMobileConnect && isMobileAvailable) && !(isWifiConnect && isWifiAvailable)) {
                Toast.makeText(getContext(), "네트워크 연결이 필요합니다", Toast.LENGTH_LONG).show();
                /* If network is unconnected, finish */
                cancel(true);
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
            loading.dismiss();
        }
    }

    public void setListData(){
        try{
            JSONObject root = new JSONObject(gpslist);
            //JSONArray 이름 : "results"
            JSONArray ja = root.getJSONArray("results");

            for(int i=0;i<ja.length();i++){
                JSONObject jo = ja.getJSONObject(i);
                drawLati = jo.getString("latitude");
                drawLong = jo.getString("longitude");
            }

            dLat = Double.parseDouble(drawLati);
            dLon = Double.parseDouble(drawLong);

            // 버퍼에 받아오는 Lon, Lat 값 저장하기
            latBuffer.append(dLat + ",");
            lonBuffer.append(dLon + ",");

            Log.i("DHJJ", "버퍼 값 저장 현황 : " + latBuffer);

            switch (testphp){
                case 1: // 타이머 시작 버튼 눌렀을 때
                    timerStart(); // 타이머 시작
                    markStarting(); // 첫 시작 마커 찍기
                    break;
                case 2: // 타이머 새로고침 및 3분마다 값 불러오는 경우
                    drawNewCoordinate(dLat, dLon);
                    break;
                case 3: // 타이머 종료하는 경우
                    //♥마지막 경로값으로 마커 찍기
                    markEnding();
                    break;
                default:
                    break;
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}