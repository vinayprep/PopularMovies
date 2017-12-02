package com.udacity.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import utils.FavoriteContract;
import utils.FavoritesDbHelper;
import utils.NetworkUtils;

import static utils.NetworkUtils.API_KEY_VALUE;
import static utils.NetworkUtils.PARAM_API_KEY;

public class MovieDetails extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>, View.OnClickListener {

    private static final int SEARCH_LOADER = 22;
    private static final String SEARCH_QUERY_URL_EXTRA = "query";
    String poster_path;
    String original_title;
    String overview;
    String vote_average;
    String release_date;
    String id;
    TextView movieTitle;
    TextView voteAverage;
    TextView releaseDate;
    TextView movieOverview;
    TextView addedToFav;
    Button favoriteBtn;
    Button removeFromBtn;
    ImageView posterThumbnail;
    ReviewAdapter reviewAdapter;
    ArrayList<Reviews> mModelReviews;
    RecyclerView reviews;
    TrailerAdapter trailerAdapter;
    ArrayList<Trailers> mModel;
    RecyclerView trailers;
    String[] columnName = new String[1];
    String[] columnValue = new String[1];
    int columnId = 0;
    private SQLiteDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Movie Details");
        getSupportActionBar().setElevation(4.0f);
        poster_path = getIntent().getStringExtra("poster_path");
        original_title = getIntent().getStringExtra("original_title");
        overview = getIntent().getStringExtra("overview");
        vote_average = getIntent().getStringExtra("vote_average");
        release_date = getIntent().getStringExtra("release_date");
        id = getIntent().getStringExtra("id");
        movieTitle = (TextView) findViewById(R.id.movie_title);
        posterThumbnail = (ImageView) findViewById(R.id.poster_thumbnail);
        voteAverage = (TextView) findViewById(R.id.vote_average);
        releaseDate = (TextView) findViewById(R.id.release_date);
        movieOverview = (TextView) findViewById(R.id.overview);
        removeFromBtn = (Button) findViewById(R.id.remove_from_fav_btn);
        favoriteBtn = (Button) findViewById(R.id.mark_as_fav_btn);

        String dateStr = release_date;

