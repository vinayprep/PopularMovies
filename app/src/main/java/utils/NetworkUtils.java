package utils;

import android.net.Uri;

import com.udacity.popularmovies.BuildConfig;
import com.udacity.popularmovies.Movies;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by vinaygharge on 28/10/17.
 */

public class NetworkUtils {

    public static String BASE_URL = "http://api.themoviedb.org/3";
    public static String API_KEY_VALUE = BuildConfig.API_KEY;
    public static String POPULAR = "/movie/popular";
    public static String TOP_RATED = "/movie/top_rated";
    public static String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
    public static String IMAGE_SIZE = "w185/";
    public static String POPULAR_TEXT = "POPULAR";
    public static String TOP_RATED_TEXT = "TOP_RATED";


    public static String PARAM_API_KEY = "api_key";

    public static URL generateURL(String menuSelected) {
        String paramToPass = null;
        if (POPULAR_TEXT.equals(menuSelected)) {
            paramToPass = POPULAR;
        } else if (TOP_RATED_TEXT.equals(menuSelected)) {
            paramToPass = TOP_RATED;
        }
        Uri buildUri = Uri.parse(BASE_URL + paramToPass).buildUpon()
                .appendQueryParameter(PARAM_API_KEY, API_KEY_VALUE)
                .build();
        URL buildUrl = null;

        try {
            buildUrl = new URL(buildUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return buildUrl;
    }

    public static String getResponseFromURL(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        InputStream in = urlConnection.getInputStream();

        Scanner scanner = new Scanner(in);
        scanner.useDelimiter("\\A");

        boolean hasNext = scanner.hasNext();
        if (hasNext) {
            return scanner.next();
        }else {
            return null;
        }
    }

    public static Movies[] getPosterImage(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);
        String results = jsonObject.getString("results");
        JSONArray result = new JSONArray(results);
        Movies[] movies = new Movies[result.length()];
        for(int i = 0; i<result.length();i++) {
            JSONObject jsonobject = result.getJSONObject(i);
            movies[i] = new Movies();
            movies[i].setId(jsonobject.getString("id"));
            movies[i].setPoster_path(IMAGE_BASE_URL + IMAGE_SIZE + jsonobject.getString("poster_path"));
            movies[i].setBackdrop_path(jsonobject.getString("backdrop_path"));
            movies[i].setOriginal_title(jsonobject.getString("original_title"));
            movies[i].setOverview(jsonobject.getString("overview"));
            movies[i].setVote_average(jsonobject.getString("vote_average"));
            movies[i].setRelease_date(jsonobject.getString("release_date"));
        }
        return movies;
    }
}
