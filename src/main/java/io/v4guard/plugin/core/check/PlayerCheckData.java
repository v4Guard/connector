package io.v4guard.plugin.core.check;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class PlayerCheckData {
    private ConcurrentLinkedQueue<CallbackTask> futureCallbackTasks;
    private CallbackTask currentTask;

    private Consumer<Exception> whenCompleted;

    private CheckStatus checkStatus;
    private String username;
    private String address;
    private int version;
    private String virtualHost;

    private boolean waitMode;
    private boolean active;

    private String kickReason;
    private BlockReason blockReason;

    private long createdAt;

    public PlayerCheckData(String username, String address, int version, String virtualHost, boolean waitMode) {
        this.username = username;
        this.address = address;
        this.version = version;
        this.virtualHost = virtualHost;
        this.waitMode = /*waitMode*/true;
        this.kickReason = "Disconnected";
        this.blockReason = BlockReason.NONE;
        this.checkStatus = CheckStatus.WAITING;
        this.createdAt = System.currentTimeMillis();
        this.futureCallbackTasks = new ConcurrentLinkedQueue<>();
    }

    public void addTask(CallbackTask callbackTask) {
        this.futureCallbackTasks.add(callbackTask);
    }

    public void startChecking() {
        if (this.futureCallbackTasks.isEmpty()) {
            return;
        }

        this.currentTask = this.futureCallbackTasks.peek();
        this.active = true;

        this.getCurrentTask().start();
    }


    public void whenCompleted(Consumer<Exception> consumer) {
        this.whenCompleted = consumer;
    }

    public void triggerTaskCompleted() {
        if (this.checkStatus == CheckStatus.USER_DENIED) {
            handleWhenCompleted();
        } else {
            CallbackTask nextTask = this.futureCallbackTasks.poll();

            if (nextTask != null) {
                this.currentTask = nextTask;

                try {
                    nextTask.start();
                } catch (Exception exception) {
                    handleWhenCompleted(exception);
                }
            } else {
                handleWhenCompleted();
            }
        }
    }

    public CallbackTask getCurrentTask() {
        return this.currentTask;
    }

    public void handleWhenCompleted(Exception exception) {
        if (!this.active) {
            return;
        }

        this.futureCallbackTasks.clear();

        this.currentTask = null;
        this.active = false;

        this.whenCompleted.accept(exception);
    }

    public void handleWhenCompleted() {
        handleWhenCompleted(null);
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

    public boolean isActive() {
        return this.active;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }


}
