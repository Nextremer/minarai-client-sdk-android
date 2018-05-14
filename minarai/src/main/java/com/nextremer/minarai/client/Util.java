package com.nextremer.minarai.client;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class.
 *
 * @author nextremer.com
 */
class Util {

    public static final JSONObject EMPTY_JSON_OBJECT = new JSONObject();    // Empty json object.

    /**
     * Constructor.
     */
    private Util() {
    }

    /**
     * Gets UNIX time.
     *
     * @return UNIX time.
     */
    public static long getUnixTime() {
        return System.currentTimeMillis() / 1000L;
    }

    /**
     * Duplicate JSONObject.
     *
     * @param src Source object.
     * @return Duplicated object.
     * @throws JSONException When source object format is invalid.
     */
    public static JSONObject duplicate(@NonNull JSONObject src) throws JSONException {

        if(src == null)
            throw new IllegalArgumentException("src must not be null.");

        return new JSONObject(src.toString());
    }
}
