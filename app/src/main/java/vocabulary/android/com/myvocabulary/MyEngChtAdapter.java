package vocabulary.android.com.myvocabulary;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by wangchun-i on 15/12/19.
 */
public class MyEngChtAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private ArrayList<EngCht> engChtArrayList;
    private ArrayList<EngCht> mStringFilterList;
    private ValueFilter valueFilter;

    public MyEngChtAdapter(Context context, ArrayList<EngCht> engChtArrayList){
        this.context = context;
        this.engChtArrayList = engChtArrayList;
        this.mStringFilterList = engChtArrayList;
    }

    @Override
    public int getCount() {
        return engChtArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return engChtArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return engChtArrayList.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        convertView = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);

            TextView tv_eng = (TextView) convertView.findViewById(R.id.tv_eng);
            TextView tv_cht = (TextView) convertView.findViewById(R.id.tv_cht);

            EngCht engCht = engChtArrayList.get(position);

            tv_eng.setText(engCht.getEnglish());
            tv_cht.setText(engCht.getChinese());
        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new ValueFilter();
        }
        return valueFilter;
    }

    private class ValueFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                ArrayList<EngCht> filterList = new ArrayList<EngCht>();
                for (int i = 0; i < mStringFilterList.size(); i++) {
                    if ( (mStringFilterList.get(i).getEnglish().toUpperCase() )
                            .contains(constraint.toString().toUpperCase())) {

                        EngCht engCht = new EngCht(
                                mStringFilterList.get(i).getEnglish(),
                                mStringFilterList.get(i).getChinese());

                        filterList.add(engCht);
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = mStringFilterList.size();
                results.values = mStringFilterList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            engChtArrayList = (ArrayList<EngCht>) results.values;
            notifyDataSetChanged();
        }
    }
}
