package vocabulary.android.com.myvocabulary;

/**
 * Created by wangchun-i on 15/12/19.
 */
public class EngCht {
    private String english;
    private String chinese;

    public EngCht(String english, String chinese){
        this.english = english;
        this.chinese = chinese;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getChinese() {
        return chinese;
    }

    public void setChinese(String chinese) {
        this.chinese = chinese;
    }
}
