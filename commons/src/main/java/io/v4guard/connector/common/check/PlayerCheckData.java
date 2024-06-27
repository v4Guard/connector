package io.v4guard.connector.common.check;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class PlayerCheckData {
    private final ConcurrentLinkedQueue<CallbackTask> futureCallbackTasks;
    private CallbackTask currentTask;

    private Consumer<Exception> whenCompleted;

    private CheckStatus checkStatus;
    private final String username;
    private final String address;
    private final int version;
    private final String virtualHost;

    private final boolean waitMode;
    private boolean active;
    private final boolean bedrock;
    private boolean playerSettingsChecked;
    private boolean playerBrandChecked;

    private String kickReason;
    private BlockReason blockReason;

    private final long createdAt;

    public PlayerCheckData(String username, String address, int version, String virtualHost, boolean waitMode, boolean bedrock) {
        this.username = username;
        this.address = address;
        this.version = version;
        this.virtualHost = virtualHost;
        this.waitMode = waitMode;
        this.bedrock = bedrock;
        this.playerSettingsChecked = false;
        this.playerBrandChecked = false;
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

    public void triggerCompletedIfExpired() {
        if (!this.active) {
            return;
        }

        if (this.currentTask.isExpired()) {
            this.checkStatus = CheckStatus.EXPIRED;
            triggerTaskCompleted();
        }
    }

    private void handleWhenCompleted(Exception exception) {
        if (!this.active) {
            return;
        }

        this.futureCallbackTasks.clear();

        this.currentTask = null;
        this.active = false;

        this.whenCompleted.accept(exception);
    }

    private void handleWhenCompleted() {
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

    public void setPlayerSettingsChecked(boolean playerSettingsChecked) {
        this.playerSettingsChecked = playerSettingsChecked;
    }

    public void setPlayerBrandChecked(boolean playerBrandChecked) {
        this.playerBrandChecked = playerBrandChecked;
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

    public boolean isBedrock() {
        return this.bedrock;
    }

    public boolean isPlayerSettingsChecked() {
        return this.playerSettingsChecked;
    }

    public boolean isPlayerBrandChecked() {
        return this.playerBrandChecked;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

}
