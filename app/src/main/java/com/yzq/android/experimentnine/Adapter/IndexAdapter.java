package com.yzq.android.experimentnine.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yzq.android.experimentnine.Compoment.Index;
import com.yzq.android.experimentnine.R;

import java.util.List;

/**
 * Created by YZQ on 2016/11/27.
 */

public class IndexAdapter extends BaseAdapter {
    private Context context;
    private List<Index> indexList;

    public IndexAdapter(Context context, List<Index> indexList) {
        this.context = context;
        this.indexList = indexList;
    }

    @Override
    public int getCount() {
        if (indexList == null) return 0;
        return indexList.size();
    }

    @Override
    public Object getItem(int i) {
        if (indexList == null) return 0;
        return indexList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View convertview;
        ViewHolder viewHolder;

        if (view == null) {
            convertview = LayoutInflater.from(context).inflate(R.layout.index_item, null);
            viewHolder = new ViewHolder();
            viewHolder.describe = (TextView)convertview.findViewById(R.id.index_describe);
            viewHolder.strength = (TextView)convertview.findViewById(R.id.index_strength);
            viewHolder.suggest = (TextView)convertview.findViewById(R.id.index_suggest);
            convertview.setTag(viewHolder);
        } else {
            convertview = view;
            viewHolder = (ViewHolder)convertview.getTag();
        }

        viewHolder.describe.setText(indexList.get(i).getName());
        viewHolder.strength.setText(indexList.get(i).getValue());
        viewHolder.suggest.setText(indexList.get(i).getDetail());

        return convertview;
    }

    private class ViewHolder {
        public TextView describe, strength, suggest;
    }
}
