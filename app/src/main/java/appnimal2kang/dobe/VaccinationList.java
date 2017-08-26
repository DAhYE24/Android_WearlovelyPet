package appnimal2kang.dobe;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
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

public class VaccinationList extends Dialog {

    String myJSON, update_check, nowCheck, updatedate;
    String age_select;
    int pos = -1;
    int datepos = -1;
    ListView listView;
    TextView tvVaccDate;
    JSONArray vc = null;
    CustomAdapter adapter;
    phpDown task;
    phpUpdate task_update;
    AlarmManager alarmmanager;

    /* Vaccination Date  */
    private DatePickerDialog datepicker;
    private Calendar calendar;
    int year, month, day;

    /* Network checking parameter declare */
    boolean isMobileConnect, isWifiConnect, isMobileAvailable, isWifiAvailable;

    TextFileManager managementfileM_iage = new TextFileManager("iAge", ((ManagementActivity)ManagementActivity.vContext));

    public VaccinationList(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_vaccination);

        /* Network checking init */
        ConnectivityManager manager = (ConnectivityManager) getContext().getSystemService (Context.CONNECTIVITY_SERVICE);
        isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();

        /* ListView Init */
        listView = (ListView)findViewById(R.id.listview_vc);
        adapter = new CustomAdapter();
        listView.setAdapter(adapter);

        age_select = managementfileM_iage.load();

        /* Receive Data : php (serverDB) */
        task = new phpDown();
        task.execute("http://14.63.225.210/vaccination.php?age="+age_select);

        /* Vaccination Date Init */
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        datepicker = new DatePickerDialog(context, listener, year, month, day);

