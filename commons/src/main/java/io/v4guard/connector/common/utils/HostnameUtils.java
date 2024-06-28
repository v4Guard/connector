package io.v4guard.connector.common.utils;

import com.google.common.net.InternetDomainName;
import io.v4guard.connector.common.compatibility.DockerDetector;

import java.net.InetAddress;
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
        String mainHost;

        try {
            mainHost = InternetDomainName.from(virtualHost).topPrivateDomain().toString();

            if (anonVirtualHost && !mainHost.equals(virtualHost)) {
                virtualHost = "***." + InternetDomainName.from(virtualHost).topPrivateDomain();
            }
        } catch (Exception ex) { /* failed to get doamin, return the original host */ }

        return virtualHost;
    }

}
