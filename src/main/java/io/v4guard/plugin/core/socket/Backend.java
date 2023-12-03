package io.v4guard.plugin.core.socket;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.UnifiedLogger;
import io.v4guard.plugin.core.constants.ConnectorConstants;
import io.v4guard.plugin.core.constants.ListenersConstants;
import io.v4guard.plugin.core.socket.listener.*;
import io.v4guard.plugin.core.utils.HashCalculator;
import io.v4guard.plugin.core.utils.HostnameUtils;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class Backend {

    private Socket socket;

    private IO.Options options;

    private SocketStatus socketStatus = SocketStatus.DISCONNECTED;

    private boolean reconnected = false;
    private CoreInstance coreInstance;

    private String authCode;

    private HashMap<String, Emitter.Listener> registeredListeners = new HashMap<>();

    public Backend(CoreInstance coreInstance) {
        this.coreInstance = coreInstance;
    }

    public void prepareAndConnect() {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put(ConnectorConstants.VERSION_HEADER, Collections.singletonList(CoreInstance.PLUGIN_VERSION));
        headers.put(ConnectorConstants.HOSTNAME_HEADER, Collections.singletonList(HostnameUtils.detectHostname()));
        headers.put(ConnectorConstants.INSTANCE_NAME_HEADER, Collections.singletonList(new File(System.getProperty("user.dir")).getName()));
        headers.put(ConnectorConstants.SERVICE_NAME_HEADER, Collections.singletonList("minecraft"));
        headers.put(ConnectorConstants.PLUGIN_MODE_HEADER, Collections.singletonList(coreInstance.getPlatform().name()));
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

            if (coreInstance.isDebugEnabled()) {
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
            // Migrate old key, kinda ugly but it works
            if (CoreInstance.get().getPlugin().getDataFolder().getParentFile().toPath().resolve("v4Guard").toFile().exists()) {
                File oldFolder = CoreInstance.get().getPlugin().getDataFolder().getParentFile().toPath().resolve("v4Guard").toFile();
                File keyFile = new File(oldFolder, "vpn.key");

                if (keyFile.exists()) {
                    UnifiedLogger.get().log(Level.INFO, "Migrating old key to new location");

                    writeSecretKey(Files.readString(keyFile.toPath()));
                    Files.delete(keyFile.toPath());
                    Files.delete(oldFolder.toPath());
                }
            }


            File keyFile = new File(CoreInstance.get().getPlugin().getDataFolder(), "secret.key");
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
        registerListener(ListenersConstants.EVENT_AUTH, new AuthListener(this));
        registerListener(ListenersConstants.EVENT_SETTINGS, new SettingsListener());
        registerListener(ListenersConstants.EVENT_SETTING, new SettingListener());
        registerListener(ListenersConstants.EVENT_CONSOLE, new ConsoleMessageListener());
        registerListener(ListenersConstants.EVENT_CHECK, new CheckListener());
        registerListener(ListenersConstants.EVENT_MESSAGE, new MessageListener());
        registerListener(ListenersConstants.EVENT_KICK, new KickListener());
        registerListener(ListenersConstants.EVENT_CLEAN_CACHE, new CleanCacheListener());
        registerListener(ListenersConstants.EVENT_FIND, new FindListener(this));
    }

    public boolean isReady() {
        return socket != null
                && RemoteSettings.hasData()
                && socket.connected()
                && socketStatus == SocketStatus.AUTHENTICATED;
    }

    public void send(String event, Document payload) {
        this.socket.emit(event, payload.toJson());
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

    public Socket getSocket() {
        return socket;
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