        /* Button */
        Button vaOK = (Button)findViewById(R.id.btVaccOk);
        vaOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId()==R.id.btVaccOk)
                    dismiss();
            }
        });

        alarmmanager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


    }

    @Override
    protected void onStop() {
        task.cancel(true); // Cancle AsyncTask
        super.onStop();
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
            }
            else if(str.equals("T")){
                changeDate(datepos, updatedate);
            }
            else{
                Toast.makeText(getContext(),"update 실패",Toast.LENGTH_LONG).show();
            }
        }
    }

    /* Receive Data : php (serverDB) */
    public class phpDown extends AsyncTask<String, Integer, String> {
        ProgressDialog loading = new ProgressDialog(getContext());

        @Override
        protected void onPreExecute() {
            /* Network connecting check*/
            if(!(isMobileConnect && isMobileAvailable) && !(isWifiConnect && isWifiAvailable)) {
                Toast.makeText(getContext(), "네트워크 연결이 필요합니다", Toast.LENGTH_LONG).show();
                /* If network is unconnected, finish */
                dismiss();
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
                String check = jo.getString("bcheck");
                String updatedate = jo.getString("updatedate");
                String criterionage = jo.getString("criterionage");

                adapter.addItem(no, name, check, updatedate, criterionage);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /* ListViewItem Init */
    public class ListViewItem {
        private String checkStr ;
        private String nameStr ;
        private String vcNo;
        private String vcDate;
        private String vcPeriod;

        public void setCheck(String check) {
            checkStr = check ;
        }
        public void setName(String name) {
            nameStr = name ;
        }
        public void setVcNo(String no){ vcNo = no;}
        public void setVcDate(String date){ vcDate = date; }
        public void setVcPeriod(String period){ vcPeriod = period;}

        public String getCheck() {
            return this.checkStr ;
        }
        public String getName() {
            return this.nameStr ;
        }
        public String getVcNo() { return this.vcNo; }
        public String getVcDate(){ return this.vcDate; }
        public String getVcPeriod(){ return this.vcPeriod; }
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

        public void addItem(String no, String name, String check, String updatedate, String criterionage) {
            ListViewItem item = new ListViewItem();

            item.setVcNo(no);
            item.setName(name);
            item.setCheck(check);
            item.setVcDate(updatedate);
            item.setVcPeriod(criterionage);

            listViewItemList.add(item);
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Context context = parent.getContext();

            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_vaccination, parent, false);
            }

            ImageView checkImage = (ImageView) convertView.findViewById(R.id.bcheckBt);
            TextView nameTextView = (TextView) convertView.findViewById(R.id.tvVaccname);
            tvVaccDate = (TextView) convertView.findViewById(R.id.tvVaccDate);

            final ListViewItem listViewItem = listViewItemList.get(position);

            /* Set Vaccination Date */
            tvVaccDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    datepos = position;
                    datepickerDialog();
                }
            });

            /* checkBox Img setting */
            if(listViewItem.getCheck().equals("N")) {
                tvVaccDate.setClickable(false);
                checkImage.setImageDrawable(ContextCompat.getDrawable(VaccinationList.super.getContext(), R.drawable.uncheck_box));
            }
            else if(listViewItem.getCheck().equals("Y")){
                tvVaccDate.setClickable(true);
                checkImage.setImageDrawable(ContextCompat.getDrawable(VaccinationList.super.getContext(), R.drawable.checked_box));
            }

            /* Vaccination Name, Date */
            nameTextView.setText(listViewItem.getName());
            tvVaccDate.setText(listViewItem.getVcDate());

            if(listViewItem.getVcDate().equals("0000-00-00")) {
                tvVaccDate.setVisibility(View.GONE);
            }
            else if(!listViewItem.getVcDate().equals("0000-00-00")) {
                tvVaccDate.setVisibility(View.VISIBLE);
            }

            /* Update data (check) */
            checkImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pos = position;
                    onClickCheck(listViewItem.getVcNo(), listViewItem.getCheck(), listViewItem.getVcDate());
                }
            });

            return convertView;
        }
    }

    public void datepickerDialog(){
        datepicker.show();
    }

    /* Set Vaccination Date - DatapickerDialog listener*/
    private android.app.DatePickerDialog.OnDateSetListener listener = new android.app.DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int yearOf, int monthOfyear, int dayOfyear) {

            updatedate = String.format("%04d-%02d-%02d", yearOf, monthOfyear+1, dayOfyear);

            /* Update data Process (date) */
            task_update = new phpUpdate();
            CustomAdapter adapter = (CustomAdapter)listView.getAdapter();
            ListViewItem listviewitem = (ListViewItem) adapter.getItem(datepos);
            String update_no = listviewitem.getVcNo();

            task_update.execute("http://14.63.225.210/vaccination_update_date.php?updatedate="+ updatedate+ "&no=" + update_no);
        }
    };

    /* Change Vaccination Date : if 'update data process(date)' success */
    public void changeDate(int position, String update_date){
        CustomAdapter adapter = (CustomAdapter)listView.getAdapter();
        ListViewItem listviewitem = (ListViewItem) adapter.getItem(position);

        listviewitem.setVcDate(update_date);
        setPushNotification(listviewitem.getVcNo(), listviewitem.getName(), update_date, listviewitem.getVcPeriod());

        listView.invalidateViews();
    }
    /* Update data Process (check) */
    public void onClickCheck(String update_no, String now_Check, String update_date) {
        task_update = new phpUpdate();

        nowCheck = now_Check;

        if(nowCheck.equals("N")) {
            update_check = "Y";
            updatedate = "now()";
        }
        else {
            update_check = "N";
            updatedate = "'" + update_date + "'";
        }

        //Toast.makeText(getContext(),update_check + "/"+update_no,Toast.LENGTH_LONG).show();
        task_update.execute("http://14.63.225.210/vaccination_update.php?bcheck="+ update_check+
                "&updatedate=" + updatedate + "&no=" + update_no);


    }
    /* Change Vaccination check : if 'update data process(check)' success */
    public void changeCheck(int position, String nowCheck){
        CustomAdapter adapter = (CustomAdapter)listView.getAdapter();
        ListViewItem listviewitem = (ListViewItem) adapter.getItem(position);

        calendar = Calendar.getInstance();
        String updateDate;
        updateDate = String.format("%04d-%02d-%02d",
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));

        if(nowCheck.equals("N")) {
            listviewitem.setCheck("Y");
            listviewitem.setVcDate(updateDate);
            setPushNotification(listviewitem.getVcNo(), listviewitem.getName(), updateDate, listviewitem.getVcPeriod());
            tvVaccDate.setClickable(true);
            tvVaccDate.setVisibility(View.VISIBLE);
        }
        else{
            listviewitem.setCheck("N");
            tvVaccDate.setClickable(false);
        }

        listView.invalidateViews();
    }

    public void setPushNotification(String no, String name, String date, String criterionage){
        int dYear, dMonth, dDay;
        int vcNo = Integer.parseInt(no);
        int notiDate = Integer.parseInt(criterionage);

        dYear = Integer.parseInt(date.substring(0, 4));
        dMonth = Integer.parseInt(date.substring(5, 7));
        dDay = Integer.parseInt(date.substring(8, 10));

        calendar = Calendar.getInstance();

//        calendar.set(dYear, dMonth - 1 + notiDate, dDay, 9, 0 , 0);
        calendar.set(dYear, dMonth - 1 , dDay, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)+ notiDate , 0);
//        String ddate = String.format("%04d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
//        String time = String.format("%02d:%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
//        Toast.makeText(getContext(),ddate  + " " + time,Toast.LENGTH_LONG).show();

        Intent intent = new Intent(getContext(), VCReceiver.class);
        intent.putExtra("vcNo", Integer.toString(vcNo));
        intent.putExtra("vcName", name);
        PendingIntent pIntent = PendingIntent.getBroadcast(getContext(), vcNo, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final long calendarTime = calendar.getTimeInMillis();
        long triggerTime = calendarTime;

        //정해진 시간에 울리도록 알람 등록
        alarmmanager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pIntent);
    }
}
