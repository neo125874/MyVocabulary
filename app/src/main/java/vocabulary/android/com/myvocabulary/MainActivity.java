package vocabulary.android.com.myvocabulary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.loopj.android.http.AsyncHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    EditText txtEng, txtCht;
    //static final int READ_BLOCK_SIZE = 100;
    static HashMap<String, String> map = new HashMap<String, String>();
    private PendingIntent pendingIntent;

    private AsyncHttpClient client;
    //private RequestParams params;
    private ProgressDialog progressDialog;
    private String defaultDropBoxUrl = "https://www.dropbox.com/s/3bpoday33ypbzif/myVocabularyTextFile.txt?dl=0";
    private String APP_KEY = "";
    private String APP_SECRET = "";
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private boolean mLoggedIn;
    private final String VOCABULARY_DIR = "/MyVocabulary/";
    private Long mFileLen;
    private Context mContext;

    @Override
    protected void onResume() {
        super.onResume();

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                //shared preference
                //String accessToken = mDBApi.getSession().getOAuth2AccessToken();
                storeAuth(mDBApi.getSession());
                setLoggedIn(true);
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }

    private void storeAuth(AndroidAuthSession session) {
        // Store the OAuth 2 access token, if there is one.
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.commit();
            return;
        }
    }

    private void logOut() {
        // Remove credentials from the session
        mDBApi.getSession().unlink();

        // Clear our stored keys
        clearKeys();
        // Change UI state to display logged out version
        setLoggedIn(false);
    }

    private void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtEng=(EditText)findViewById(R.id.editText1);
        txtCht=(EditText)findViewById(R.id.editText2);

        APP_KEY = this.getString(R.string.app_key);
        APP_SECRET = this.getString(R.string.app_secret);
        //AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        //AndroidAuthSession session = new AndroidAuthSession(appKeys);
        AndroidAuthSession session = buildSession();
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        if(mDBApi.getSession().isLinked())
            setLoggedIn(mDBApi.getSession().isLinked());
        else
            mDBApi.getSession().startOAuth2Authentication(MainActivity.this);
        //checkAppKeySetup();

        this.mContext = this;
        /*progressDialog = new ProgressDialog(this);
        progressDialog.setMax(100);
        progressDialog.setMessage("Loading");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);*/
    }

    private void setLoggedIn(boolean loggedIn) {
        mLoggedIn = loggedIn;
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);

        loadAuth(session);

        return session;
    }

    private void loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

        session.setOAuth2AccessToken(secret);
    }

    //upload
    public void UploadBtn(View view){
        try {
            File file;
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "myVocabularyTextFile.txt");
            uploadApiTask uploadApiTask = new uploadApiTask(MainActivity.this, progressDialog, mDBApi, VOCABULARY_DIR, file);
            uploadApiTask.execute();
        }catch (Exception e){
            e.printStackTrace();
        }
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

    private class dropboxApiTask extends AsyncTask<Void, Long, Void>{

        @Override
        protected void onProgressUpdate(Long... progress) {
            int percent = (int)(100.0*(double)progress[0]/mFileLen + 0.5);
            progressDialog.setProgress(percent);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMax(100);
            progressDialog.setMessage("Loading");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //dropbox api
            try {
                // Get the metadata for a directory
                DropboxAPI.Entry dirent = mDBApi.metadata(VOCABULARY_DIR, 1000, null, true, null);
                ArrayList<DropboxAPI.Entry> thumbs = new ArrayList<DropboxAPI.Entry>();
                for (DropboxAPI.Entry ent: dirent.contents) {
                    // Add it to the list of thumbs we can choose from
                    thumbs.add(ent);
                }
                int index = 0;
                for(int i=0; i<thumbs.size(); i++){
                    if(thumbs.get(i).path.startsWith(VOCABULARY_DIR)){
                        index = i;
                    }
                }
                DropboxAPI.Entry ent = thumbs.get(index);
                String path = ent.path;
                mFileLen = ent.bytes;

                File dropboxFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "myVocabularyTextFile.txt");
                FileOutputStream outputStream = new FileOutputStream(dropboxFile);
                mDBApi.getFile(path, null, outputStream,
                        new ProgressListener() {
                            @Override
                            public void onProgress(long bytes, long total) {
                                publishProgress(bytes);
                            }

                            @Override
                            public long progressInterval()
                            {
                                // Update the progress bar every half-second or so
                                return 500;
                            }
                        });

                /*File dropboxFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "myVocabularyTextFile.txt");
                FileOutputStream outputStream = new FileOutputStream(dropboxFile);
                DropboxAPI.DropboxFileInfo info = mDBApi.getFile("/myVocabularyTextFile.txt", null, outputStream, null);
                Log.i("DbExampleLog", "The file's rev is: " + info.getMetadata().rev);*/
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
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
                    if (line!=null && line.contains("=")) {
                        String[] strings = line.split("=");
                        map.put(strings[0], strings[1]);
                    }

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

                Intent myIntent = new Intent(MainActivity.this, MyReceiver.class);
                myIntent.putExtra("map", map);
                pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                //alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
                Calendar calendar = Calendar.getInstance();
                calendar.add(calendar.MINUTE, 1);
                long howmany = calendar.getTimeInMillis() - System.currentTimeMillis();

                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, howmany, 1 * 60 * 1000, pendingIntent);

                //Toast.makeText(getBaseContext(), s,Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (progressDialog != null || progressDialog.isShowing())
                progressDialog.dismiss();
            Toast.makeText(getBaseContext(), "File renewed successfully!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Read text from file
    public void ReadBtn(View v) {
        new dropboxApiTask().execute();
    }
}
