package com.udacity.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import utils.NetworkUtils;

public class MainActivity extends AppCompatActivity {

    public static final String MY_PREFS_NAME = "MyPrefsFile";
    private static final String SORTING = "sorting";
    private static final String SCROLL_POSITION = "scroll";
    MovieAdapter movieAdapter;
    ArrayList<Movies> mModel;
    ArrayList<Movies> mModelTemp;
    RecyclerView movies;
    String[] columnName = new String[1];
    String[] columnValue;
    StaggeredGridLayoutManager layoutManager;
    int scrollPos = 0;
    String sortBy;
    SharedPreferences sharedpreferences;
    String sorting;
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
        sharedpreferences = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        sorting = sharedpreferences.getString("sorting", NetworkUtils.POPULAR_TEXT);
        Log.d("ADebugTag", "sorting............" + sorting);
        columnName[0] = FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID;
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SCROLL_POSITION)) {
                scrollPos = Integer.parseInt(savedInstanceState.getString(SCROLL_POSITION));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        movies.getLayoutManager().scrollToPosition(scrollPos);
                    }
                }, 200);
            }
        } else {
            scrollPos = Integer.parseInt(sharedpreferences.getString("scrollPostion", "0"));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    movies.getLayoutManager().scrollToPosition(scrollPos);
                }
            }, 200);
        }
        if (isOnline()) {
            if (NetworkUtils.POPULAR_TEXT.equals(sorting) ||
                    sorting == null ||
                    "".equals(sorting)) {
                URL url = NetworkUtils.generateURL(NetworkUtils.POPULAR_TEXT);
                new MainAsyncClass().execute(url);
            } else if (NetworkUtils.TOP_RATED_TEXT.equals(sorting)) {
                URL url = NetworkUtils.generateURL(NetworkUtils.TOP_RATED_TEXT);
                new MainAsyncClass().execute(url);
            } else if (NetworkUtils.FAVORITES_TEXT.equals(sorting)) {
                URL url = NetworkUtils.generateURL(NetworkUtils.POPULAR_TEXT);
                new MainAsyncClass().execute(url);
                url = NetworkUtils.generateURL(NetworkUtils.TOP_RATED_TEXT);
                new MainAsyncClass().execute(url);
            }
        } else {
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedpreferences.edit();
        int into[] = new int[2];
        layoutManager.findFirstVisibleItemPositions(into);
        editor.putString("scrollPostion", String.valueOf(into[0]));
        editor.apply();
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
        SharedPreferences.Editor editor = sharedpreferences.edit();

        switch (item.getItemId()) {
            case R.id.popular:
                editor.putString("sorting", NetworkUtils.POPULAR_TEXT);
                editor.commit();
                Intent intentPop = new Intent(this, MainActivity.class);
                startActivity(intentPop);
                return true;
            case R.id.top_rated:
                editor.putString("sorting", NetworkUtils.TOP_RATED_TEXT);
                editor.commit();
                Intent intentTR = new Intent(this, MainActivity.class);
                startActivity(intentTR);
                return true;
            case R.id.favorites:
                editor.putString("sorting", NetworkUtils.FAVORITES_TEXT);
                editor.commit();
                Intent intentFV = new Intent(this, MainActivity.class);
                startActivity(intentFV);
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

                if (NetworkUtils.FAVORITES_TEXT.equals(sorting)) {
                    columnValue = new String[1];
                    String movieId = null;
                    for (int i = 0; i < mModel.size(); i++) {
                        columnValue[0] = mModel.get(i).getId();
                        Cursor cursor = getAllFavorites();
                        cursor.moveToFirst();
                        if (cursor.getCount() > 0) {
                            movieId = cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID));
                            if (mModel.get(i).getId().equals(movieId)) {
                                mModelTemp.add(mModel.get(i));
                            }
                        }
                    }
                    mModel.clear();
                    mModel.addAll(mModelTemp);
                    mModelTemp.clear();
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
