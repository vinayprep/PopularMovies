package com.udacity.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import utils.NetworkUtils;

public class MainActivity extends AppCompatActivity {

    MovieAdapter movieAdapter;
    ArrayList<Movies> mModel;
    RecyclerView movies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(4.0f);
        movies = (RecyclerView) findViewById(R.id.main_recycler_view);
        movieAdapter = new MovieAdapter(this);
        movies.setHasFixedSize(false);
        movies.setAdapter(movieAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        movies.setLayoutManager(layoutManager);
        mModel = new ArrayList<>();
        if (isOnline()) {
            if (NetworkUtils.POPULAR_TEXT.equals(getIntent().getStringExtra("sortBy")) ||
                    getIntent().getStringExtra("sortBy") == null ||
                    "".equals(getIntent().getStringExtra("sortBy"))) {
                URL url = NetworkUtils.generateURL(NetworkUtils.POPULAR_TEXT);
                new MainAsyncClass().execute(url);
            } else if (NetworkUtils.TOP_RATED_TEXT.equals(getIntent().getStringExtra("sortBy"))) {
                URL url = NetworkUtils.generateURL(NetworkUtils.TOP_RATED_TEXT);
                new MainAsyncClass().execute(url);
            }
        } else {
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_LONG).show();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.popular:
                Intent intentPop = new Intent(this, MainActivity.class);
                intentPop.putExtra("sortBy", NetworkUtils.POPULAR_TEXT);
                startActivity(intentPop);
                finish();
                return true;
            case R.id.top_rated:
                Intent intentTR = new Intent(this, MainActivity.class);
                intentTR.putExtra("sortBy", NetworkUtils.TOP_RATED_TEXT);
                startActivity(intentTR);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClickPoster(Movies movies) {
        Intent intent = new Intent(this, MovieDetails.class);
        intent.putExtra("original_title", movies.getOriginal_title());
        intent.putExtra("poster_path", movies.getPoster_path());
        intent.putExtra("overview", movies.getOverview());
        intent.putExtra("vote_average", movies.getVote_average());
        intent.putExtra("release_date", movies.getRelease_date());
        startActivity(intent);
    }

    class MainAsyncClass extends AsyncTask<URL, Void, ArrayList<Movies>> {
        @Override
        protected ArrayList<Movies> doInBackground(URL... urls) {
            String response = null;
            try {
                response = NetworkUtils.getResponseFromURL(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mModel.addAll(Arrays.asList(NetworkUtils.getPosterImage(response)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return mModel;
        }

        @Override
        protected void onPostExecute(ArrayList<Movies> s) {
            super.onPostExecute(s);
            movieAdapter.addAllPath(mModel);
        }
    }
}
