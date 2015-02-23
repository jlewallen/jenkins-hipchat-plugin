package jenkins.plugins.hipchat;

public class HipChatNotifierBuilder {
    private String token = "token";
    private String room = "room";
    private boolean startNotification = false;
    private boolean notifySuccess = false;
    private boolean notifyAborted = true;
    private boolean notifyNotBuilt = false;
    private boolean notifyUnstable = true;
    private boolean notifyFailure = true;
    private boolean notifyBackToNormal = true;
    private String messageJobStarted;
    private String messageJobCompleted;

    public static HipChatNotifierBuilder notifier() {
        return new HipChatNotifierBuilder();
    }

    public HipChatNotifierBuilder setToken(String token) {
        this.token = token;
        return this;
    }

    public HipChatNotifierBuilder setRoom(String room) {
        this.room = room;
        return this;
    }

    public HipChatNotifierBuilder setStartNotification(boolean startNotification) {
        this.startNotification = startNotification;
        return this;
    }

    public HipChatNotifierBuilder setNotifySuccess(boolean notifySuccess) {
        this.notifySuccess = notifySuccess;
        return this;
    }

    public HipChatNotifierBuilder setNotifyAborted(boolean notifyAborted) {
        this.notifyAborted = notifyAborted;
        return this;
    }

    public HipChatNotifierBuilder setNotifyNotBuilt(boolean notifyNotBuilt) {
        this.notifyNotBuilt = notifyNotBuilt;
        return this;
    }

    public HipChatNotifierBuilder setNotifyUnstable(boolean notifyUnstable) {
        this.notifyUnstable = notifyUnstable;
        return this;
    }

    public HipChatNotifierBuilder setNotifyFailure(boolean notifyFailure) {
        this.notifyFailure = notifyFailure;
        return this;
    }

    public HipChatNotifierBuilder setNotifyBackToNormal(boolean notifyBackToNormal) {
        this.notifyBackToNormal = notifyBackToNormal;
        return this;
    }

    public HipChatNotifier createHipChatNotifier() {
        return new HipChatNotifier(token, room, startNotification, notifySuccess, notifyAborted, notifyNotBuilt,
                notifyUnstable, notifyFailure, notifyBackToNormal, messageJobStarted, messageJobCompleted);
    }

    public HipChatNotifierBuilder setMessageJobStarted(String messageJobStarted) {
        this.messageJobStarted = messageJobStarted;
        return this;
    }

    public HipChatNotifierBuilder setMessageJobCompleted(String messageJobCompleted) {
        this.messageJobCompleted = messageJobCompleted;
        return this;
    }
}