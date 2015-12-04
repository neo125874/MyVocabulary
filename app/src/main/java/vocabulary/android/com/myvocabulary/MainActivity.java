package vocabulary.android.com.myvocabulary;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    EditText txtEng, txtCht;
    //static final int READ_BLOCK_SIZE = 100;
    static HashMap<String, String> map = new HashMap<String, String>();
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtEng=(EditText)findViewById(R.id.editText1);
        txtCht=(EditText)findViewById(R.id.editText2);
    }

    // write text to file
    public void WriteBtn(View v) {
        File file;
        FileOutputStream outputStream;

        // add-write text into file
        try {
            String vocabulary = txtEng.getText().toString() + "=" + txtCht.getText().toString() + "\n";
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "myVocabularyTextFile.txt");
            outputStream = new FileOutputStream(file);
            outputStream.write(vocabulary.getBytes());
            outputStream.close();
            /*FileOutputStream fileout=openFileOutput("myVocabularyTextFile.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write(textmsg.getText().toString());
            outputWriter.close();*/

            //display file saved message
            Toast.makeText(getBaseContext(), "File saved successfully!",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int notification_id = 1;
    private void presentNotification(int visibility, int icon, String title, String text) {
        Notification notification = new NotificationCompat.Builder(this)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setVisibility(visibility).build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notification_id, notification);
    }

    // Read text from file
    public void ReadBtn(View v) {
        File file;
        FileInputStream inputStream;
        //reading text from file
        try {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "myVocabularyTextFile.txt");
            inputStream = new FileInputStream(file);
            InputStreamReader InputRead= new InputStreamReader(inputStream);

            BufferedReader buffreader = new BufferedReader(InputRead);

            String line;

            do {
                line = buffreader.readLine();
                // do something with the line
                if(line.contains("=")){
                    String[] strings = line.split("=");
                    map.put(strings[0], strings[1]);
                }

                Intent myIntent = new Intent(MainActivity.this, MyReceiver.class);
                myIntent.putExtra("map", map);
                pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
                //alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
                Calendar calendar= Calendar.getInstance();
                calendar.add(calendar.MINUTE, 1);
                long howmany = calendar.getTimeInMillis()-System.currentTimeMillis();

                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, howmany, 1 * 60 * 1000, pendingIntent);

            } while (line != null);
            //char[] inputBuffer= new char[READ_BLOCK_SIZE];
            //String s="";
            //int charRead;

            //while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
            //    String readstring=String.copyValueOf(inputBuffer,0,charRead);
            //    s +=readstring;
            //}
            //InputRead.close();

            inputStream.close();

            //Toast.makeText(getBaseContext(), s,Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
