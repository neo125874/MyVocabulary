package vocabulary.android.com.myvocabulary;

import java.util.ArrayList;

/**
 * Created by tw4585 on 2015/12/21.
 */
public class Wordnik {
    public class Provider
    {
        private String name;
        public String getName() { return this.name; }
        public void setName(String name) { this.name = name; }

        private int id;
        public int getId() { return this.id; }
        public void setId(int id) { this.id = id; }
    }

    public class Example
    {
        private Provider provider;
        public Provider getProvider() { return this.provider; }
        public void setProvider(Provider provider) { this.provider = provider; }

        private int year;
        public int getYear() { return this.year; }
        public void setYear(int year) { this.year = year; }

        private int rating;
        public int getRating() { return this.rating; }
        public void setRating(int rating) { this.rating = rating; }

        private String url;
        public String getUrl() { return this.url; }
        public void setUrl(String url) { this.url = url; }

        private String word;
        public String getWord() { return this.word; }
        public void setWord(String word) { this.word = word; }

        private String text;
        public String getText() { return this.text; }
        public void setText(String text) { this.text = text; }

        private int documentId;
        public int getDocumentId() { return this.documentId; }
        public void setDocumentId(int documentId) { this.documentId = documentId; }

        private int exampleId;
        public int getExampleId() { return this.exampleId; }
        public void setExampleId(int exampleId) { this.exampleId = exampleId; }

        private String title;
        public String getTitle() { return this.title; }
        public void setTitle(String title) { this.title = title; }
    }

    public class RootObject
    {
        private ArrayList<Example> examples;
        public ArrayList<Example> getExamples() { return this.examples; }
        public void setExamples(ArrayList<Example> examples) { this.examples = examples; }
    }
}
