package vocabulary.android.com.myvocabulary;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

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
        Intent myIntent = new Intent(this.getApplicationContext(), MainActivity.class);

        HashMap<String, String> x = (HashMap<String, String>)(intent.getSerializableExtra("map"));
        Random random = new Random();
        List<String> keys = new ArrayList<String>(x.keySet());
        String randomKey = keys.get(random.nextInt(keys.size()));
        String value = x.get(randomKey);

        Notification notification = new Notification(R.mipmap.ic_launcher,
                randomKey + "：" + value,
                System.currentTimeMillis());
        myIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.setLatestEventInfo(this.getApplicationContext(), randomKey, value, pendingNotificationIntent);

        mManager.notify(0, notification);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
}
