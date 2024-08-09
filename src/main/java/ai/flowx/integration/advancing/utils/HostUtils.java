package ai.flowx.integration.advancing.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

@Slf4j
public class HostUtils {

    public static HostUtils INSTANCE = new HostUtils();
    private String hostname;

    public static String getCurrentHostname() {
        return INSTANCE.getHostname();
    }

    private String getHostname(){
        if (StringUtils.isEmpty(hostname)) {
            initHostname();
        }
        return hostname;
    }

    private void initHostname() {
        String unixCommand = "hostname";
        try {
            hostname = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(unixCommand).getInputStream())).lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.warn("Could not obtain result of unix command '" + unixCommand + "' because " + e.getClass().getName() + ": " + e.getMessage());
        }

        //obtain the K8s namespace, if running in k8s
        String filePath = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";
        try {
            String namespace = Files.readString(Path.of(filePath)).trim();
            hostname = namespace + ":" + hostname;
        } catch (Exception e) {
            log.warn("Could not obtain current namespace name from file " + filePath + " because " + e.getClass().getName() + ": " + e.getMessage());
        }

    }

}
