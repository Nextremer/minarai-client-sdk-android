package com.nextremer.minarai.client;

import org.json.JSONObject;

/**
 * Send options class.
 *
 * @author nextremer.com
 */
public class MinaraiSendOptions {

    private static final JSONObject DEFAULT_POSITION = new JSONObject();    // Default position data.
    private static final JSONObject DEFAULT_EXTRA = new JSONObject();       // Default extra data.

    private String lang;            // Language.
    private JSONObject position;    // position.
    private JSONObject extra;       // Extra data.

    /**
     * Constructor.
     */
    public MinaraiSendOptions() {
    }

    /**
     * Constructor.
     *
     * @param lang Language.
     */
    public MinaraiSendOptions(String lang) {
        this.lang = lang;
    }

    /**
     * Constructor.
     *
     * @param lang     Language.
     * @param position Position.
     */
    public MinaraiSendOptions(String lang, JSONObject position) {
        this.lang = lang;
        this.position = position;
    }

    /**
     * Constructor.
     *
     * @param lang     Language.
     * @param position Position.
     * @param extra    Extra data.
     */
    public MinaraiSendOptions(String lang, JSONObject position, JSONObject extra) {
        this.lang = lang;
        this.position = position;
        this.extra = extra;
    }

    /**
     * Gets language.
     */
    public String getLang() {
        return lang;
    }

    /**
     * Gets language. Return default value if it is not set.
     *
     * @param def Default value.
     * @return Language.
     */
    String getLangOrDefault(String def) {
        return (lang != null) ? lang : def;
    }

    /**
     * Gets position.
     */
    public JSONObject getPosition() {
        return position;
    }

    /**
     * Gets position. Return default value if it is not set.
     */
    JSONObject getPositionOrDefault() {
        return (position != null) ? position : DEFAULT_POSITION;
    }

    /**
     * Gets extra data.
     */
    public JSONObject getExtra() {
        return extra;
    }

    /**
     * Gets extra data. Return default value if it is not set.
     */
    JSONObject getExtraOrDefault() {
        return (extra != null) ? extra : DEFAULT_EXTRA;
    }
}
