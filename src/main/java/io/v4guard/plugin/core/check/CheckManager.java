package io.v4guard.plugin.core.check;

import io.v4guard.plugin.core.check.common.CheckStatus;
import io.v4guard.plugin.core.check.common.VPNCheck;
import io.v4guard.plugin.core.socket.SocketStatus;
import io.v4guard.plugin.core.utils.StringUtils;
import io.v4guard.plugin.core.v4GuardCore;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CheckManager {

    private final ConcurrentHashMap<String, VPNCheck> checkStatusMap;
    private final List<CheckProcessor> processors;

    public CheckManager() {
        this.checkStatusMap = new ConcurrentHashMap<>();
        this.processors = new ArrayList<>();
        scheduleExpirationTask();
    }

    public void scheduleExpirationTask(){
        new Timer().schedule(new TimerTask(){
            @Override
            public void run() {
                try {
                    Iterator<Map.Entry<String, VPNCheck>> it = checkStatusMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, VPNCheck> entry = it.next();
                        VPNCheck check = entry.getValue();
                        if (check != null && (check.hasExpired() || check.hasPostExpired())) {
                            check.performActionOnExpire();
                        }
                    }
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }, 200L, 200L);
    }

    public CheckProcessor getProcessorByClass(Class clazz) {
        for (CheckProcessor processor : processors) {
            if (processor.getClass().equals(clazz)) {
                return processor;
            }
        }
        return null;
    }

    public void addProcessor(CheckProcessor processor) {
        this.processors.add(processor);
    }

    public List<CheckProcessor> getProcessors() {
        return processors;
    }

    public ConcurrentHashMap<String, VPNCheck> getCheckStatusMap() {
        return checkStatusMap;
    }

    public VPNCheck getCheckStatus(String username) {
        return checkStatusMap.get(username);
    }

    public void runPreLoginCheck(String name, Object e) {
        if (!v4GuardCore.getInstance().getBackendConnector().getSocketStatus().equals(SocketStatus.AUTHENTICATED)) {
            return;
        }
        for (CheckProcessor processor : processors) {
            processor.onPreLogin(name, e);
        }
    }

    public void runPostLoginCheck(String name, Object e) {
        for (CheckProcessor processor : processors) {
            processor.onPostLogin(name, e);
        }
    }

    public VPNCheck buildCheckStatus(String username, String hostname){
        Document kickMessages = (Document) v4GuardCore.getInstance().getBackendConnector().getSettings().get("messages");
        String kickReasonMessage = StringUtils.buildMultilineString((List<String>) kickMessages.get("kick"));
        return new VPNCheck(username, hostname, kickReasonMessage, false);
    }

    public void cleanupChecks(String username){
        Iterator<Map.Entry<String, VPNCheck>> it = checkStatusMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, VPNCheck> entry = it.next();
            VPNCheck check = entry.getValue();
            if(check.getName().equals(username)){
                check.setStatus(CheckStatus.FINISHED);
                checkStatusMap.remove(entry.getKey());
            }
        }
    }
}
