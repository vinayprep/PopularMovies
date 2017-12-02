package com.udacity.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by vinaygharge on 02/12/17.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.ViewHolder> {
    public ArrayList<Trailers> path = new ArrayList<Trailers>();
    Context context;

    public TrailerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public TrailerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_trailers, parent, false);
        return new TrailerAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TrailerAdapter.ViewHolder holder, int position) {
        holder.textView.setText(path.get(position).getName());
        Log.d("ADebugTag", "hello.." + path.get(position));
    }

    @Override
    public int getItemCount() {
        return path.size();
    }

    public void addAllPath(ArrayList<Trailers> allPath) {
        path.clear();
        path.addAll(allPath);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.trailer_play_button);
            textView = (TextView) itemView.findViewById(R.id.trailer_number);
            imageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (context instanceof MovieDetails) {
                ((MovieDetails) context).onClickTrailer(path.get(getAdapterPosition()).getKey());
            }
        }
    }
}
