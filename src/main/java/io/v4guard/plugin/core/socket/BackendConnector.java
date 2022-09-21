
package io.v4guard.plugin.core.socket;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.v4guard.plugin.core.socket.listener.*;
import io.v4guard.plugin.core.v4GuardCore;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;

public class BackendConnector {

    private Socket socket;
    private IO.Options options;
    private SocketStatus socketStatus = SocketStatus.DISCONNECTED;
    private final Runtime runtime = Runtime.getRuntime();
    private HashMap<String, Object> settings;
    private boolean reconnected = false;
    private String authCode;

    private HashMap<String, Emitter.Listener> registeredListeners = new HashMap<>();

    public BackendConnector() throws IOException, URISyntaxException {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("v4g-version", Collections.singletonList(v4GuardCore.pluginVersion));
        headers.put("v4g-hostname", Collections.singletonList(getHostname()));
        headers.put("v4g-name", Collections.singletonList(new File(System.getProperty("user.dir")).getName()));
        headers.put("v4g-service", Collections.singletonList("minecraft"));
        headers.put("v4g-mode", Collections.singletonList(v4GuardCore.getInstance().getPluginMode().name()));
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
                .setAuth(getSecretKey())
                .build();
        this.socket = IO.socket("wss://connector.v4guard.io/minecraft", this.options);
        new Thread(() -> this.socket.connect()).start();
        registerListener("connect", new ConnectListener(this));
        if(v4GuardCore.getInstance().isDebugEnabled()){
            registerListener("connect_error", args -> v4GuardCore.getInstance().getLogger().log(Level.SEVERE,"An error occurred while attempting to contact server: " + Arrays.toString(args)));
        }
    }

    @NotNull
    private Map<String, String> getSecretKey() throws IOException {
        if (new File(v4GuardCore.getInstance().getDataFolder(), "vpn.key").exists()){
            return Collections.singletonMap("secret_key", Files.readAllLines(Paths.get(v4GuardCore.getInstance().getDataFolder() + "/vpn.key")).get(0));
        } else {
            return Collections.singletonMap("secret_key", null);
        }
    }

    public void handleEvents() {
        registerListener("auth", new AuthListener(this));
        registerListener("settings", new SettingsListener(this));
        registerListener("setting", new SettingListener(this));
        registerListener("console", new ConsoleListener());
        //registerListener("ipset", new IPSetListener(this));
        registerListener("check", new CheckListener());
        registerListener("message", new MessageListener(this));
        registerListener("kick", new KickListener());
    }

    public void registerListener(String event, Emitter.Listener listener){
        if(this.registeredListeners.containsKey(event)){
            this.socket.off(event);
        }
        this.registeredListeners.put(event, listener);
        this.socket.on(event, listener);
    }

    public SocketStatus getSocketStatus() {
        return this.socketStatus;
    }

    public HashMap<String, Object> getSettings() {
        return this.settings;
    }

    public void send(String event, Document payload) {
        this.socket.emit(event, payload.toJson());
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public IO.Options getOptions() {
        return options;
    }

    public void setSocketStatus(SocketStatus socketStatus) {
        this.socketStatus = socketStatus;
    }

    public void setSettings(HashMap<String, Object> settings) {
        this.settings = settings;
    }

    public boolean isReconnected() {
        return reconnected;
    }

    public void setReconnected(boolean reconnected) {
        this.reconnected = reconnected;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public Runtime getRuntime() {
        return runtime;
    }

    public String getHostname(){
        if(isRunningInsideDocker()){
            return "Docker Container";
        } else {
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                return "Unknown";
            }
        }
    }

    public static Boolean isRunningInsideDocker() {
        try (Stream<String> stream = Files.lines(Paths.get("/proc/1/cgroup"))) {
            return stream.anyMatch(line -> line.contains("/docker"));
        } catch (IOException e) {
            return false;
        }
    }
}