package appnimal2kang.dobe;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import java.util.Calendar;

/**
 * Created by User on 2016-11-24.
 */
public class CareReceiver extends BroadcastReceiver {

    String careNo;
    Context con;
    AlarmManager alarmManager;

    public void onReceive(Context context, Intent intent) {

        con = context;

        careNo = intent.getStringExtra("careNo");

        int care = Integer.parseInt(careNo);

        switch (care){
            case 1:
                mPush();
                break;
            case 11:
                ePush();
                break;
            case 111:
                sPush();
                setRepeat(care);
                break;
            case 10:
                rm_fatPush();
                break;
            case 3:
                celanPush();
                setRepeat(care);
                break;
            case 30:
                rm_cleanPush();
                break;
            case 4:
                menstPush();
                break;
            case 40:
                rm_menstPush();
                break;
        }
    }

    public void mPush(){
        NotificationManager notificationManager = (NotificationManager)con.getSystemService(Context.NOTIFICATION_SERVICE);



        Intent pushIntent = new Intent(con, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(con, 0, pushIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder noti = new NotificationCompat.Builder(con);
        noti.setSmallIcon(R.drawable.ic_push)
                .setTicker("아침 사료 적정량 주기")
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentTitle("아침 사료 적정량 주기")
                .setContentText("비만견에게는 적정량의 사료를 주어야 합니다.")
                .setAutoCancel(true)
                .setContentIntent(pIntent);

        Notification notification = noti.build();
        notificationManager.notify(1, notification);
    }

    public void ePush(){
        NotificationManager notificationManager = (NotificationManager)con.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent pushIntent = new Intent(con, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(con, 0, pushIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder noti = new NotificationCompat.Builder(con);
        noti.setSmallIcon(R.drawable.ic_push)
                .setTicker("저녁 사료 적정량 주기")
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentTitle("저녁 사료 적정량 주기")
                .setContentText("비만견에게는 적정량의 사료를 주어야 합니다.")
                .setAutoCancel(true)
                .setContentIntent(pIntent);

        Notification notification = noti.build();
        notificationManager.notify(11, notification);
    }

    public void sPush(){
        NotificationManager notificationManager = (NotificationManager)con.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent pushIntent = new Intent(con, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(con, 0, pushIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder noti = new NotificationCompat.Builder(con);
        noti.setSmallIcon(R.drawable.ic_push)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentTitle("간식 양 줄이기")
                .setContentText("건강한 반려견을 위해 간식 양을 줄여야 합니다.")
                .setContentIntent(pIntent);

        Notification notification = noti.build();
        notification.flags = Notification.FLAG_NO_CLEAR;
        notificationManager.notify(111, notification);
    }

    public void rm_fatPush(){
        NotificationManager notificationManager = (NotificationManager)con.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        notificationManager.cancel(11);
        notificationManager.cancel(111);
    }

    public void celanPush(){
        NotificationManager notificationManager = (NotificationManager)con.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent pushIntent = new Intent(con, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(con, 0, pushIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder noti = new NotificationCompat.Builder(con);
        noti.setSmallIcon(R.drawable.ic_push)
                .setTicker("위생 관리 알림")
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentTitle("위생 관리 알림")
                .setContentText("항문낭 관리, 귀청소, 털관리는 한달에 한번씩 해줘야 합니다.")
                .setAutoCancel(true)
                .setContentIntent(pIntent);

        Notification notification = noti.build();
        notificationManager.notify(3, notification);
    }

    public void rm_cleanPush(){
        NotificationManager notificationManager = (NotificationManager)con.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(3);
    }

    public void menstPush(){
        NotificationManager notificationManager = (NotificationManager)con.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent pushIntent = new Intent(con, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(con, 0, pushIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder noti = new NotificationCompat.Builder(con);
        noti.setSmallIcon(R.drawable.ic_push)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentTitle("반려견 생리 중 관리")
                .setContentText("생리 중 미용, 목욕은 자제하되 산책은 필수입니다. 애견 기저귀를 상시 준비해주세요.")
                .setContentIntent(pIntent);


        Notification notification = noti.build();
        notification.flags = Notification.FLAG_NO_CLEAR;
        notificationManager.notify(4, notification);
    }

    public void rm_menstPush(){
        NotificationManager notificationManager = (NotificationManager)con.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(4);
    }

    private void setRepeat(int careNo){

        alarmManager = (AlarmManager) con.getSystemService(Context.ALARM_SERVICE);

        Calendar cal = Calendar.getInstance();

        long day = 24*60*60*1000;
        final long calendarTime = cal.getTimeInMillis();
        long triggerTime = calendarTime + day;

        Intent intentR = new Intent(con, CareReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(con, careNo, intentR, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pIntent);

    }

}
