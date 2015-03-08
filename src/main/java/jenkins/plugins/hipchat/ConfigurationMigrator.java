package jenkins.plugins.hipchat;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.listeners.ItemListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.plugins.hipchat.HipChatNotifier.HipChatJobProperty;

@Extension
public class ConfigurationMigrator extends ItemListener {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationMigrator.class.getName());

    @Override
    public void onLoaded() {
        for (AbstractProject<?, ?> item : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
            HipChatJobProperty property = item.getProperty(HipChatJobProperty.class);
            if (property != null) {
                HipChatNotifier notifier = item.getPublishersList().get(HipChatNotifier.class);
                if (notifier != null) {
                    notifier.setRoom(property.getRoom());
                    notifier.setStartNotification(property.getStartNotification());
                    notifier.setNotifyAborted(property.getNotifyAborted());
                    notifier.setNotifyBackToNormal(property.getNotifyBackToNormal());
                    notifier.setNotifyFailure(property.getNotifyFailure());
                    notifier.setNotifyNotBuilt(property.getNotifyNotBuilt());
                    notifier.setNotifySuccess(property.getNotifySuccess());
                    notifier.setNotifyUnstable(property.getNotifyUnstable());
                }
                try {
                    item.removeProperty(HipChatJobProperty.class);
                    LOGGER.log(Level.INFO, "Successfully migrated project configuration for build job: {0}",
                            item.getFullDisplayName());
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, "An error occurred while trying to update job configuration for "
                            + item.getName(), ioe);
                }
            }
        }
    }
}
