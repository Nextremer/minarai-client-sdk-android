package com.nextremer.minarai.client;

import android.support.annotation.NonNull;

/**
 * Defines the events.
 *
 * @author nextremer.com
 */
public enum MinaraiEvent {

    CONNECT("connect"),                         // When connected to minarai successfully
    DISCONNECTED("disconnected"),               // When disconnected to minarai successfully
    JOINED("joined"),                           // When signed in to minarai as client successfully
    SYNC("sync"),                               // When you or your group send message to minarai(for sync message between multiple devices)
    SYNC_SYSTEM_COMMAND("sync-system-command"), // When you or your group send system command to minarai(for sync system command between multiple devices)
    SYNC_COMMAND("sync-command"),               //
    MESSAGE("message"),                         // When minarai send any event
    OPERATOR_COMMAND("operator-command"),       //
    SYSTEM_MESSAGE("system-message"),           //
    LOGS("logs"),                               //
    ERROR("error");                             //

    private final String socketIoEvent; // Socket.IO event.

    /**
     * Constructor.
     *
     * @param socketIoEvent Socket.IO event.
     */
    private MinaraiEvent(@NonNull String socketIoEvent) {
        this.socketIoEvent = socketIoEvent;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return socketIoEvent;
    }

    /**
     * Gets Socket.IO event.
     */
    String getSocketIoEvent() {
        return socketIoEvent;
    }

    /**
     * Gets instance match with Socket.IO event.
     *
     * @param socketIoEvent Socket.IO event.
     * @param def           Default instance when matched value not found.
     * @return instance.
     */
    static MinaraiEvent valueOf(String socketIoEvent, MinaraiEvent def) {

        for(MinaraiEvent value : values()) {
            if(value.socketIoEvent.equals(socketIoEvent))
                return value;
        }

        return def;
    }
}
