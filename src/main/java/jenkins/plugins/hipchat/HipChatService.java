package jenkins.plugins.hipchat;

import hudson.model.AbstractBuild;

public interface HipChatService {

    void publish(NotificationType notificationType, AbstractBuild<?, ?> build);

    void publish(String message, String color);

    void publish(String message, String color, boolean notify);
}