        SimpleDateFormat curFormater = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            curFormater = new SimpleDateFormat("yyyy-mm-dd");
        }
        Date dateObj = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dateObj = curFormater.parse(dateStr);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat postFormater = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            postFormater = new SimpleDateFormat("MMM dd, yyyy");
        }
        String newDateStr = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            newDateStr = postFormater.format(dateObj);
        }

        movieTitle.setText(original_title);
        Picasso.with(this).load(poster_path).fit().into(posterThumbnail);
        voteAverage.setText(vote_average + "/10");
        if (newDateStr != null) {
            releaseDate.setText(newDateStr);
        } else {
            releaseDate.setText(release_date);
        }
        movieOverview.setText(overview);

        trailers = (RecyclerView) findViewById(R.id.trailers_recycler_view);
        trailerAdapter = new TrailerAdapter(this);
        trailers.setHasFixedSize(false);
        trailers.setAdapter(trailerAdapter);
        reviews = (RecyclerView) findViewById(R.id.reviews_recycler_view);
        reviewAdapter = new ReviewAdapter(this);
        reviews.setHasFixedSize(false);
        reviews.setAdapter(reviewAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        trailers.setLayoutManager(llm);
        LinearLayoutManager llmReviews = new LinearLayoutManager(this);
        llmReviews.setOrientation(LinearLayoutManager.VERTICAL);
        reviews.setLayoutManager(llmReviews);
        mModel = new ArrayList<>();
        mModelReviews = new ArrayList<>();
        if (isOnline()) {
            Bundle queryBundle = new Bundle();
            queryBundle.putString(SEARCH_QUERY_URL_EXTRA, id);
            LoaderManager loaderManager = getSupportLoaderManager();
            Loader<String> loader = loaderManager.getLoader(SEARCH_LOADER);
            if (loader == null) {
                loaderManager.initLoader(SEARCH_LOADER, queryBundle, this);
            } else {
                loaderManager.restartLoader(SEARCH_LOADER, queryBundle, this);
            }
        } else {
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_LONG).show();
        }
        favoriteBtn.setOnClickListener(this);
        removeFromBtn.setOnClickListener(this);

        columnName[0] = FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID;
        columnValue[0] = id;
        FavoritesDbHelper dbHelper = new FavoritesDbHelper(this);
        mDb = dbHelper.getWritableDatabase();
        Cursor cursor = getAllFavorites();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            String movieId = cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID));
            if (id.equals(movieId)) {
                removeFromBtn.setVisibility(View.VISIBLE);
                favoriteBtn.setVisibility(View.GONE);
            } else {
                removeFromBtn.setVisibility(View.GONE);
                favoriteBtn.setVisibility(View.VISIBLE);
            }
        }

    }

    private Cursor getAllFavorites() {
        return mDb.query(
                FavoriteContract.FavoriteEntry.TABLE_NAME,
                columnName,
                FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID + "=?",
                columnValue,
                null,
                null,
                FavoriteContract.FavoriteEntry.COLUMN_CREATION_DATE
        );
    }

    private long addToFavorites() {
        ContentValues cv = new ContentValues();
        cv.put(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID, id);
        return mDb.insert(FavoriteContract.FavoriteEntry.TABLE_NAME, null, cv);
    }

    private boolean removeFromFavorites(String movieId) {
        return mDb.delete(FavoriteContract.FavoriteEntry.TABLE_NAME, FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID + "=" + movieId, null) > 0;
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if (args == null) {
                    Log.d("ADebug", "Inside onStartLoading11");
                    return;
                }
                forceLoad();
                Log.d("ADebug", "Inside onStartLoading");
            }

            @Override
            public String loadInBackground() {
                Log.d("ADebug", "Inside loadInBackground11");
                String id = args.getString(SEARCH_QUERY_URL_EXTRA);
                if (id == null || TextUtils.isEmpty(id)) {
                    return null;
                }
                Uri buildUri = Uri.parse(NetworkUtils.BASE_URL + NetworkUtils.MOVIE_TRAILERS.replace("{id}", id)).buildUpon()
                        .appendQueryParameter(PARAM_API_KEY, API_KEY_VALUE)
                        .build();
                URL buildUrl = null;
                Log.d("ADebug", "URL: " + buildUri.toString());

                try {
                    buildUrl = new URL(buildUri.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                String movieTrailers = null;
                try {
                    movieTrailers = NetworkUtils.getResponseFromURL(buildUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                Log.d("ADebug", "Length: " + movieTrailers.length());

                try {
                    mModel.addAll(Arrays.asList(NetworkUtils.getMovieTrailers(movieTrailers)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                Uri buildReviewsUri = Uri.parse(NetworkUtils.BASE_URL + NetworkUtils.MOVIE_REVIEWS.replace("{id}", id)).buildUpon()
                        .appendQueryParameter(PARAM_API_KEY, API_KEY_VALUE)
                        .build();
                URL buildReviewsUrl = null;
                Log.d("ADebug", "URL: " + buildReviewsUri.toString());

                try {
                    buildReviewsUrl = new URL(buildReviewsUri.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                String movieReviews = null;
                try {
                    movieReviews = NetworkUtils.getResponseFromURL(buildReviewsUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                Log.d("ADebug", "Length: " + movieReviews.length());

                try {
                    mModelReviews.addAll(Arrays.asList(NetworkUtils.getMovieReviews(movieReviews)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return movieTrailers;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        Log.d("ADebug", "Inside onLoadFinished1");
        if (data != null && !data.equals("")) {
            trailerAdapter.addAllPath(mModel);
            reviewAdapter.addAllPath(mModelReviews);
        }
        Log.d("ADebug", "Inside onLoadFinished2");
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }


    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void onClickTrailer(String key) {
        String url = "https://www.youtube.com/watch?v=".concat(key);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mark_as_fav_btn:
                addToFavorites();
                break;
            case R.id.remove_from_fav_btn:
                removeFromFavorites(id);
                break;
        }
        Intent intent = new Intent(this, MovieDetails.class);
        intent.putExtra("original_title", original_title);
        intent.putExtra("poster_path", poster_path);
        intent.putExtra("overview", overview);
        intent.putExtra("vote_average", vote_average);
        intent.putExtra("release_date", release_date);
        intent.putExtra("id", id);
        startActivity(intent);
    }
}
