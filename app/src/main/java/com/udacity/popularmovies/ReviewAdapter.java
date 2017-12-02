package com.udacity.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by vinaygharge on 02/12/17.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    public ArrayList<Reviews> path = new ArrayList<Reviews>();
    Context context;

    public ReviewAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ReviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_reviews, parent, false);
        return new ReviewAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ReviewAdapter.ViewHolder holder, int position) {
        holder.textView.setText(path.get(position).getContent());
        Log.d("ADebugTag", "hello.." + path.get(position));
    }

    @Override
    public int getItemCount() {
        return path.size();
    }

    public void addAllPath(ArrayList<Reviews> allPath) {
        path.clear();
        path.addAll(allPath);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.review_content);
        }
    }
}
