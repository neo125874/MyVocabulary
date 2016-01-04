package vocabulary.android.com.myvocabulary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by tw4585 on 2015/12/4.
 */
public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent myService = new Intent(context, MessageBox.class);
        myService.putExtra("map", intent.getSerializableExtra("map"));
        context.startService(myService);
        //Intent scheduledIntent = new Intent(context, MessageBox.class);
        //scheduledIntent.putExtra("map", intent.getSerializableExtra("map"));
        //scheduledIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //scheduledIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //context.startActivity(scheduledIntent);
    }
}
