package com.nextremer.minarai.client;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Client operations class.
 * Implementation is not thread-safe.
 *
 * @author nextremer.com
 */
public class MinaraiClient implements Closeable {

    private static final String LOG_TAG = MinaraiClient.class.getName();    // Logging tag.

    private static final MinaraiClientOptions DEFAULT_OPTIONS = new MinaraiClientOptions();     // Default options.
    private static final MinaraiSendOptions DEFAULT_SEND_OPTIONS = new MinaraiSendOptions();    // Default send options.

    private final MinaraiClientOptions opts;    // Options.
    private final Map<MinaraiEvent, List<MinaraiEventListener>> listeners;  // Event listeners.

    private Socket socketIo;                    // Socket.IO socket.
    private boolean initialized;                // Initialized flag.
    private boolean joined;                     // Joined flag.
    private String applicationId;               // Application secret to connect.
    private String applicationSecret;           // Application id to connect.
    private String clientId;                    // Client id.
    private String userId;                      // User id.
    private String deviceId;                    // Device id.
    private String imageUrl;                    // Image URL.
    private boolean closed;                     // Closed flag.

    private final OkHttpClient httpClient = new OkHttpClient(); // HTTP Client.

    /**
     * Constructor.
     *
     * @param applicationId     Application id to connect.
     * @param applicationSecret Application secret to connect.
     * @param clientId          Client id.
     * @param userId            User id.
     * @param deviceId          Device id.
     */
    public MinaraiClient(@NonNull String applicationId,
                         @NonNull String applicationSecret,
                         @NonNull String clientId,
                         @NonNull String userId,
                         @NonNull String deviceId) {
        this(applicationId,
                applicationSecret,
                clientId,
                userId,
                deviceId,
                null);
    }

    /**
     * Constructor.
     *
     * @param applicationId     Application id to connect.
     * @param applicationSecret Application secret to connect.
     * @param clientId          Client id.
     * @param userId            User id.
     * @param deviceId          Device id.
     * @param opts              Options.
     */
    public MinaraiClient(@NonNull String applicationId,
                         @NonNull String applicationSecret,
                         @NonNull String clientId,
                         @NonNull String userId,
                         @NonNull String deviceId,
                         MinaraiClientOptions opts) {

        if(applicationId == null)
            throw new IllegalArgumentException("applicationId must not be null.");

        if(applicationSecret == null)
            throw new IllegalArgumentException("applicationSecret must not be null.");

        if(clientId == null)
            throw new IllegalArgumentException("clientId must not be null.");

        if(userId == null)
            throw new IllegalArgumentException("userId must not be null.");

        if(deviceId == null)
            throw new IllegalArgumentException("deviceId must not be null.");

        this.opts = (opts != null) ? opts : DEFAULT_OPTIONS;
        this.applicationId = applicationId;
        this.applicationSecret = applicationSecret;
        this.clientId = clientId;
        this.userId = userId;
        this.deviceId = deviceId;
        this.imageUrl = this.opts.getSocketIoRootUrlOrDefault().replaceFirst("\\/$", "")
                + "/" + this.opts.getApiVersionOrDefault()
                + "/upload-image";

        listeners = new HashMap<>();
    }

    /**
     * Register event listener.
     *
     * @param event    Event to listen on.
     * @param listener Listener.
     */
    public void on(@NonNull MinaraiEvent event, @NonNull MinaraiEventListener listener) {

        if(event == null)
            throw new IllegalArgumentException("event must not be null.");

        if(listener == null)
            throw new IllegalArgumentException("listener must not be null.");

        List<MinaraiEventListener> list = listeners.get(event);

        if(list == null) {
            list = new CopyOnWriteArrayList<>();
            listeners.put(event, list);
        }

        list.add(listener);
    }

    /**
     * Unregister event listener.
     *
     * @param event    Event that was lisntened on.
     * @param listener Listener.
     * @return Successfully unregistered or not.
     */
    public boolean off(@NonNull MinaraiEvent event, @NonNull MinaraiEventListener listener) {

        if(event == null)
            throw new IllegalArgumentException("event must not be null.");

        if(listener == null)
            throw new IllegalArgumentException("listener must not be null.");

        List<MinaraiEventListener> list = listeners.get(event);

        if(list == null)
            return false;

        return list.remove(listener);
    }

