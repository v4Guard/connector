package io.v4guard.connector.common.socket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.constants.ConnectorConstants;
import io.v4guard.connector.common.constants.ListenersConstants;
import io.v4guard.connector.common.socket.listener.*;
import io.v4guard.connector.common.utils.HashCalculator;
import io.v4guard.connector.common.utils.HostnameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class Connection {

    private Socket socket;

    private IO.Options options;

    private SocketStatus socketStatus = SocketStatus.DISCONNECTED;

    private boolean reconnected = false;
    private final CoreInstance backend;

    private String authCode;

    private final HashMap<String, Emitter.Listener> registeredListeners = new HashMap<>();

    public Connection(CoreInstance coreInstance) {
        this.backend = coreInstance;
    }

    public void prepareAndConnect() {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put(ConnectorConstants.VERSION_HEADER, Collections.singletonList(CoreInstance.PLUGIN_VERSION));
        headers.put(ConnectorConstants.HOSTNAME_HEADER, Collections.singletonList(HostnameUtils.detectHostname()));
        headers.put(ConnectorConstants.INSTANCE_NAME_HEADER, Collections.singletonList(new File(System.getProperty("user.dir")).getName()));
        headers.put(ConnectorConstants.SERVICE_NAME_HEADER, Collections.singletonList("minecraft"));
        headers.put(ConnectorConstants.PLUGIN_MODE_HEADER, Collections.singletonList(backend.getPlatform().name()));
        headers.put(ConnectorConstants.INTEGRITY_HEADER, Collections.singletonList(HashCalculator.calculateHash()));

        this.options = IO.Options.builder()
                .setForceNew(false)
                .setMultiplex(true)
                .setTransports(new String[]{"websocket","polling"})
                .setUpgrade(true)
                .setRememberUpgrade(false)
                .setSecure(true)
                .setPath("/socket")
                .setQuery(null)
                .setExtraHeaders(headers)
                .setReconnection(true)
                .setReconnectionAttempts(Integer.MAX_VALUE)
                .setReconnectionDelay(1000L)
                .setReconnectionDelayMax(5000L)
                .setRandomizationFactor(0.5)
                .setTimeout(3000L)
                .build();

        connectToSocketWith(options);
    }

    public void reconnect() {
        socket.disconnect();
        connectToSocketWith(options);
    }

    private void connectToSocketWith(IO.Options options) {
        try {
            initializeSecretKey();
            this.socket = IO.socket("wss://connector.v4guard.io/minecraft", options);
            this.socket.connect();

            registerListener(ListenersConstants.EVENT_CONNECT, new ConnectListener(this));
            registerListener(ListenersConstants.EVENT_RECONNECT, new ReconnectListener(this));

            if (backend.isDebugEnabled()) {
                registerListener(
                        ListenersConstants.EVENT_CONNECT_ERROR
                        , args -> UnifiedLogger.get().log(Level.SEVERE, "An error occurred while attempting to contact server: " + Arrays.toString(args))
                );
            }
        } catch (URISyntaxException exception) {
            UnifiedLogger.get().log(Level.SEVERE, "An exception has occurred while connecting to the backend.", exception);
        }
    }

    private void initializeSecretKey() {
        try {
            File dataFolder = CoreInstance.get().getPlugin().getDataFolder();
            File oldFolder = dataFolder.getParentFile().toPath().resolve("v4Guard").toFile();
            File keyFile;

            if (oldFolder.exists()) {
                keyFile = new File(oldFolder, "vpn.key");

                if (keyFile.exists()) {
                    UnifiedLogger.get().info("Migrating old key to new location");

                    writeSecretKey(Files.readString(keyFile.toPath()));
                    Files.delete(keyFile.toPath());
                    Files.delete(oldFolder.toPath());
                }
            }

            keyFile = new File(dataFolder, "secret.key");
            String secretKey = null;

            if (keyFile.exists()){
                secretKey = Files.readString(keyFile.toPath());
            }

            options.auth = Collections.singletonMap(
                    "secret_key",
                    secretKey
            );
        } catch (IOException exception) {
            UnifiedLogger.get().log(Level.SEVERE, "An exception has occurred while reading secret key.", exception);
        }
    }

    public void writeSecretKey(String secretKey) throws IOException {
        File keyFile = new File(CoreInstance.get().getPlugin().getDataFolder(), "secret.key");

        if (!keyFile.exists()) {
            keyFile.getParentFile().mkdirs();
            keyFile.createNewFile();
        }

        Files.writeString(keyFile.toPath(), secretKey);
    }

    public void registerListener(String event, Emitter.Listener listener){
        if (this.registeredListeners.containsKey(event)) {
            this.socket.off(event);
        }

        this.registeredListeners.put(event, listener);
        this.socket.on(event, listener);
    }

    public void initializeListeners() {
        registerListener(ListenersConstants.EVENT_AUTH, new AuthListener(backend, this));
        registerListener(ListenersConstants.EVENT_SETTINGS, new SettingsListener(backend));
        registerListener(ListenersConstants.EVENT_SETTING, new SettingListener());
        registerListener(ListenersConstants.EVENT_CONSOLE, new ConsoleMessageListener());
        registerListener(ListenersConstants.EVENT_CHECK, new CheckListener());
        registerListener(ListenersConstants.EVENT_MESSAGE, new ChatMessageListener());
        registerListener(ListenersConstants.EVENT_KICK, new KickListener());
        registerListener(ListenersConstants.EVENT_CLEAN_CACHE, new CleanCacheListener(backend));
        registerListener(ListenersConstants.EVENT_FIND, new FindListener(backend));
    }

    public boolean isReady() {
        return socket != null
                && socket.connected()
                && socketStatus == SocketStatus.AUTHENTICATED
                && CoreInstance.get().getActiveSettings() != null;
    }

    public void send(String channel, ObjectNode payload) {
        try {
            this.socket.emit(channel, payload);
        } catch (IllegalArgumentException exception) {
            UnifiedLogger.get().log(Level.SEVERE, "An exception has occurred while sending a message to the backend.", exception);
        }
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public SocketStatus getSocketStatus() {
        return socketStatus;
    }

    public void setSocketStatus(SocketStatus socketStatus) {
        this.socketStatus = socketStatus;
    }

    public void disconnect() {
        socket.disconnect();
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public boolean isReconnected() {
        return reconnected;
    }

    public void setReconnected(boolean reconnected) {
        this.reconnected = reconnected;
    }

}
