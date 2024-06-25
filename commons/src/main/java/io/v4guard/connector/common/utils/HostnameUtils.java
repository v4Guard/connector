package io.v4guard.connector.common.utils;

import io.v4guard.connector.common.compatibility.DockerDetector;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

public class HostnameUtils {

    public static String detectHostname() {
        if (DockerDetector.isRunningInsideDocker()){
            return "Docker Container";
        } else try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }

    public static String detectVirtualHost(String virtualHost, boolean anonVirtualHost) {
        String host = virtualHost;
        try {
            URL hostParsed = new URL(virtualHost);

            if (hostParsed.getHost() != null) {
                if (anonVirtualHost) {
                    return "***" + hostParsed.getHost().substring(hostParsed.getHost().lastIndexOf('.') + 1);
                }

                host = hostParsed.getHost();
            }

        } catch (Exception ignored) {}

        return host;
    }

}
