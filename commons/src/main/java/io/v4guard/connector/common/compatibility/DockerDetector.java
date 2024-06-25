package io.v4guard.connector.common.compatibility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class DockerDetector {

    /**
     * DISCLAIMER: this method may trigger a malware warning for "Checking Debug Environment" for
     * reading system information from the /proc filesystem. -- This could always trigger the warning
     * because WE DO CHECK if it's on a virtual machine or not.
    */
    public static Boolean isRunningInsideDocker() {
        try (Stream<String> stream = Files.lines(Paths.get("/proc/1/cgroup"))) {
            return stream.anyMatch(line -> line.contains("/docker"));
        } catch (IOException e) {
            return false;
        }
    }

}
