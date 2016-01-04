package vocabulary.android.com.myvocabulary;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by wangchun-i on 16/1/4.
 */
public class MessageBox extends Service {

    private HashMap<String, String> map;
    private Random random;
    private List<String> keys;
    private TextView eng, cht;
    private Button detail;
    private WindowManager wm;
    private View view;

    private void initialize(View view){
        eng = (TextView)view.findViewById(R.id.eng_title);
        cht = (TextView)view.findViewById(R.id.cht_content);
        detail = (Button)view.findViewById(R.id.detail_btn);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int id = super.onStartCommand(intent, flags, startId);

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if(view == null)
            view = layoutInflater.inflate(R.layout.mycustom, null);
        else
            wm.removeView(view);

        initialize(view);
        map = (HashMap<String, String>)(intent.getSerializableExtra("map"));
        random = new Random();
        keys = new ArrayList<String>(map.keySet());

        String randomKey = keys.get(random.nextInt(keys.size()));
        String value = map.get(randomKey);

        //control click event
        final Intent myIntent = new Intent(this.getApplicationContext(), DisplayActivity.class);
        myIntent.putExtra("translate", randomKey);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(myIntent);
                //finish();
                stopSelf();
            }
        });
        eng.setText(randomKey);
        cht.setText(value);

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

        wm.addView(view, params);

        return id;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        wm.removeView(view);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
