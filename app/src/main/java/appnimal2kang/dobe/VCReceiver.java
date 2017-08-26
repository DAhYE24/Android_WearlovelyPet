package appnimal2kang.dobe;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by User on 2016-11-21.
 */
public class VCReceiver extends BroadcastReceiver {

    /* DogName Init*/
    String vcNo;
    phpUpdate task_update;
    Context con;
    int count = 0, push=0;

    public void onReceive(Context context, Intent intent) {

        con = context;

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        vcNo = intent.getStringExtra("vcNo");
        String vcName = intent.getStringExtra("vcName");
        String content = "'" + vcName + "' 예방접종 날짜가 다가왔습니다.";

        Intent pushIntent = new Intent(context, ManagementActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, pushIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder noti = new NotificationCompat.Builder(context);
        noti.setSmallIcon(R.drawable.ic_push)
                .setTicker("예방접종 알림")
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentTitle("예방접종 알림")
                .setContentText(content)
                .setAutoCancel(true)
                .setContentIntent(pIntent);

        Notification notification = noti.build();
        notificationManager.notify(0, notification);

        updateDB();

    }

    public void updateDB(){
        task_update = new phpUpdate();
        task_update.execute("http://14.63.225.210/vaccination_update.php?bcheck="+ "N" +
                "&updatedate=" + "now()" + "&no=" + vcNo);
    }

    public void checkUpdate(int count){
        if(count== 0)
            updateDB();
        else
            task_update.cancel(true);
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
                count++;
                //Toast.makeText(con,"update 성공",Toast.LENGTH_LONG).show();
            }
            else{
                if(push % 5 == 0) {
                    Toast.makeText(con, "네트워크 연결이 불안정합니다.", Toast.LENGTH_LONG).show();
                }
                push++;
            }

            checkUpdate(count);
        }
    }
}
