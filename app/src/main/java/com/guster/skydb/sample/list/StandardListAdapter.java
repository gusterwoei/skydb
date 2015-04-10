package com.guster.skydb.sample.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gusterwoei on 4/10/14.
 */
public abstract class StandardListAdapter<T> extends ArrayAdapter<T> {
    private Context context;
    private int resId = 0;
    private List<T> data;
    private List<T> dataOri; // original full data list, used for filtering
    //private ListAdapterListener listener;

    public StandardListAdapter(Context context, int listItemResId, List data) {
        super(context, listItemResId, 0, data);
        this.context = context;
        this.resId = listItemResId;
        this.data = data;
        this.dataOri = new ArrayList<T>();
        for(T i : this.data) {
            dataOri.add(i);
        }
        //this.listener = listener;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public T getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(resId, parent, false);
        }

        getView(position, getItem(position), view, parent);

        return view;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    public abstract View getView(int i, T item, View view, ViewGroup parent);
    public abstract String getFilterCriteria(T item, CharSequence userInput);
    public abstract String getFilterResultText(T item, CharSequence userInput);
    public abstract Boolean getRegex(String compareValue, CharSequence userInput);

    /*public interface ListAdapterListener {
        View getView(int i, Object item, View view, ViewGroup parent);
        String getFilterCriteria(Object item);
    }*/

    @Override
    public Filter getFilter() {
        return new MyFilter();
    }

    private class MyFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence c) {
            FilterResults results = new FilterResults();
            List list = new ArrayList<T>();

            for(T t : dataOri) {
                String compareValue = getFilterCriteria(t, c);
                Boolean regex = getRegex(compareValue, c);
                compareValue = (compareValue==null)? "" : compareValue;
                if(regex == null)
                    regex = compareValue.toLowerCase().matches(".*" + c.toString().toLowerCase() + ".*");
                if(regex) {
                    list.add(getFilterResultText(t, c)!=null? getFilterResultText(t, c) : t);
                    //list.add(t);
                }
            }
            results.values = list;
            results.count = list.size();

            return results;
        }
        @Override
        protected void publishResults(CharSequence c, FilterResults filterResults) {
            if(filterResults.count > 0) {
                data = (List<T>)filterResults.values;

                // this will affect the original data source, use it with care
                /*clear();
                for(T i : data) {
                    add(i);
                }*/

                //notifyDataSetChanged();
            } else {
                data = new ArrayList<T>();
                //notifyDataSetInvalidated();
            }

            notifyDataSetChanged();
        }
    }
}
