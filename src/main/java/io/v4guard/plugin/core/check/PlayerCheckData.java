package io.v4guard.plugin.core.check;

import io.v4guard.plugin.core.CoreInstance;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class PlayerCheckData {
    private ConcurrentLinkedQueue<CallbackTask> futureCallbackTasks;

    private Consumer<Exception>  whenCompleted;

    private CheckStatus checkStatus;
    private String username;
    private String address;
    private int version;
    private String virtualHost;

    private boolean waitMode;
    private boolean started;

    private String kickReason;
    private BlockReason blockReason;

    public PlayerCheckData(String username, String address, int version, String virtualHost, boolean waitMode) {
        this.username = username;
        this.address = address;
        this.version = version;
        this.virtualHost = virtualHost;
        this.waitMode = waitMode;
        this.kickReason = "Disconnected";
        this.blockReason = BlockReason.NONE;
        this.checkStatus = CheckStatus.WAITING;
        this.futureCallbackTasks = new ConcurrentLinkedQueue<>();
    }

    public void addTask(CallbackTask callbackTask) {
        this.futureCallbackTasks.add(callbackTask);
    }

    public void startChecking() {
        if (this.futureCallbackTasks.isEmpty()) {
            return;
        }

        this.started = true;

        this.getCurrentTask().start();
    }


    public void whenCompleted(Consumer<Exception> consumer) {
        this.whenCompleted = consumer;
    }

    public void triggerTaskCompleted() {
        CallbackTask currentTask = this.futureCallbackTasks.poll();

        if (this.checkStatus == CheckStatus.USER_DENIED) {
            handleWhenCompleted(null);
        } else if (currentTask != null) {
            try {
                currentTask.start();
            } catch (Exception exception) {
                handleWhenCompleted(exception);
            }
        } else {
            handleWhenCompleted(null);
        }
    }

    private CallbackTask getCurrentTask() {
        return this.futureCallbackTasks.peek();
    }

    public void handleWhenCompleted(Exception exception) {
        if (!this.started) {
            return;
        }

        this.futureCallbackTasks.clear();

        this.started = false;

        this.whenCompleted.accept(exception);
    }

    public CheckStatus getCheckStatus() {
        return this.checkStatus;
    }

    public void setCheckStatus(CheckStatus checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getKickReason() {
        return this.kickReason;
    }

    public void setKickReason(String kickReason) {
        this.kickReason = kickReason;
    }

    public BlockReason getBlockReason() {
        return this.blockReason;
    }

    public void setBlockReason(BlockReason blockReason) {
        this.blockReason = blockReason;
    }

    public String getUsername() {
        return this.username;
    }

    public String getAddress() {
        return this.address;
    }

    public int getVersion() {
        return this.version;
    }

    public String getVirtualHost() {
        return this.virtualHost;
    }

    public boolean isWaitMode() {
        return this.waitMode;
    }


}
