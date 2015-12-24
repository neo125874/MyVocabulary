package vocabulary.android.com.myvocabulary;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by tw4585 on 2015/12/4.
 */
public class MyAlarmService extends Service {
    private NotificationManager mManager;

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @SuppressWarnings("static-access")
    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);

        mManager = (NotificationManager) this.getApplicationContext().getSystemService(this.getApplicationContext().NOTIFICATION_SERVICE);

        //control click event
        Intent myIntent = new Intent(this.getApplicationContext(), DisplayActivity.class);

        HashMap<String, String> x = (HashMap<String, String>)(intent.getSerializableExtra("map"));
        Random random = new Random();
        List<String> keys = new ArrayList<String>(x.keySet());
        String randomKey = keys.get(random.nextInt(keys.size()));
        String value = x.get(randomKey);

        myIntent.putExtra("translate", randomKey);

        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder notification = new Notification.Builder(this.getApplicationContext());
        notification.setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(randomKey + "：" + value)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(randomKey)
                .setContentText(value)
                .setContentIntent(pendingNotificationIntent)
                .setAutoCancel(false);
        //Notification notification = new Notification(R.mipmap.ic_launcher,
        //        randomKey + "：" + value,
        //        System.currentTimeMillis());
        myIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);


        //notification.flags |= Notification.FLAG_AUTO_CANCEL;
        //notification.setLatestEventInfo(this.getApplicationContext(), randomKey, value, pendingNotificationIntent);

        mManager.notify(0, notification.build());

        PowerManager pm = (PowerManager)this.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        if(isScreenOn==false)
        {
            PowerManager.WakeLock wl =
                    pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,
                            "MyLock");
            wl.acquire(5000);
            PowerManager.WakeLock wl_cpu =
                    pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                            "MyCpuLock");
            wl_cpu.acquire(5000);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
}
