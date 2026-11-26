package ues.edu.sv.proyect_bs;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionManager {
    private static final String PREFERENCES = "auth_session";
    private static final String TOKEN = "token";
    private static final String TOKEN_TYPE = "token_type";
    private static final String PROFILE = "profile";
    private static final String USER_ID = "user_id";
    private static final String PERSON_ID = "person_id";
    private static final String REMEMBER_LOGIN = "remember_login";
    private static final String SAVED_EMAIL = "saved_email";
    private static final String SAVED_PASSWORD = "saved_password";

    private SessionManager() {
    }

    public static void save(Context context, String token, String tokenType, String profile,
                     long userId, long personId) {
        preferences(context).edit()
                .putString(TOKEN, token)
                .putString(TOKEN_TYPE, tokenType)
                .putString(PROFILE, profile)
                .putLong(USER_ID, userId)
                .putLong(PERSON_ID, personId)
                .apply();
    }

    public static String getToken(Context context) {
        return preferences(context).getString(TOKEN, null);
    }

    public static String getTokenType(Context context) {
        return preferences(context).getString(TOKEN_TYPE, "Bearer");
    }

    public static String getProfile(Context context) {
        return preferences(context).getString(PROFILE, null);
    }

    public static long getUserId(Context context) {
        return preferences(context).getLong(USER_ID, 0L);
    }

    public static long getPersonId(Context context) {
        return preferences(context).getLong(PERSON_ID, 0L);
    }

    public static boolean hasAdminAccess(Context context) {
        return getToken(context) != null && "ADMIN".equalsIgnoreCase(getProfile(context));
    }

    public static boolean shouldRememberLogin(Context context) {
        return preferences(context).getBoolean(REMEMBER_LOGIN, false);
    }

    public static void setRememberLogin(Context context, boolean remember) {
        preferences(context).edit()
                .putBoolean(REMEMBER_LOGIN, remember)
                .apply();
        if (!remember) {
            clearSavedLogin(context);
        }
    }

    public static void saveLoginCredentials(Context context, String email, String password) {
        preferences(context).edit()
                .putString(SAVED_EMAIL, email)
                .putString(SAVED_PASSWORD, password)
                .apply();
    }

    public static String getSavedEmail(Context context) {
        return preferences(context).getString(SAVED_EMAIL, "");
    }

    public static String getSavedPassword(Context context) {
        return preferences(context).getString(SAVED_PASSWORD, "");
    }

    public static void clearSavedLogin(Context context) {
        preferences(context).edit()
                .remove(SAVED_EMAIL)
                .remove(SAVED_PASSWORD)
                .apply();
    }

    public static void clear(Context context) {
        preferences(context).edit()
                .remove(TOKEN)
                .remove(TOKEN_TYPE)
                .remove(PROFILE)
                .remove(USER_ID)
                .remove(PERSON_ID)
                .apply();
        if (!shouldRememberLogin(context)) {
            clearSavedLogin(context);
        }
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }
}
