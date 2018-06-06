minarai client SDK (for Android)
====

## Description
A SDK that enables you to connect minarai easily on Android.


## Requirement
* [socket.io-client-java](https://github.com/socketio/socket.io-client-java)

## Installation

Add it as a gradle dependency for Android Studio, in build.gradle
```
repositories {
    ...
    maven {
        url "https://nextremer.github.io/minarai-client-sdk-android-maven/repository"
    }
}

dependencies {
	...
    implementation 'com.nextremer:minarai-client-sdk-android:0.1.0'
}
```

## Usage
```java
import com.nextremer.minarai.client.*;

...

final MinaraiClient client = new MinaraiClient(
        yourApplicationId,
        yourApplicationSercret,
        yourClientId,
        yourUserId,
        yourDeviceId);

client.init();

// binding events
client.on(MinaraiEvent.CONNECT, new MinaraiEventListener() {
    @Override
    public void onEvent(MinaraiEvent event, JSONObject data) {
        Log.i(LOG_TAG, "## socket.io connected. trying to join as minarai client");
    }
});
client.on(MinaraiEvent.JOINED, new MinaraiEventListener() {
    @Override
    public void onEvent(MinaraiEvent event, JSONObject data) {
        Log.i(LOG_TAG, "## minarai CONNECTED");
    }
});
client.on(MinaraiEvent.MESSAGE, new MinaraiEventListener() {
    @Override
    public void onEvent(MinaraiEvent event, JSONObject data) {
        Log.i(LOG_TAG, "recieve message: " + data);
    }
});
client.on(MinaraiEvent.ERROR, new MinaraiEventListener() {
    @Override
    public void onEvent(MinaraiEvent event, JSONObject data) {
        Log.i(LOG_TAG, "minarai client error: " + data);
    }
});

// send message "hello" to dialogue-hub every 3 seconds
TimerTask task = new TimerTask() {
    @Override
    public void run() {
        client.send("hello");
    }
};

new Timer().scheduleAtFixedRate(task, 0, 3000);
```

## References
### Constructor arguments
 * applicationId: application id to connect
 * applicationSecret: application secret to connect
 * clientId: clientId
 * userId: userId
 * deviceId: deviceId

### Methods
 * **send**: send message to minarai
   * arguments
     * uttr: String: message to send
     * options: MinaraiSendOptions: contains language, any extra data.
 * sendSystemCommand: send system command to minarai
   * arguments
     * command: String
     * payload: any JSONObject

```java
client.send("hello", new MinaraiSendOptions("ja-JP"));
client.sendSystemCommand("happyEmotionDetected", new JSONObject("{ value: true }"));
```

### Events
 * **CONNECT**: when connected to minarai successfully
 * **JOINED**: when signed in to minarai as client successfully
 * **DISCONNECTED**: when disconnected to minarai successfully
 * **SYNC**: when you or your group send message to minarai(for sync message between multiple devices)
 * **SYNC_SYSTEM_COMMAND**: when you or your group send system command to minarai(for sync system command between multiple devices)
 * **MESSAGE**: when minarai send any event


See the [Javadoc](https://github.com/Nextremer/minarai-client-sdk-android/apidocs/) for more details.