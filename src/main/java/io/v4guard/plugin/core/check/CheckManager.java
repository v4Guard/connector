package io.v4guard.plugin.core.check;

import io.v4guard.plugin.core.utils.CheckStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CheckManager {

    private final HashMap<String, CheckStatus> checkStatusMap;
    private final List<CheckProcessor> processors;

    public CheckManager() {
        this.checkStatusMap = new HashMap<>();
        this.processors = new ArrayList<>();
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
}
