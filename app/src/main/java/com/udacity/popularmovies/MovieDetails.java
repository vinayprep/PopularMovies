package com.udacity.popularmovies;

import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.util.Date;

public class MovieDetails extends AppCompatActivity {

    String poster_path;
    String original_title;
    String overview;
    String vote_average;
    String release_date;
    TextView movieTitle;
    TextView voteAverage;
    TextView releaseDate;
    TextView movieOverview;
    ImageView posterThumbnail;

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
        movieTitle = (TextView) findViewById(R.id.movie_title);
        posterThumbnail = (ImageView) findViewById(R.id.poster_thumbnail);
        voteAverage = (TextView) findViewById(R.id.vote_average);
        releaseDate = (TextView) findViewById(R.id.release_date);
        movieOverview = (TextView) findViewById(R.id.overview);

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
    }

}
