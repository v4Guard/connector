package io.v4guard.plugin.core.utils;

import com.google.common.net.InternetDomainName;
import io.v4guard.plugin.core.compatibility.DockerDetector;

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
        String mainHost = virtualHost;

        try {
            mainHost = InternetDomainName.from(virtualHost).topPrivateDomain().toString();
        } catch (Exception ex) { /* ignore: this is not a domain */ }

        if (anonVirtualHost && !mainHost.equals(virtualHost)) {
            virtualHost = "***." + InternetDomainName.from(virtualHost).topPrivateDomain();
        }

        return virtualHost;
    }

}
