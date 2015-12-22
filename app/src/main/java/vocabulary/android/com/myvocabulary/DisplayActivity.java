package vocabulary.android.com.myvocabulary;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class DisplayActivity extends AppCompatActivity {

    private String vocabulary;
    private String translateApi = "";
    private String translateKey = "";
    private AsyncHttpClient client;
    private RequestParams params;
    private ProgressDialog progressDialog;
    private TextView txt_search, txt_syn, txt_mean, txt_ex;
    private ExpandableTextView txt_tr;

    //Wordnik API key
    private String mWordnikAPIkey="";
    private String mWordnikUrl = "http://api.wordnik.com:80/v4/word.json/";

    //Yandex Translate
    private String mYandexUrl = "";
    private String mYandexKey = "";


    //open dictionary api
    //private OpenDictionaryAPI api;

    //TTS object
    private TextToSpeech myTTS;
    private Button mySpeak;
    //status check code
    private int MY_DATA_CHECK_CODE = 0;
    private Context myContext;
    private String speakWord;

    private void initialize(){
        txt_search = (TextView)findViewById(R.id.txt_search);
        txt_tr = (ExpandableTextView)findViewById(R.id.ex_txt_tr);
        txt_syn = (TextView)findViewById(R.id.txt_syn);
        txt_mean = (TextView)findViewById(R.id.txt_mean);
        txt_ex = (TextView)findViewById(R.id.txt_ex);

        this.myContext = this;
        //check for TTS data
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        translateApi = getString(R.string.translateApi);
        translateKey = getString(R.string.translateKey);
        mWordnikAPIkey = getString(R.string.WordnikAPIkey);

        mYandexUrl = getString(R.string.YandexTransUrl);
        mYandexKey = getString(R.string.YandexTransApiKey);

        mySpeak = (Button)findViewById(R.id.speak_btn);
        mySpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String words = txt_search.getText().toString();
                speakWords(speakWord);
            }
        });
    }

    //act on result of TTS data check
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                //the user has the necessary data - create the TTS
                myTTS = new TextToSpeech(myContext, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        //check for successful instantiation
                        if (status == TextToSpeech.SUCCESS) {
                            //us solution
                            int result = myTTS.setLanguage(Locale.US);
                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                                Log.e("TTS", "This Language is not supported");
                                Intent installIntent = new Intent();
                                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                                startActivity(installIntent);
                            }else {
                                mySpeak.setEnabled(true);
                            }
                            //Locale[] locales = Locale.getAvailableLocales();
                            //if(myTTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                            //    myTTS.setLanguage(Locale.US);
                        }
                        else if (status == TextToSpeech.ERROR) {
                            Toast.makeText(myContext, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            else {
                //no data - install it now
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    private void speakWords(String speech) {
        //speak straight away
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
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

    private  void doAsyncYandexTrans(){
        params = new RequestParams();
        params.put("key", mYandexKey);
        params.put("text", speakWord);
        params.put("lang", "en-zh");
        params.put("format", "plain");
        client.get(mYandexUrl, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (progressDialog != null || progressDialog.isShowing())
                    progressDialog.dismiss();

                String content = "";
                if (statusCode == 200) {
                    try {
                        content = new String(responseBody, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    txt_search.append("\n");
                    try{
                        Gson gson = new Gson();
                        YandexTrans.RootObject rootObject = gson.fromJson(content, YandexTrans.RootObject.class);
                        for(int i=0; i<rootObject.getText().size(); i++){
                            txt_search.append(rootObject.getText().get(i));
                            if(i > 0) txt_search.append("；");
                        }
                    }catch (Exception e){
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

    private void doAsyncWordnik(){
        params = new RequestParams();
        params.put("api_key", mWordnikAPIkey);
        params.put("includeDuplicates", false);
        params.put("useCanonical", false);
        params.put("skip", 0);
        params.put("limit", 3);
        client.get(mWordnikUrl + speakWord + "/examples", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (progressDialog != null || progressDialog.isShowing())
                    progressDialog.dismiss();

                String content = "";
                if (statusCode == 200) {
                    try {
                        content = new String(responseBody, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    txt_ex.setText("");
                    try {
                        Gson gson = new Gson();
                        Wordnik.RootObject rootObject = gson.fromJson(content, Wordnik.RootObject.class);

                        int j=0;
                        for(int i=1; i<=rootObject.getExamples().size(); i++){
                            if(i>1) txt_ex.append("\n");

                            txt_ex.append("Ex" + i + "：");
                            txt_ex.append(rootObject.getExamples().get(j).getText() + "\n");
                            j++;
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        progressDialog.show();
                        doAsyncYandexTrans();
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
                        speakWord = jsonObject.getString("text");
                        txt_search.setText(jsonObject.getString("text") + " [ " + jsonObject.getString("ts") + " ]" + ": " + jsonObject.getString("pos") + ". ");
                        jsonArray = jsonObject.getJSONArray("tr");

                        Gson gson = new Gson();
                        Type listType = new TypeToken<ArrayList<SynObject>>(){}.getType();
                        ArrayList<SynObject> synObjects = gson.fromJson(jsonArray.toString(), listType);
                        txt_tr.setText("");
                        String tr_content = "";
                        for(int i=0; i<synObjects.size(); i++){
                            if(i > 0){
                                //txt_tr.append("\n\n");
                                tr_content += "\n\n";
                            }
                            SynObject synObject = synObjects.get(i);
                            //txt_tr.append(synObject.getText()
                            //        + " (" + synObject.getPos() + ") ");
                            tr_content += synObject.getText() + " (" + synObject.getPos() + ") ";

                            if(synObject.getSyn() != null){
                                //txt_tr.append("\n");
                                tr_content += "\n";
                                ArrayList<Syn> synArrayList = synObject.getSyn();
                                for (int j=0; j<synArrayList.size(); j++){
                                    Syn syn = synArrayList.get(j);
                                    if(j > 0) //txt_tr.append("\n");
                                        tr_content += "\n";

                                    //txt_tr.append("\t\t" + syn.getText() + " (" + syn.getPos() + ") ");
                                    tr_content += "\t\t" + syn.getText() + " (" + syn.getPos() + ") ";
                                }
                            }
                        }
                        txt_tr.setText(tr_content);

                        txt_syn.setVisibility(View.GONE);
                        txt_mean.setVisibility(View.GONE);
                        //txt_ex.setVisibility(View.GONE);
                        progressDialog.show();
                        doAsyncWordnik();

                        /*Dictionary dict = null;
                        api = new OpenDictionaryAPI(getApplicationContext());
                        if(api.hasDictionary(new Direction(Language.English, Language.Chinese))){
                            HashSet<Dictionary> dictionaries = api.getDictionaries(new Direction(Language.English, Language.Chinese));
                            if (!dictionaries.isEmpty()) {
                                dict = dictionaries.iterator().next();
                            }
                        }*/
                        /*dict.getTranslationAsText(speakWord, TranslateMode.FULL, TranslateFormat.PLAIN, new Dictionary.TranslateAsTextListener() {
                            @Override
                            public void onComplete(String s, TranslateMode translateMode) {

                            }

                            @Override
                            public void onWordNotFound(ArrayList<String> arrayList) {

                            }

                            @Override
                            public void onError(com.paragon.open.dictionary.api.Error error) {

                            }

                            @Override
                            public void onIPCError(String s) {

                            }
                        });*/

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
