package vocabulary.android.com.myvocabulary;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    EditText txtEng, txtCht;
    //static final int READ_BLOCK_SIZE = 100;
    static HashMap<String, String> map = new HashMap<String, String>();
    private PendingIntent pendingIntent;

    private AsyncHttpClient client;
    //private RequestParams params;
    private ProgressDialog progressDialog;
    private String defaultDropBoxUrl = "https://www.dropbox.com/s/3bpoday33ypbzif/myVocabularyTextFile.txt?dl=0";
    final private String APP_KEY = this.getString(R.string.app_key);
    final private String APP_SECRET = this.getString(R.string.app_secret);
    private DropboxAPI<AndroidAuthSession> mDBApi;

    @Override
    protected void onResume() {
        super.onResume();

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                //shared preference
                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtEng=(EditText)findViewById(R.id.editText1);
        txtCht=(EditText)findViewById(R.id.editText2);

        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        mDBApi.getSession().startOAuth2Authentication(MainActivity.this);
    }

    // write text to file
    public void WriteBtn(View v) {
        File file;
        FileOutputStream outputStream;
        OutputStreamWriter outputStreamWriter;

        // add-write text into file
        try {
            String vocabulary = txtEng.getText().toString() + "=" + txtCht.getText().toString();
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "myVocabularyTextFile.txt");
            outputStream = new FileOutputStream(file, true);
            outputStream.write(vocabulary.getBytes());

            outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.append("\r\n");
            outputStreamWriter.flush();
            outputStreamWriter.close();

            outputStream.flush();
            outputStream.close();
            /*FileOutputStream fileout=openFileOutput("myVocabularyTextFile.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write(textmsg.getText().toString());
            outputWriter.close();*/

            //display file saved message
            Toast.makeText(getBaseContext(), "File saved successfully!",
                    Toast.LENGTH_SHORT).show();

            txtCht.setText("");
            txtEng.setText("");
            txtEng.requestFocus();

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

    private boolean RenewFromDropbox(byte[] responseBody){
        File file;
        FileOutputStream outputStream;
        OutputStreamWriter outputStreamWriter;

        try {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "myVocabularyTextFile.txt");
            outputStream = new FileOutputStream(file, true);
            outputStream.write(responseBody);

            outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.append("\r\n");
            outputStreamWriter.flush();
            outputStreamWriter.close();

            outputStream.flush();
            outputStream.close();

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    // Read text from file
    public void ReadBtn(View v) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();
        client = new AsyncHttpClient(true, 80, 443);
        client.get(defaultDropBoxUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (progressDialog != null || progressDialog.isShowing())
                    progressDialog.dismiss();

                String content = new String(responseBody);
                if(RenewFromDropbox(responseBody)) {
                    Toast.makeText(getBaseContext(), "File renewed successfully!",
                            Toast.LENGTH_SHORT).show();

                    File file;
                    FileInputStream inputStream;
                    //reading text from file
                    try {
                        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "myVocabularyTextFile.txt");
                        inputStream = new FileInputStream(file);
                        InputStreamReader InputRead = new InputStreamReader(inputStream);

                        BufferedReader buffreader = new BufferedReader(InputRead);

                        String line;

                        do {
                            line = buffreader.readLine();
                            // do something with the line
                            if (line.contains("=")) {
                                String[] strings = line.split("=");
                                map.put(strings[0], strings[1]);
                            }

                            Intent myIntent = new Intent(MainActivity.this, MyReceiver.class);
                            myIntent.putExtra("map", map);
                            pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                            //alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
                            Calendar calendar = Calendar.getInstance();
                            calendar.add(calendar.MINUTE, 1);
                            long howmany = calendar.getTimeInMillis() - System.currentTimeMillis();

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

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (progressDialog != null || progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        });
    }
}
