package appnimal2kang.dobe;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ManagementActivity extends AppCompatActivity implements View.OnClickListener{

    /* LAYOUT */
    LinearLayout layout_rest;
    LinearLayout layout_walk;
    LinearLayout layout_run;
    LinearLayout layout_vacc;

    /* Rest, Walk, Run Time Graph/Time */
    Graphic restview, walkview, runview;
    int resttime, walktime, runtime;
    public static int REST = 11;
    public static int WALK = 12;
    public static int RUN = 13;

    /* TEXTVIEW */
    TextView tv_resttime , tv_walktime, tv_runtime;
    TextView tvName1, tvName2;
    TextView tv_temp, tv_heartrate, tv_vacc;

    /* IMAGEVIEW */
    ImageView imgTitle;

    /* Receiver DATA param declare */
    String list;
    ListItem item;
    phpDown task;

    /* DogName Init*/
    String DogName;
    TextFileManager fileM_name = new TextFileManager("DogName", ((StartActivity)StartActivity.mContext));
    TextFileManager managementfileM_name = new TextFileManager("DogName", this);
    TextFileManager fileM_age = new TextFileManager("DogAge", ((MainActivity)MainActivity.maContext));
    TextFileManager fileM_chYear = new TextFileManager("CheckAYear",((MainActivity)MainActivity.maContext));
    TextFileManager managementfileM_age = new TextFileManager("DogAge", this);
    TextFileManager managementfileM_chYear = new TextFileManager("CheckAYear", this);
    TextFileManager managementfileM_iage = new TextFileManager("iAge", this);

    String age, chYear;
    String age_select;

    /* Network checking parameter declare */
    boolean isMobileConnect, isWifiConnect, isMobileAvailable, isWifiAvailable;

    /* To get context */
    public static Context vContext; // Context : to use activity's function in VaccinationList.java

    /* VaccinationList */
    VaccinationList vaccinationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);

        vContext = this; // Save context

        /* Network checking init */
        ConnectivityManager manager = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
        isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();

        age = managementfileM_age.load();
        chYear = managementfileM_chYear.load();
        if(chYear.equals("T")){
            age_select = age;
        }
        if(chYear.equals("F")){
            int iage = Integer.parseInt(age);
//            iage = iage * 12;
//            age_select = Integer.toString(iage);
            age_select = "12";
        }
        managementfileM_iage.save(age_select);

        /* Domain : php -> receive data */
        task = new phpDown();
        task.execute("http://14.63.225.210/management.php?age="+age_select);
        item = new ListItem();

        /* Title Image : Refresh Button */
        imgTitle = (ImageView) findViewById(R.id.imgTitle);
        imgTitle.setOnClickListener(this);

        /* Set DogName */
        managementfileM_name.save(fileM_name.load());
        DogName = managementfileM_name.load();
        tvName1 = (TextView) findViewById(R.id.tvName1);
        tvName2 = (TextView) findViewById(R.id.tvName2);
        tvName1.setText(DogName);
        tvName2.setText(DogName);

        //age
        managementfileM_age.save(fileM_age.load());
        managementfileM_chYear.save(fileM_chYear.load());

        /* Temperature, Heartbeat, Vaccination Init */
        tv_temp = (TextView) findViewById(R.id.tv_temp);
        tv_heartrate = (TextView) findViewById(R.id.tv_heartrate);
        tv_vacc = (TextView) findViewById(R.id.tv_vacc);

        /* Rest, Walk, Run Time Graph/Time Init */
        layout_rest = (LinearLayout) findViewById(R.id.layout_rest);
        layout_walk = (LinearLayout) findViewById(R.id.layout_walk);
        layout_run = (LinearLayout) findViewById(R.id.layout_run);
        layout_vacc = (LinearLayout) findViewById(R.id.layout_vacc);
        tv_resttime = (TextView) findViewById(R.id.tv_resttime);
        tv_walktime = (TextView) findViewById(R.id.tv_walktime);
        tv_runtime = (TextView) findViewById(R.id.tv_runtime);

        /* Rest, Walk, Run Time Graph setting */
        restview = new Graphic(this);
        walkview = new Graphic(this);
        runview = new Graphic(this);
        layout_rest.addView(restview);
        layout_walk.addView(walkview);
        layout_run.addView(runview);



        /* Vaccination Dialog */
        layout_vacc.setOnClickListener(this);
        vaccinationList = new VaccinationList(ManagementActivity.this);
    }

    /* Android life cycle : onPause*/
    @Override
    protected void onPause() {
        super.onPause();
        task.cancel(true); // Cancle AsyncTask
    }

    /* Receive Data : php (serverDB) */
    private class phpDown extends AsyncTask<String, Integer, String> {
        ProgressDialog loading = new ProgressDialog(ManagementActivity.this);

        @Override
        protected void onPreExecute() {
            /* Network connecting check*/
            if(!(isMobileConnect && isMobileAvailable) && !(isWifiConnect && isWifiAvailable)) {
                Toast.makeText(ManagementActivity.this, "네트워크 연결이 필요합니다", Toast.LENGTH_LONG).show();
                /* If network is unconnected, finish this activity and then go to MainActivity */
                Intent intent = new Intent(ManagementActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                finish();
                startActivity(intent);
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
            list = str;
            setListData();

            resttime = Integer.parseInt(item.getRest());
            walktime = Integer.parseInt(item.getWalk());
            runtime = Integer.parseInt(item.getRun());

            /* Redraw Graph with received time data */
            restview.reDraw(resttime);
            walkview.reDraw(walktime);
            runview.reDraw(runtime);
            setTimeText(REST, resttime);
            setTimeText(WALK, walktime);
            setTimeText(RUN, runtime);

            /* set Text(temp, hb, vacc) with received data */
            tv_temp.setText(item.getTemp());
            tv_heartrate.setText(item.getHB());
            tv_vacc.setText("예방접종이 "+item.getVacc()+"회 남았습니다.");

            /* Dismiss loading dialog */
            loading.dismiss();
        }


    }

    public void setListData(){
        try{
            JSONObject root = new JSONObject(list);
            //JSONArray 이름 : "results"
            JSONArray ja = root.getJSONArray("results");

            for(int i=0;i<ja.length();i++){
                JSONObject jo = ja.getJSONObject(i);
                String rest = jo.getString("rest");
                String walk = jo.getString("walk");
                String run = jo.getString("run");
                String temperature = jo.getString("temperature");
                String heartbeat = jo.getString("heartbeat");
                String vaccination = jo.getString("vaccination");
                addItem(rest, walk, run, temperature, heartbeat, vaccination);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    /* ListItem Init */
    public class ListItem {
        private String restTime ;
        private String walkTime ;
        private String runTime;
        private String temp;
        private String heartB;
        private String vacc;

        public void setTime(String rest, String walk, String run) {
            restTime = rest;
            walkTime = walk;
            runTime = run;
        }
        public void setTemp(String temperature) {
            temp = temperature ;
        }
        public void setHB(String heartbeat){ heartB = heartbeat;}
        public void setVacc(String vaccination){ vacc = vaccination; }

        public String getRest() {
            return this.restTime ;
        }
        public String getWalk() {
            return this.walkTime ;
        }
        public String getRun() {
            return this.runTime ;
        }
        public String getTemp() {
            return this.temp ;
        }
        public String getHB() { return this.heartB; }
        public String getVacc(){ return this.vacc; }
    }

    public void addItem(String rest, String walk, String run, String temperature, String heartbeat, String vaccination) {

        item.setTime(rest, walk, run);
        item.setTemp(temperature);
        item.setHB(heartbeat);
        item.setVacc(vaccination);
    }

    /* Set Action Time TextView */
    public void setTimeText(int action, int time){
        int hour, minute;
        hour = time / 60;
        minute = time % 60;
        if(action == REST)
            tv_resttime.setText(hour + "시간 " + minute + "분");
        if(action == WALK)
            tv_walktime.setText(hour + "시간 " + minute + "분");
        if(action == RUN)
            tv_runtime.setText(hour + "시간 " + minute + "분");

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.layout_vacc){
            /* Vaccination Dialog */
            vaccinationList.show();
            vaccinationList.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    Intent intent = new Intent(ManagementActivity.this, ManagementActivity.class);
                    finish();
                    startActivity(intent);
                    overridePendingTransition(0,0);
                }
            });
        }
        if(v.getId() == R.id.imgTitle){
            /* Refresh */
            Intent intent = new Intent(ManagementActivity.this, ManagementActivity.class);
            finish();
            startActivity(intent);
            overridePendingTransition(0,0);
        }
    }

}