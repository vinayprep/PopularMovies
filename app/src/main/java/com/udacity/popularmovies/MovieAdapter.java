package com.udacity.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by vinaygharge on 28/10/17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {
    public ArrayList<Movies> path = new ArrayList<Movies>();
    Context context;

    public MovieAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_movies, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Picasso.with(context).load(path.get(position).getPoster_path()).fit().into(holder.imageView);
        Log.d("ADebugTag", "hello.." + path.get(position));
    }

    @Override
    public int getItemCount() {
        return path.size();
    }

    public void addAllPath(ArrayList<Movies> allPath) {
        path.clear();
        path.addAll(allPath);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.movie_poster);
            imageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (context instanceof MainActivity) {
                ((MainActivity) context).onClickPoster(path.get(getAdapterPosition()));
            }
        }
    }
}
