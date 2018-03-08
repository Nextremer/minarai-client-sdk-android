package com.nextremer.minarai.client;

import com.github.nkzawa.socketio.client.IO;

/**
 * Client options class.
 *
 * @author nextremer.com
 */
public class MinaraiClientOptions {

    private static final String DEFAULT_LANG = "ja-JP";                                                 // Default language.
    private static final String DEFAULT_SOCKET_IO_ROOT_URL = "https://socketio-connector.minarai.ch";   // Default Socket.IO root URL.
    private static final String DEFAULT_API_VERSION = "v1";                                             // Default API version.

    private String lang;                // Language.
    private String socketIoRootUrl;     // Root url of minarai Socket.IO Connector
    private String apiVersion;          // API version of minarai Socket.IO Connector
    private IO.Options socketIoOptions; // Socket.IO options.
    private boolean getImageByHeader;   // Get image by header flag.

    /**
     * Constructor.
     */
    public MinaraiClientOptions() {
    }

    /**
     * Gets language.
     */
    public String getLang() {
        return lang;
    }

    /**
     * Gets language. Return default value if it is not set.
     */
    String getLangOrDefault() {
        return (lang != null) ? lang : DEFAULT_LANG;
    }

    /**
     * Puts language.
     *
     * @param lang Language.
     * @return this
     */
    public MinaraiClientOptions putLang(String lang) {
        this.lang = lang;
        return this;
    }

    /**
     * Gets root URL.
     */
    public String getSocketIoRootUrl() {
        return socketIoRootUrl;
    }

    /**
     * Gets root URL. Return default value if it is not set.
     */
    String getSocketIoRootUrlOrDefault() {
        return (socketIoRootUrl != null) ? socketIoRootUrl : DEFAULT_SOCKET_IO_ROOT_URL;
    }

    /**
     * Puts root URL.
     *
     * @param url URL
     * @return this
     */
    public MinaraiClientOptions putSocketIoRootUrl(String url) {
        socketIoRootUrl = url;
        return this;
    }

    /**
     * Gets API version.
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Gets API version. Return default value if it is not set.
     */
    String getApiVersionOrDefault() {
        return (apiVersion != null) ? apiVersion : DEFAULT_API_VERSION;
    }

    /**
     * Puts API version.
     *
     * @param version API version.
     * @return this
     */
    public MinaraiClientOptions putApiVersion(String version) {
        apiVersion = version;
        return this;
    }

    /**
     * Gets Socket.IO options.
     */
    public IO.Options getSocketIoOptions() {
        return socketIoOptions;
    }

    /**
     * Gets Socket.IO options.Return default value if it is not set.
     *
     * @param defApiVersion API version for default value.
     */
    IO.Options getSocketIoOptionsOrDefault(String defApiVersion) {

        if(socketIoOptions != null)
            return socketIoOptions;

        IO.Options opts = new IO.Options();
        opts.path = "/socket.io/" + defApiVersion;
        opts.transports = new String[] {"websocket"};

        return opts;
    }

    /**
     * Puts Socket.IO options.
     *
     * @param options Options.
     * @return this
     */
    public MinaraiClientOptions putSocketIoOptions(IO.Options options) {
        socketIoOptions = options;
        return this;
    }

    /**
     * Gets get image by header flag.
     */
    public boolean isGetImageByHeader() {
        return getImageByHeader;
    }

    /**
     * Puts get image by header flag.
     *
     * @param enabled flag.
     * @return this
     */
    public MinaraiClientOptions putGetImageByHeader(boolean enabled) {
        this.getImageByHeader = enabled;
        return this;
    }
}
