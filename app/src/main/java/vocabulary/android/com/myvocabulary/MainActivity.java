package vocabulary.android.com.myvocabulary;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.loopj.android.http.AsyncHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    //swipe refresh
    private SwipeRefreshLayout mSwipeRefreshLayout;

    //fab
    private FloatingActionsMenu floatingActionsMenu;
    private FloatingActionButton fabCamera, fabGallery;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_PICK_PHOTO = 2;
    private ProgressDialog mProgressDialog;
    private String mCurrentPhotoPath;
    private TessOCR mTessOCR;

    //init ocr
    private static String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/MyVocabularyOCR/";
    private static String TAG = "MainActivity.java";

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

        Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            Uri uri = (Uri) intent
                    .getParcelableExtra(Intent.EXTRA_STREAM);
            uriOCR(uri);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mTessOCR.onDestroy();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO
                && resultCode == Activity.RESULT_OK) {
            setPic();
        }
        else if (requestCode == REQUEST_PICK_PHOTO
                && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                uriOCR(uri);
            }
        }
    }

    private void uriOCR(Uri uri) {
        if (uri != null) {
            InputStream is = null;
            try {
                is = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                //mImage.setImageBitmap(bitmap);
                doOCR(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void setPic() {
        // Get the dimensions of the View
        //int targetW = mImage.getWidth();
        //int targetH = mImage.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        //bmOptions.inJustDecodeBounds = true;
        //bmOptions.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        try {
            ExifInterface exif = new ExifInterface(mCurrentPhotoPath);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Log.v(TAG, "Orient: " + exifOrientation);

            int rotate = 0;

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            Log.v(TAG, "Rotation: " + rotate);

            if (rotate != 0) {

                // Getting width & height of the given image.
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();

                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);

                // Rotating Bitmap
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
            }

            // Convert to ARGB_8888, required by tess
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        } catch (IOException e) {
            Log.e(TAG, "Couldn't correct orientation: " + e.toString());
        }

        //int photoW = bmOptions.outWidth;
        //int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        //int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        //bmOptions.inJustDecodeBounds = false;
        //bmOptions.inSampleSize = scaleFactor << 1;
        //bmOptions.inPurgeable = true;

        //Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        //mImage.setImageBitmap(bitmap);

        Log.v(TAG, "Before baseApi");
        doOCR(bitmap);

    }

    private void initialize(){
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_view);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFromDropbox();
                //mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        //resource not color
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);

        floatingActionsMenu = (FloatingActionsMenu)findViewById(R.id.fam);
        fabCamera = (FloatingActionButton)findViewById(R.id.fab_camera);
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        fabGallery = (FloatingActionButton)findViewById(R.id.fab_gallery);
        fabGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPhoto();
            }
        });

        //mTessOCR = new TessOCR(DATA_PATH);
    }

    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_PHOTO);
    }

    private void takePhoto() {
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        /*String storageDir = Environment.getExternalStorageDirectory()
                + "/TessOCR";*/
        String storageDir = DATA_PATH + "tessdata/";
        File dir = new File(storageDir);
        if (!dir.exists())
            dir.mkdir();

        File image = new File(storageDir + "/" + imageFileName + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void doOCR(final Bitmap bitmap) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "Processing",
                    "Doing OCR...", true);
        }
        else {
            mProgressDialog.show();
        }

        new Thread(new Runnable() {
            public void run() {

                final String result = mTessOCR.getOCRResult(bitmap);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (result != null && !result.equals("")) {
                            txtEng.setText(result);
                        }

                        mProgressDialog.dismiss();
                    }

                });

            };
        }).start();
    }

    private class DownloadFilesTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getBaseContext(), "File renewed successfully!",
                    Toast.LENGTH_SHORT).show();
            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
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
                mDBApi.getFile(path, null, outputStream, null);
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }

    private void refreshFromDropbox(){
        new DownloadFilesTask().execute();
    }

    private void initializeOCR(){
        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };
        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }
        }

        if (!(new File(DATA_PATH + "tessdata/eng.traineddata")).exists()) {
            try {

                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("eng.traineddata");
                OutputStream out = new FileOutputStream(DATA_PATH + "tessdata/eng.traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();

                Log.v(TAG, "Copied eng traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy eng traineddata " + e.toString());
            }
        }

        mTessOCR = new TessOCR(DATA_PATH);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        initializeOCR();

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
        //new dropboxApiTask().execute();
        Intent myIntent = new Intent(MainActivity.this, MyReceiver.class);
        myIntent.putExtra("map", GetLocalFileMap());
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.add(calendar.MINUTE, 1);
        long howmany = calendar.getTimeInMillis() - System.currentTimeMillis();

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, howmany, 1 * 60 * 1000, pendingIntent);
    }

    public void StopService(){
        Intent myIntent = new Intent(MainActivity.this, MyReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }

    private HashMap<String, String> GetLocalFileMap(){
        HashMap<String, String> hashMap = new HashMap<String, String>();
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
                    hashMap.put(strings[0], strings[1]);
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return hashMap;
    }

    public void SearchBtn(View v){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, SearchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("hashMap", GetLocalFileMap());
        startActivity(intent);
    }
}
