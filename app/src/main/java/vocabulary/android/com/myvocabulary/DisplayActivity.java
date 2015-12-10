package vocabulary.android.com.myvocabulary;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class DisplayActivity extends AppCompatActivity {

    private String vocabulary;
    private String translateApi = "https://dictionary.yandex.net/api/v1/dicservice.json/lookup";
    private String translateKey = "dict.1.1.20151207T112349Z.4c76df247f50835e.7db41e42ba45e9ed71b239ee17b72ef529673fb1";
    private AsyncHttpClient client;
    private RequestParams params;
    private ProgressDialog progressDialog;
    private TextView txt_search, txt_tr, txt_syn, txt_mean, txt_ex;

    private void initialize(){
        txt_search = (TextView)findViewById(R.id.txt_search);
        txt_tr = (TextView)findViewById(R.id.txt_tr);
        txt_syn = (TextView)findViewById(R.id.txt_syn);
        txt_mean = (TextView)findViewById(R.id.txt_mean);
        txt_ex = (TextView)findViewById(R.id.txt_ex);
    }

    public class Syn
    {
        private String text;
        public String getText() { return this.text; }
        public void setText(String text) { this.text = text; }

        private String pos;
        public String getPos() { return this.pos; }
        public void setPos(String pos) { this.pos = pos; }
    }

    public class SynObject
    {
        private String text;
        public String getText() { return this.text; }
        public void setText(String text) { this.text = text; }

        private String pos;
        public String getPos() { return this.pos; }
        public void setPos(String pos) { this.pos = pos; }

        private ArrayList<Syn> syn;
        public ArrayList<Syn> getSyn() { return this.syn; }
        public void setSyn(ArrayList<Syn> syn) { this.syn = syn; }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        vocabulary = intent.getStringExtra("translate");
        progressDialog.show();
        params = new RequestParams();
        params.put("key", translateKey);
        params.put("lang", "en-en");
        params.put("text", vocabulary);
        doAsyncHttpClient();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        vocabulary = getIntent().getStringExtra("translate");

        initialize();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();

        client = new AsyncHttpClient(true, 80, 443);
        params = new RequestParams();
        params.put("key", translateKey);
        params.put("lang", "en-en");
        params.put("text", vocabulary);

        doAsyncHttpClient();
    }

    private void doAsyncHttpClient(){
        client.get(translateApi, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (progressDialog != null || progressDialog.isShowing())
                    progressDialog.dismiss();

                String content = "";
                JSONObject jsonObject = null;
                JSONArray jsonArray = null;
                if (statusCode == 200) {
                    try {
                        content = new String(responseBody, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObject = new JSONObject(content);
                        jsonArray = jsonObject.getJSONArray("def");
                        jsonObject = (JSONObject) jsonArray.get(0);
                        txt_search.setText(jsonObject.getString("text") + " [ " + jsonObject.getString("ts") + " ]" + ": " + jsonObject.getString("pos") + ". ");
                        jsonArray = jsonObject.getJSONArray("tr");

                        Gson gson = new Gson();
                        Type listType = new TypeToken<ArrayList<SynObject>>(){}.getType();
                        ArrayList<SynObject> synObjects = gson.fromJson(jsonArray.toString(), listType);
                        txt_tr.setText("");
                        for(int i=0; i<synObjects.size(); i++){
                            if(i > 0){
                                txt_tr.append("\n\n");
                            }
                            SynObject synObject = synObjects.get(i);
                            txt_tr.append(synObject.getText()
                                    + " (" + synObject.getPos() + ") ");

                            if(synObject.getSyn() != null){
                                txt_tr.append("\n");
                                ArrayList<Syn> synArrayList = synObject.getSyn();
                                for (int j=0; j<synArrayList.size(); j++){
                                    Syn syn = synArrayList.get(j);
                                    if(j > 0) txt_tr.append("\n");

                                    txt_tr.append("\t\t" + syn.getText() + " (" + syn.getPos() + ") ");
                                }
                            }
                        }

                        txt_syn.setVisibility(View.GONE);
                        txt_mean.setVisibility(View.GONE);
                        txt_ex.setVisibility(View.GONE);

                        /*Gson gson = new Gson();
                        Type listType = new TypeToken<ArrayList<Syn>>(){}.getType();
                        ArrayList<Syn> synArrayList = gson.fromJson(jsonArray.toString(), listType);
                        SynObject synObject = new SynObject();
                        synObject.setSyn(synArrayList);
                        txt_syn.setText("synonym: \n");
                        for(int i=0; i<synObject.getSyn().size(); i++){
                            Syn syn = synObject.getSyn().get(i);
                            if(i > 0)
                                txt_syn.append("\n");
                            txt_syn.append("\t" + syn.getText() + " (" + syn.getPos() + ". )");
                        }

                        txt_tr.setVisibility(View.GONE);
                        txt_mean.setText("meaning: \n");
                        txt_mean.append("\t" + jsonArray.getJSONObject(0).getString("text") + " (" + jsonArray.getJSONObject(1).getString("pos") + ". )");*/
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
