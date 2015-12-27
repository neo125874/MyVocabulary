package vocabulary.android.com.myvocabulary;

import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Created by tw4585 on 2015/12/25.
 */
public class TessOCR {
    private TessBaseAPI mTess;

    public TessOCR(String path) {
        mTess = new TessBaseAPI();
        //String datapath = Environment.getExternalStorageDirectory() + "/tesseract/";
        String language = "eng";
        //File dir = new File(datapath + "tessdata/");
        //if (!dir.exists())
        //    dir.mkdirs();
        //mTess.init(datapath, language);
        mTess.setDebug(true);
        mTess.init(path, language);
    }

    public String getOCRResult(Bitmap bitmap) {

        mTess.setImage(bitmap);
        String result = mTess.getUTF8Text();
        result = result.replaceAll("[^a-zA-Z0-9]+", " ");
        return result;
    }

    public void onDestroy() {
        if (mTess != null)
            mTess.end();
    }
}
