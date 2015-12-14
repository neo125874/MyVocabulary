package vocabulary.android.com.myvocabulary;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by tw4585 on 2015/12/14.
 */
public class uploadApiTask extends AsyncTask<Void, Long, Boolean> {

    private ProgressDialog progressDialog;
    private DropboxAPI<?> mApi;
    private String mDir;
    private long mFileLen;
    private File mFile;
    private DropboxAPI.UploadRequest mRequest;
    private Context mContext;

    public uploadApiTask(Context context, ProgressDialog progressDialog, DropboxAPI<?> api, String dropboxDir, File file){
        this.progressDialog = progressDialog;
        this.mApi = api;
        this.mDir = dropboxDir;
        this.mFileLen = file.length();
        this.mFile = file;
        this.mContext = context;
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
    protected void onProgressUpdate(Long... progress) {
        int percent = (int)(100.0*(double)progress[0]/mFileLen + 0.5);
        progressDialog.setProgress(percent);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (progressDialog != null || progressDialog.isShowing())
            progressDialog.dismiss();
        Toast.makeText(mContext, "File uploaded successfully!",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            FileInputStream fis = new FileInputStream(mFile);
            String path = mDir + mFile.getName();
            mRequest = mApi.putFileOverwriteRequest(path, fis, mFileLen, new ProgressListener() {
                @Override
                public void onProgress(long bytes, long total) {
                    publishProgress(bytes);
                }

                @Override
                public long progressInterval() {
                    // Update the progress bar every half-second or so
                    return 500;
                }
            });
            if (mRequest != null) {
                mRequest.upload();
                return true;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
