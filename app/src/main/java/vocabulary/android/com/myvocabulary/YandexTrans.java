package vocabulary.android.com.myvocabulary;

import java.util.ArrayList;

/**
 * Created by tw4585 on 2015/12/22.
 */
public class YandexTrans {
    public class RootObject
    {
        private int code;
        public int getCode() { return this.code; }
        public void setCode(int code) { this.code = code; }

        private String lang;
        public String getLang() { return this.lang; }
        public void setLang(String lang) { this.lang = lang; }

        private ArrayList<String> text;
        public ArrayList<String> getText() { return this.text; }
        public void setText(ArrayList<String> text) { this.text = text; }
    }
}
