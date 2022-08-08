package io.v4guard.plugin.core.check;

import io.v4guard.plugin.bungee.v4GuardBungee;
import io.v4guard.plugin.core.utils.CheckStatus;
import io.v4guard.plugin.core.utils.StringUtils;
import org.bson.Document;

import java.util.*;

public class CheckManager {

    private final HashMap<String, CheckStatus> checkStatusMap;
    private final List<CheckProcessor> processors;

    public CheckManager() {
        this.checkStatusMap = new HashMap<>();
        this.processors = new ArrayList<>();
        scheduleExpirationTask();
    }

    public void scheduleExpirationTask(){
        new Timer().schedule(new TimerTask(){
            @Override
            public void run() {
                Iterator<Map.Entry<String, CheckStatus>> it = checkStatusMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, CheckStatus> entry = it.next();
                    CheckStatus checkStatus = entry.getValue();
                    if (checkStatus.hasExpired()) {
                        checkStatus.makeCheckExpire();
                    }
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

    public HashMap<String, CheckStatus> getCheckStatusMap() {
        return checkStatusMap;
    }

    public CheckStatus getCheckStatus(String username) {
        return checkStatusMap.get(username);
    }

    public void runPreLoginCheck(String name, Object e) {
        for (CheckProcessor processor : processors) {
            processor.onPreLogin(name, e);
        }
    }

    public void runLoginCheck(String name, Object e) {
        for (CheckProcessor processor : processors) {
            processor.onLogin(name,  e);
        }
    }

    public void runPostLoginCheck(String name, Object e) {
        for (CheckProcessor processor : processors) {
            processor.onPostLogin(name, e);
        }
    }

    public CheckStatus buildCheckStatus(String username){
        Document kickMessages = (Document) v4GuardBungee.getCoreInstance().getBackendConnector().getSettings().get("messages");
        String kickReasonMessage = StringUtils.buildMultilineString((List<String>) kickMessages.get("kick"));
        return new CheckStatus(username, kickReasonMessage, false);
    }
}