    /**
     * Gets if connection is initialized or not.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Initialize connection.
     *
     * @return Successfully initialized or not.
     */
    public boolean init() {

        if(initialized) {
            Log.w(LOG_TAG, "Already initialized.");
            return false;
        }

        if(closed) {
            Log.w(LOG_TAG, "Already closed.");
            return false;
        }

        // Create Socket.IO socket.
        String socketIoRootURL = opts.getSocketIoRootUrlOrDefault();
        String socketIoApiVersion = opts.getApiVersionOrDefault();
        IO.Options socketIoOptions = opts.getSocketIoOptionsOrDefault(socketIoApiVersion);

        try {
            socketIo = IO.socket(socketIoRootURL, socketIoOptions);
        }
        catch(URISyntaxException ex) {
            Log.e(LOG_TAG, "Unexpected error: " + ex);
            return false;
        }

        // Register system event listeners.
        socketIo.on(MinaraiEvent.CONNECT.getSocketIoEvent(), new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                onConnect();
            }
        });
        socketIo.on(MinaraiEvent.JOINED.getSocketIoEvent(), new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                onJoined(args);
            }
        });

        // Register user event listeners.
        for(final MinaraiEvent event : MinaraiEvent.values()) {

            socketIo.on(event.getSocketIoEvent(), new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    onSocketIoEvent(event, args);
                }
            });
        }

        // Connect.
        socketIo.connect();

        initialized = true;

        return true;
    }

    /**
     * Called when connect.
     */
    private void onConnect() {
        Log.d(LOG_TAG, "onConnect");

        try {
            // Make payload.
            JSONObject payload = new JSONObject();

            payload.put("applicationId", applicationId);
            payload.put("applicationSecret", applicationSecret);
            payload.put("clientId", clientId);
            payload.put("userId", userId);
            payload.put("deviceId", deviceId);

            // Emit join event.
            socketIo.emit("join-as-client", payload);
        }
        catch(JSONException ex) {
            Log.e(LOG_TAG, "Failed to make json: " + ex);
            return;
        }
    }

    /**
     * Gets if client is joined or not.
     */
    public boolean isJoined() {
        return joined;
    }

    /**
     * Called when joined.
     *
     * @param args Arguments.
     */
    private void onJoined(Object... args) {
        Log.d(LOG_TAG, "onJoined: " + args);

        // Take payload from arguments.
        if(args.length < 1 || !(args[0] instanceof JSONObject)) {
            Log.e(LOG_TAG, "Unexpected argument: " + args);
            return;
        }

        JSONObject payload = (JSONObject)args[0];

        // Parse payload.
        try {
            applicationId = payload.getString("applicationId");
            applicationSecret = payload.getString("applicationSecret");
            clientId = payload.getString("clientId");
            userId = payload.getString("userId");
            deviceId = payload.getString("deviceId");

            joined = true;
        }
        catch(JSONException ex) {
            Log.e(LOG_TAG, "Failed to parse json: " + ex);
        }
    }

    /**
     * Called when Socket.IO event.
     *
     * @param event Event.
     * @param args  Arguments.
     */
    private void onSocketIoEvent(MinaraiEvent event, Object... args) {
        Log.d(LOG_TAG, "onSocketIoEvent: " + event + ", " + args);

        // Take data from arguments.
        JSONObject data;

        if(args.length < 1) {
            data = new JSONObject();
        }
        else if(args[0] instanceof JSONObject) {
            data = (JSONObject)args[0];
        }
        else {
            Log.e(LOG_TAG, "Socket.IO event is not JSONObject.");
            return;
        }

        // Get image data by URL form.
        switch(event) {
            case SYNC:
                try {
                    JSONObject body = data.getJSONObject("body");

                    if("image".equals(body.getString("type"))) {

                        JSONObject message = body.getJSONObject("message");
                        String imageUrl = message.getString("imageUrl");
                        String imageType = message.getString("imageType");

                        String url = getImageUrl(imageUrl, imageType);
                        message.put("url", url);
                    }
                }
                catch(JSONException ex) {
                }
                catch(IOException ex) {
                    Log.e(LOG_TAG, "Error while get image URL", ex);
                }
                break;

            case MESSAGE:
                try {
                    JSONObject body = data.getJSONObject("body");

                    if("image".equals(body.getString("type"))) {

                        JSONArray messages = body.getJSONArray("messages");

                        JSONObject message = messages.getJSONObject(0);
                        String imageUrl = message.getString("imageUrl");
                        String imageType = message.getString("imageType");

                        String url = getImageUrl(imageUrl, imageType);
                        message.put("url", url);
                    }
                }
                catch(JSONException ex) {
                }
                catch(IOException ex) {
                    Log.e(LOG_TAG, "Error while get image URL", ex);
                }
                break;
        }

        // Notify event to listeners.
        List<MinaraiEventListener> list = listeners.get(event);

        if(list == null)
            return;

        Log.i(LOG_TAG, "onEvent: " + event + ", " + data);

        for(MinaraiEventListener listener : list) {
            listener.onEvent(event, data);
        }
    }

    /**
     * Send message.
     *
     * @param uttr Message to send.
     * @return Successfully sent or not.
     */
    public final boolean send(@NonNull String uttr) {
        return send(uttr, null);
    }

    /**
     * Send message.
     *
     * @param uttr    Message to send.
     * @param options Options.
     * @return Successfully sent or not.
     */
    public boolean send(@NonNull String uttr, MinaraiSendOptions options) {

        if(uttr == null)
            throw new IllegalArgumentException("uttr must not be null.");

        if(options == null)
            options = DEFAULT_SEND_OPTIONS;

        // Check states.
        if(!checkInitialized() || !checkJoined() || !checkNotClosed())
            return false;

        try {
            // Make payload.
            JSONObject payload = makePayload();
            payload.getJSONObject("head").put("lang", options.getLangOrDefault(opts.getLangOrDefault()));

            JSONObject body = new JSONObject();
            body.put("message", uttr);
            body.put("position", options.getPositionOrDefault());
            body.put("extra", options.getExtraOrDefault());
            payload.put("body", body);

            // Emit event.
            Log.i(LOG_TAG, "send " + payload);
            socketIo.emit("message", payload);

            return true;
        }
        catch(JSONException ex) {
            Log.w(LOG_TAG, "send: " + ex);
            return false;
        }
    }

    /**
     * Send system command.
     *
     * @param command        Command.
     * @param commandPayload Command payload.
     * @return Successfully sent or not.
     */
    @Deprecated
    public boolean sendSystemCommand(@NonNull String command, JSONObject commandPayload) {
        Log.w(LOG_TAG, "sendSystemCommand is deprecated. Please use sendCommand instead.");

        if(command == null)
            throw new IllegalArgumentException("command must not be null.");

        if(commandPayload == null)
            commandPayload = Util.EMPTY_JSON_OBJECT;

        // Check states.
        if(!checkInitialized() || !checkJoined() || !checkNotClosed())
            return false;

        try {
            // Make payload.
            JSONObject payload = makePayload();

            JSONObject body = new JSONObject();
            JSONObject message = new JSONObject();
            message.put("command", command);
            message.put("payload", commandPayload);
            body.put("message", message);
            payload.put("body", body);

            // Emit event.
            Log.i(LOG_TAG, "send-system-command " + payload);
            socketIo.emit("system-command", payload);

            return true;
        }
        catch(JSONException ex) {
            Log.w(LOG_TAG, "sendSystemCommand: " + ex);
            return false;
        }
    }

    /**
     * Send command.
     *
     * @param name  Command name.
     * @param extra Extra data.
     * @return Successfully sent or not.
     */
    public boolean sendCommand(@NonNull String name, JSONObject extra) {

        if(name == null)
            throw new IllegalArgumentException("name must not be null.");

        if(extra == null)
            extra = Util.EMPTY_JSON_OBJECT;

        // Check states.
        if(!checkInitialized() || !checkJoined() || !checkNotClosed())
            return false;

        try {
            // Make payload.
            JSONObject payload = makePayload();

            JSONObject body = new JSONObject();
            body.put("name", name);
            body.put("extra", extra);
            payload.put("body", body);

            // Emit event.
            Log.i(LOG_TAG, "send-command " + payload);
            socketIo.emit("command", payload);

            return true;
        }
        catch(JSONException ex) {
            Log.w(LOG_TAG, "sendCommand: " + ex);
            return false;
        }
    }

    /**
     * Request get logs.
     *
     * @return Successfully requested or not.
     */
    public final boolean getLogs() {
        return getLogs(null);
    }

    /**
     * Request get logs.
     *
     * @param options Options. If null, use default options.
     * @return Successfully requested or not.
     */
    public boolean getLogs(MinaraiGetLogsOptions options) {

        // Check states.
        if(!checkInitialized() || !checkJoined() || !checkNotClosed())
            return false;

        try {
            // Make payload.
            JSONObject payload = makePayload();
            payload.put("id", payload.getString("id") + "-logs");

            JSONObject body = new JSONObject();
            if(options != null) {
                body.put("ltDate", options.getLtDate());
                body.put("limit", options.getLimit());
            }
            payload.put("body", body);

            // Emit event.
            Log.i(LOG_TAG, "logs " + payload);
            socketIo.emit("logs", payload);

            return true;
        }
        catch(JSONException ex) {
            Log.w(LOG_TAG, "sendCommand: " + ex);
            return false;
        }
    }

    /**
     * Upload image data.
     *
     * @param bytes   Bytes of image.
     * @param off     Byte offset of content.
     * @param len     Byte length of content.
     * @param type    Image type. e.g. 'image/jpeg'
     * @param options Options.
     * @return Image URL.
     * @throws IOException When communication error occurred.
     */
    public URL uploadImage(@NonNull byte[] bytes, int off, int len,
                           @NonNull String type,
                           @NonNull String fileName,
                           MinaraiSendOptions options) throws IOException, JSONException {

        if(bytes == null)
            throw new IllegalArgumentException("file must not be null.");

        if(type == null)
            throw new IllegalArgumentException("type must not be null.");

        if(fileName == null)
            throw new IllegalArgumentException("fileName must not be null.");

        MediaType mediaType = MediaType.parse(type);

        if(mediaType == null)
            throw new IOException("Can not parse media type: " + type);

        MultipartBody.Builder formBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("applicationId", applicationId)
                .addFormDataPart("applicationSecret", applicationSecret)
                .addFormDataPart("clientId", clientId)
                .addFormDataPart("userId", userId)
                .addFormDataPart("deviceId", deviceId)
                .addFormDataPart("file", fileName, RequestBody.create(mediaType, bytes, off, len));

        if(options != null && options.getExtra() != null)
            formBuilder.addFormDataPart("params", options.getExtra().toString());

        RequestBody form = formBuilder.build();

        Request request = new Request.Builder()
                .url(imageUrl)
                .post(form)
                .build();

        Response res = httpClient.newCall(request).execute();

        JSONObject data = new JSONObject(res.body().string());
        String message = data.getString("message"); //"ok" succeeded
        String url = data.getString("url");

        return new URL(url);
    }

    /**
     * Get image data by URL form.
     *
     * @param url  Image URL.
     * @param type Image Type.
     * @return Image data.
     * @throws IOException When communication error occurred.
     */
    private String getImageUrl(@NonNull String url, @NonNull String type) throws IOException {

        if(url == null)
            throw new IllegalArgumentException("url must not be null.");

        if(type == null)
            throw new IllegalArgumentException("type must not be null.");

        if(opts.isGetImageByHeader()) {
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .header("X-Minarai-Application-Id", applicationId)
                    .header("X-Minarai-Application-Secret", applicationSecret)
                    .header("X-Minarai-User-Id", userId)
                    .build();

            ResponseBody resBody = httpClient.newCall(request).execute().body();

            return "data:" + type + "base64," + Base64.encodeToString(resBody.bytes(), Base64.DEFAULT);
        }
        else {
            HttpUrl httpUrl = HttpUrl.parse(url).newBuilder()
                    .addQueryParameter("applicationId", applicationId)
                    .addQueryParameter("applicationSecret", applicationSecret)
                    .addQueryParameter("userId", userId)
                    .build();
            Request request = new Request.Builder()
                    .url(httpUrl)
                    .get()
                    .build();

            ResponseBody resBody = httpClient.newCall(request).execute().body();

            return "data:" + type + "base64," + Base64.encodeToString(resBody.bytes(), Base64.DEFAULT);
//            return resBody.string();
        }
    }

    /**
     * Request disconnect forcibly.
     *
     * @return Successfuly requested or not.
     */
    public boolean forceDisconnect() {

        // Check states.
        if(!checkInitialized() || !checkNotClosed())
            return false;

        // Emit event.
        Log.i(LOG_TAG, "force-disconnect");
        socketIo.emit("force-disconnect");

        return true;
    }

    /**
     * Make payload base.
     *
     * @return payload.
     * @throws JSONException When error while construct JSON.
     */
    private JSONObject makePayload() throws JSONException {

        JSONObject payload = new JSONObject();

        // Get UNIX time.
        long unixTime = Util.getUnixTime();

        // Put id.
        String id = String.format("%s%s%s%s-%s",
                applicationId,
                clientId,
                userId,
                deviceId,
                unixTime);

        payload.put("id", id);

        // Put header.
        JSONObject head = new JSONObject();
        head.put("applicationId", applicationId);
        head.put("applicationSecret", applicationSecret);
        head.put("clientId", clientId);
        head.put("userId", userId);
        head.put("deviceId", deviceId);
        head.put("timestampUnixTime", unixTime);

        payload.put("head", head);

        return payload;
    }

    /**
     * Checks whether connection is initialized.
     */
    private boolean checkInitialized() {

        if(!initialized) {
            Log.w(LOG_TAG, "Not initialized yet.");
            return false;
        }

        return true;
    }

    /**
     * Checks whether joined.
     */
    private boolean checkJoined() {

        if(!joined) {
            Log.w(LOG_TAG, "Not joined yet.");
            return false;
        }

        return true;
    }

    /**
     * Checks whether connection is not closed.
     */
    private boolean checkNotClosed() {

        if(closed) {
            Log.w(LOG_TAG, "Already closed.");
            return false;
        }

        return true;
    }

    /**
     * Gets whether connection is closed or not.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Close connection.
     */
    @Override
    public void close() {

        if(closed)
            return;

        socketIo.close();
        socketIo = null;

        closed = true;
    }
}
