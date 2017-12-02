package com.udacity.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import utils.FavoriteContract;
import utils.FavoritesDbHelper;
import utils.NetworkUtils;

public class MainActivity extends AppCompatActivity {

    private static final String SCROLL_POSITION = "scroll";
    private static final String SORTING = "sorting";
    MovieAdapter movieAdapter;
    ArrayList<Movies> mModel;
    ArrayList<Movies> mModelTemp;
    RecyclerView movies;
    String[] columnName = new String[1];
    String[] columnValue;
    StaggeredGridLayoutManager layoutManager;
    int scrollPos = 0;
    private SQLiteDatabase mDb;
    private Parcelable recyclerViewState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(4.0f);
        movies = (RecyclerView) findViewById(R.id.main_recycler_view);
        movieAdapter = new MovieAdapter(this);
        movies.setHasFixedSize(true);
        movies.setAdapter(movieAdapter);
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        movies.setLayoutManager(layoutManager);
        mModel = new ArrayList<>();
        mModelTemp = new ArrayList<>();
        if (isOnline()) {
            if (NetworkUtils.POPULAR_TEXT.equals(getIntent().getStringExtra("sortBy")) ||
                    getIntent().getStringExtra("sortBy") == null ||
                    "".equals(getIntent().getStringExtra("sortBy"))) {
                URL url = NetworkUtils.generateURL(NetworkUtils.POPULAR_TEXT);
                new MainAsyncClass().execute(url);
            } else if (NetworkUtils.TOP_RATED_TEXT.equals(getIntent().getStringExtra("sortBy"))) {
                URL url = NetworkUtils.generateURL(NetworkUtils.TOP_RATED_TEXT);
                new MainAsyncClass().execute(url);
            } else if (NetworkUtils.FAVORITES_TEXT.equals(getIntent().getStringExtra("sortBy"))) {
                URL url = NetworkUtils.generateURL(NetworkUtils.POPULAR_TEXT);
                new MainAsyncClass().execute(url);
            }
        } else {
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_LONG).show();
        }


        columnName[0] = FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID;
        FavoritesDbHelper dbHelper = FavoritesDbHelper.getInstance(this);
        mDb = dbHelper.getWritableDatabase();
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SCROLL_POSITION)) {
                scrollPos = Integer.parseInt(savedInstanceState.getString(SCROLL_POSITION));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        movies.getLayoutManager().scrollToPosition(scrollPos);
                    }
                }, 200);
                Log.d("ADebugTag", "scrollPos..................." + scrollPos);
            }
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
            case R.id.favorites:
                Intent intentFV = new Intent(this, MainActivity.class);
                intentFV.putExtra("sortBy", NetworkUtils.FAVORITES_TEXT);
                startActivity(intentFV);
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
        intent.putExtra("id", movies.getId());
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int into[] = new int[2];
        layoutManager.findFirstVisibleItemPositions(into);
        outState.putString(SCROLL_POSITION, String.valueOf(into[0]));
        recyclerViewState = layoutManager.onSaveInstanceState();//save
        outState.putParcelable("statekey", recyclerViewState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            recyclerViewState = savedInstanceState.getParcelable("statekey");
        }
    }

    //
//
    @Override
    protected void onResume() {
        super.onResume();
        if (recyclerViewState != null) {
            layoutManager.onRestoreInstanceState(recyclerViewState);
        }
        Log.d("ADebugTag", "SCROLL..");
    }

    private Cursor getAllFavorites() {
        return getContentResolver().query(
                FavoriteContract.FavoriteEntry.CONTENT_URI,
                columnName,
                FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID + "=?",
                columnValue,
                FavoriteContract.FavoriteEntry.COLUMN_CREATION_DATE
        );
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

                if (NetworkUtils.FAVORITES_TEXT.equals(getIntent().getStringExtra("sortBy"))) {
                    mModelTemp.clear();
                    columnValue = new String[1];
                    String movieId = null;
                    for (int i = 0; i < mModel.size(); i++) {
                        columnValue[0] = mModel.get(i).getId();
                        Cursor cursor = getAllFavorites();
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            movieId = cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID));
                        }
                        if (mModel.get(i).getId().equals(movieId)) {
                            mModelTemp.add(mModel.get(i));
                        }
                    }
                    mModel.clear();
                    mModel = mModelTemp;
                }
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
