package vocabulary.android.com.myvocabulary;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;

public class DisplayActivity extends AppCompatActivity {

    private String vocabulary;
    private String translateApi = "https://dictionary.yandex.net/api/v1/dicservice.json/lookup";
    private String translateKey = "dict.1.1.20151207T112349Z.4c76df247f50835e.7db41e42ba45e9ed71b239ee17b72ef529673fb1";
    private AsyncHttpClient client;
    private RequestParams params;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        vocabulary = getIntent().getStringExtra("translate");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();

        client = new AsyncHttpClient(true, 80, 443);
        params = new RequestParams();
        params.put("key", translateKey);
        params.put("lang", "en-en");
        params.put("text", vocabulary);

        client.get(translateApi, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (progressDialog != null || progressDialog.isShowing())
                    progressDialog.dismiss();

                String content = "";
                JSONObject jsonObject = null;
                if (statusCode == 200) {
                    try {
                        content = new String(responseBody, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObject = new JSONObject(content);
                    } catch (JSONException e) {
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
