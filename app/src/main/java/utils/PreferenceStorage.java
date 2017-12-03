package utils;

import android.content.Context;
import android.content.SharedPreferences;


public class PreferenceStorage {
    public static final String PREFERENCE_NAME = "PREFERENCE_DATA";
    public static PreferenceStorage preferenceStorage;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public PreferenceStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static PreferenceStorage getInstance() {
        if (preferenceStorage == null) {
            preferenceStorage = new PreferenceStorage(MyApplication.getContext());
        }
        return preferenceStorage;
    }

    public void saveStringData(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void saveIntData(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public void saveDoubleData(String key, double value) {
        editor.putLong(key, Double.doubleToLongBits(value));
        editor.commit();
    }

    public String getStringData(String key) {
        return sharedPreferences.getString(key, "");
    }

    public int getIntData(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public Double getDoubleData(String key) {
        return Double.longBitsToDouble(sharedPreferences.getLong(key, Double.doubleToLongBits(0)));
    }

    public boolean removeKey(String key) {
        return editor.remove(key).commit();
    }

}
