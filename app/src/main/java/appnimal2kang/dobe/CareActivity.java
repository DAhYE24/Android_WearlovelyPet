package appnimal2kang.dobe;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

public class CareActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    /* Php Networking declare */
    phpDown task;
    phpUpdate task_update;
    String myJSON, updateCheck, nowCheck;
    int pos = -1;
    ListView listView;
    JSONArray vc = null;
    CustomAdapter adapter;

    /* CareInfo Dialog declare */
    CareInfoDialog careinfoDialog;

    /* Context */
    Activity activity;

    /* Network checking parameter declare */
    boolean isMobileConnect, isWifiConnect, isMobileAvailable, isWifiAvailable;

    /*TextFile*/
    TextFileManager fileM_properWalking = new TextFileManager("ProperWalking", (StartActivity)StartActivity.mContext);

    /*Push*/
    AlarmManager alarmmanager;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care);

        /* Network checking init */
        ConnectivityManager manager = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
        isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();

        /* Context */
        activity = this;

        /* Domain : php -> receive data */
        task = new phpDown();
        listView = (ListView)findViewById(R.id.listView_care);
        adapter = new CustomAdapter();
        listView.setAdapter(adapter);
        task.execute("http://14.63.225.210/care.php");
        listView.setOnItemClickListener(this);

        /* Title Image : Refresh Button */
        ImageView careTitle = (ImageView) findViewById(R.id.careTitle);
        careTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId()==R.id.careTitle){
                    Intent intent = new Intent(activity, CareActivity.class);
                    finish();
                    startActivity(intent);
                    overridePendingTransition(0,0);
                }
            }
        });

        alarmmanager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
    }

    /* Android life cycle : onPause*/
    @Override
    protected void onPause() {
        super.onPause();
        task.cancel(true); // Cancle AsyncTask
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
                changeCheck(pos, nowCheck);
            }else{
                Toast.makeText(activity,"네트워크 연결이 불안정합니다.",Toast.LENGTH_LONG).show();
            }
        }

    }

    /* Receive Data : php (serverDB) */
    public class phpDown extends AsyncTask<String, Integer, String> {
        ProgressDialog loading = new ProgressDialog(CareActivity.this);

        @Override
        protected void onPreExecute() {
            /* Network connecting check*/
            if(!(isMobileConnect && isMobileAvailable) && !(isWifiConnect && isWifiAvailable)) {
                Toast.makeText(CareActivity.this, "네트워크 연결이 필요합니다", Toast.LENGTH_LONG).show();
                /* If network is unconnected, finish this activity and then go to MainActivity */
                Intent intent = new Intent(CareActivity.this, MainActivity.class);
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
            myJSON = str;
            showList();

            /* Dismiss loading dialog */
            loading.dismiss();
        }
    }


    protected void showList(){
        try{
            JSONObject jsonObj = new JSONObject(myJSON);
            vc = jsonObj.getJSONArray("results");

            for(int i=0;i<vc.length();i++) {
                JSONObject jo = vc.getJSONObject(i);

                String no = jo.getString("no");
                String name = jo.getString("name");
                String explanation = jo.getString("explanation");
                String check = jo.getString("bcheck");

                adapter.addItem(no, name, explanation, check);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /* ListViewItem Init */
    public class ListViewItem {
        private String checkStr ;
        private String nameStr ;
        private String infoStr;
        private String noStr;

        public void setCheck(String check) {
            checkStr = check ;
        }
        public void setName(String name) {
            nameStr = name ;
        }
        public void setInfo(String explanation) { infoStr = explanation; }
        public void setNo(String no){noStr = no;}

        public String getCheck() {
            return this.checkStr ;
        }
        public String getName() {
            return this.nameStr ;
        }
        public String getInfo() { return this.infoStr; }
        public String getNo(){ return this.noStr;}
    }

    /* CustomListView setting */
    public class CustomAdapter extends BaseAdapter {

        private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>();

        public CustomAdapter(){};

        @Override
        public int getCount() {
            return listViewItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return listViewItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void addItem(String no, String name, String explanation, String check) {
            ListViewItem item = new ListViewItem();

            item.setNo(no);
            item.setName(name);
            item.setInfo(explanation);
            item.setCheck(check);

            listViewItemList.add(item);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Context context = parent.getContext();
            final String info, checkstate;

            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_care, parent, false);
            }

            TextView nameTextView = (TextView) convertView.findViewById(R.id.tvCareName);
            ImageView infoImage = (ImageView) convertView.findViewById(R.id.btInfo);
            LinearLayout carelist = (LinearLayout) convertView.findViewById(R.id.carelist);

            ListViewItem listViewItem = listViewItemList.get(position);
            checkstate = listViewItem.getCheck();

            /* Layout color setting */
            if(checkstate.equals("N")) {
                carelist.setBackgroundColor(getResources().getColor(R.color.gray));
            }
            else if (checkstate.equals("Y")){
                carelist.setBackgroundColor(getResources().getColor(R.color.lightPurple));
            }

            /* Care name, info */
            info = listViewItem.getInfo();
            nameTextView.setText(listViewItem.getName());

            /* CareInfo Dialog */
            infoImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    careinfoDialog = new CareInfoDialog(activity, info);
                    careinfoDialog.show();
                }
            });

            return convertView;
        }
    }

    /* Update data Process (check) */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        task_update = new phpUpdate();
        CustomAdapter adapter = (CustomAdapter)listView.getAdapter();
        ListViewItem listviewitem = (ListViewItem) adapter.getItem(position);
        String updateNo = listviewitem.getNo();
        nowCheck = listviewitem.getCheck();
        pos = position;

        if(nowCheck.equals("N"))
            updateCheck = "Y";
        else
            updateCheck="N";

        task_update.execute("http://14.63.225.210/care_update.php?check="+updateCheck+"&no="+updateNo);
    }

    /* Change Care check : if 'update data process(check)' success */
    public void changeCheck(int position, String nowCheck){
        CustomAdapter adapter = (CustomAdapter)listView.getAdapter();
        ListViewItem listviewitem = (ListViewItem) adapter.getItem(position);
        String no = listviewitem.getNo();
        if(nowCheck.equals("N")) {
            listviewitem.setCheck("Y");
            switch (no){
                case "1":
                    fatDog(no);
                    break;
                case "2":
                    oldDog(no);
                    break;
                case "3":
                    cleanliness(no);
                    break;
                case "4":
                    menstruation(no);
                    break;
            }
        }
        else{
            listviewitem.setCheck("N");
            switch (no){
                case "1":
                    rm_fatDog(no);
                    break;
                case "2":
                    rm_oldDog(no);
                    break;
                case "3":
                    rm_cleanliness(no);
                    break;
                case "4":
                    rm_menstruation(no);
                    break;
            }
        }

        listView.invalidateViews();
    }

    public void fatDog(String no){
        int morningPush = Integer.parseInt(no);
        int eveningPush = Integer.parseInt(no+no);
        int snackPush = Integer.parseInt(no+no+no);

        int walking = Integer.parseInt(fileM_properWalking.load());
        fileM_properWalking.save(Integer.toString(walking+10));

        mPush(morningPush);
        ePush(eveningPush);
        sPush(snackPush);
    }

    public void rm_fatDog(String no){
        int morningPush = Integer.parseInt(no);
        int eveningPush = Integer.parseInt(no+no);
        int snackPush = Integer.parseInt(no+no+no);

        int walking = Integer.parseInt(fileM_properWalking.load());
        fileM_properWalking.save(Integer.toString(walking-10));

        rm_Push(morningPush);
        rm_Push(eveningPush);
        rm_Push(snackPush);

        int rmFatPush = Integer.parseInt(no+0);
        Intent intent = new Intent(this, CareReceiver.class);
        intent.putExtra("careNo", Integer.toString(rmFatPush));
        PendingIntent pIntent = PendingIntent.getBroadcast(this, rmFatPush, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long sec = 1000;

        //정해진 시간에 울리도록 알람 등록
        alarmmanager.setExact(AlarmManager.RTC_WAKEUP, sec, pIntent);

    }

    public void mPush(int morningPush){
        calendar = Calendar.getInstance();
//        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
//                calendar.get(Calendar.DAY_OF_MONTH), 8, 0, 0);
        //시연용
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)+ 1, 0);


        Intent intent = new Intent(this, CareReceiver.class);
        intent.putExtra("careNo", Integer.toString(morningPush));
        PendingIntent pIntent = PendingIntent.getBroadcast(this, morningPush, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long aday = 24*60*60*1000;
        final long currentTime = System.currentTimeMillis();
        final long calendarTime = calendar.getTimeInMillis();
        long triggerTime = calendarTime;

        //현재 시간보다 이전 시간이라면 내일부터 알람이 울리도록 설정
        if (currentTime > calendarTime) {
            triggerTime += aday;
        }

        //정해진 시간에 울리도록 알람 등록
        alarmmanager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pIntent);
    }

    public void ePush(int eveningPush){
        calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), 19, 0, 0);

        Intent intent = new Intent(this, CareReceiver.class);
        intent.putExtra("careNo", Integer.toString(eveningPush));
        PendingIntent pIntent = PendingIntent.getBroadcast(this, eveningPush, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long aday = 24*60*60*1000;
        final long currentTime = System.currentTimeMillis();
        final long calendarTime = calendar.getTimeInMillis();
        long triggerTime = calendarTime;

        //현재 시간보다 이전 시간이라면 내일부터 알람이 울리도록 설정
        if (currentTime > calendarTime) {
            triggerTime += aday;
        }

        //정해진 시간에 울리도록 알람 등록
        alarmmanager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pIntent);
    }

    public void sPush(int snackPush){

        Intent intent = new Intent(this, CareReceiver.class);
        intent.putExtra("careNo", Integer.toString(snackPush));
        PendingIntent pIntent = PendingIntent.getBroadcast(this, snackPush, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long sec = 1000;

        //정해진 시간에 울리도록 알람 등록
        alarmmanager.setExact(AlarmManager.RTC_WAKEUP, sec, pIntent);
    }

    public void rm_Push(int push){
        Intent intent = new Intent(this, CareReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, push, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmmanager.cancel(pIntent);
        pIntent.cancel();
    }

    public void oldDog(String no){
        int walking = Integer.parseInt(fileM_properWalking.load());
        fileM_properWalking.save(Integer.toString(walking-10));
    }

    public void rm_oldDog(String no){
        int walking = Integer.parseInt(fileM_properWalking.load());
        fileM_properWalking.save(Integer.toString(walking+10));
    }

    public void cleanliness(String no){
        int cleanPush = Integer.parseInt(no);

        calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)+1, 17, 0, 0);

        Intent intent = new Intent(this, CareReceiver.class);
        intent.putExtra("careNo", Integer.toString(cleanPush));
        PendingIntent pIntent = PendingIntent.getBroadcast(this, cleanPush, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long aday = 24*60*60*1000;
        final long currentTime = System.currentTimeMillis();
        final long calendarTime = calendar.getTimeInMillis();
        long triggerTime = calendarTime;

        //현재 시간보다 이전 시간이라면 내일부터 알람이 울리도록 설정
        if (currentTime > calendarTime) {
            triggerTime += aday;
        }

        //정해진 시간에 울리도록 알람 등록
        alarmmanager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pIntent);
    }

    public void rm_cleanliness(String no){
        int cleanPush = Integer.parseInt(no);
        rm_Push(cleanPush);

        int rmCleanPush = Integer.parseInt(no+0);
        Intent intent = new Intent(this, CareReceiver.class);
        intent.putExtra("careNo", Integer.toString(rmCleanPush));
        PendingIntent pIntent = PendingIntent.getBroadcast(this, rmCleanPush, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long sec = 1000;

        //정해진 시간에 울리도록 알람 등록
        alarmmanager.setExact(AlarmManager.RTC_WAKEUP, sec, pIntent);
    }

    public void menstruation(String no){
        int menstPush = Integer.parseInt(no);

        Intent intent = new Intent(this, CareReceiver.class);
        intent.putExtra("careNo", Integer.toString(menstPush));
        PendingIntent pIntent = PendingIntent.getBroadcast(this, menstPush, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long sec = 1000;

        //정해진 시간에 울리도록 알람 등록
        alarmmanager.setExact(AlarmManager.RTC_WAKEUP, sec, pIntent);
    }

    public void rm_menstruation(String no){
        int menstPush = Integer.parseInt(no);
        rm_Push(menstPush);

        int rmMenstPush = Integer.parseInt(no+0);
        Intent intent = new Intent(this, CareReceiver.class);
        intent.putExtra("careNo", Integer.toString(rmMenstPush));
        PendingIntent pIntent = PendingIntent.getBroadcast(this, rmMenstPush, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long sec = 1000;

        //정해진 시간에 울리도록 알람 등록
        alarmmanager.setExact(AlarmManager.RTC_WAKEUP, sec, pIntent);
    }
}