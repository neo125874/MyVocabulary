package vocabulary.android.com.myvocabulary;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private ListView listView;
    private String[] txt_english , txt_chinese;

    private ArrayList<EngCht> engChtArrayList ;
    private MyEngChtAdapter adapter;

    private HashMap<String, String> hashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchView = (SearchView)findViewById(R.id.searchView);
        listView = (ListView)findViewById(R.id.listView);

        hashMap = (HashMap<String, String>)(getIntent().getSerializableExtra("hashMap"));
        txt_english = new String[hashMap.size()];
        txt_chinese = new String[hashMap.size()];
        int index = 0;
        for(Map.Entry<String, String> mapEntry: hashMap.entrySet()){
            txt_english[index] = mapEntry.getKey();
            txt_chinese[index] = mapEntry.getValue();
            index++;
        }

        engChtArrayList = new ArrayList<EngCht>();
        for (int i = 0; i < txt_english.length; i++) {
            EngCht engCht = new EngCht(txt_english[i] , txt_chinese[i]);
            engChtArrayList.add(engCht);
        }

        adapter = new MyEngChtAdapter(getApplicationContext(), engChtArrayList);
        listView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

    }
}
