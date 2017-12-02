package utils;

import android.provider.BaseColumns;

/**
 * Created by vinaygharge on 02/12/17.
 */

public class FavoriteContract {
    public static final class FavoriteEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_MOVIE_ID = "movieId";
        public static final String COLUMN_CREATION_DATE = "creationDate";
    }
}
