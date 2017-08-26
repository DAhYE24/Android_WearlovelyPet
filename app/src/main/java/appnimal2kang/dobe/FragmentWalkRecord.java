package appnimal2kang.dobe;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by dh93 on 2016-10-18.
 */
public class FragmentWalkRecord extends SimpleFragment {
    /* 로그 테스트 */
    public static String TAG = "WLP_TEST";

    /* JAVA */
    FrameLayout parent;
    TextFileManager fileM_properWalking = new TextFileManager("ProperWalking", ((StartActivity)StartActivity.mContext)); // Registration : dog birthday

    /* 필수 */
    public static Fragment newInstance() {
        return new FragmentWalkRecord();
    }

    /* 데이터 통신 */
    private BarChart mChart;
    phpDown task;
    public static String myJSON;
    String [] recordDate = null;

    public static String getMyJSON() {
        return myJSON;
    }

    /* 네트워크 상태 체크 */
    boolean isMobileConnect, isWifiConnect, isMobileAvailable, isWifiAvailable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 해당 프레그먼트 연결
        View v = inflater.inflate(R.layout.walk_record_fragment, container, false);

        /* Network checking init */
        ConnectivityManager manager = (ConnectivityManager) getContext().getSystemService (Context.CONNECTIVITY_SERVICE);
        isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();

        /* Receive Data : php (serverDB) */
        task = new phpDown();
        task.execute("http://14.63.225.210/walkingrecord.php?");

        /* 초기화 */
        parent = (FrameLayout) v.findViewById(R.id.walkRecordLayout);

        return v;
    }

    /* Receive Data : php (serverDB) */
    public class phpDown extends AsyncTask<String, Integer, String> {
        ProgressDialog loading = new ProgressDialog(getContext());

        @Override
        protected void onPreExecute() {
            /* Network connecting check*/
            if(!(isMobileConnect && isMobileAvailable) && !(isWifiConnect && isWifiAvailable)) {
                Toast.makeText(getContext(), "네트워크 연결이 필요합니다", Toast.LENGTH_LONG).show();
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

            /* DATE 값 */
            try{
                JSONObject jsonObj = new JSONObject(myJSON);
                record = jsonObj.getJSONArray("results");

            /* 배열 초기화 */
                recordDate = new String[record.length()];

                for(int i=0;i<record.length();i++) {
                    JSONObject jo = record.getJSONObject(i);
                    //배열로 받아옴
                    recordDate[i] = jo.getString("date");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            drawBarChart(); // 차트 그리기
            loading.dismiss(); // Dismiss loading dialog
        }
    }

    private void drawBarChart() {
        /* 바차트 생성 및 선언 */
        mChart = new BarChart(getActivity());
        mChart.getDescription().setEnabled(false);
        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);

        /* 차트 범위 설정 : 적정 산책량의 2배 */
        long test = Long.parseLong(fileM_properWalking.load()) * 2;
        long range = 120;
        mChart.setData(generateBarData(1, range, 5)); // 네모 보여줄거 숫자, 범위, 차트 갯수

        /* 차트 채우기 */
        Legend l = mChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setDrawInside(false);

        /* 왼쪽 축 생성 */
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // 시작점은 0
        leftAxis.setMaxWidth(range); // 최고점은 적정산책량 * 2인 수

        /* 오른쪽 축은 없애기 */
        mChart.getAxisRight().setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setEnabled(true);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int i = (int) value;
                return recordDate[record.length() - 1 - i];
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });

        // 프레임레이아웃에 만든 차트를 적용
        parent.addView(mChart);
    }
}