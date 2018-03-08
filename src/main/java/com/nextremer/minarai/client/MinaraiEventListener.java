package com.nextremer.minarai.client;

import android.support.annotation.NonNull;

import org.json.JSONObject;

/**
 * Defines a listener interface to handle events.
 *
 * @author nextremer.com
 */
public interface MinaraiEventListener {

    /**
     * Called when event occurred.
     *
     * @param event Event.
     * @param data  Event data.
     */
    void onEvent(@NonNull MinaraiEvent event, JSONObject data);
}
