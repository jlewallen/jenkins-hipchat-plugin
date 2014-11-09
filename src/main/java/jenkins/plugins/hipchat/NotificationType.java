package jenkins.plugins.hipchat;

import hudson.model.AbstractBuild;
import hudson.model.CauseAction;
import hudson.scm.ChangeLogSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

public enum NotificationType {

    STARTED("green") {

                @Override
                protected String getStatusMessage(AbstractBuild<?, ?> build) {
                    String changes = getChanges(build);
                    CauseAction cause = build.getAction(CauseAction.class);
                    if (changes != null) {
                        return changes;
                    } else if (cause != null) {
                        MessageBuilder message = new MessageBuilder(build);
                        message.append(cause.getShortDescription());
                        return message.appendOpenLink().toString();
                    } else {
                        return super.getStatusMessage(build);
                    }
                }
            },
    ABORTED("gray"),
    SUCCESS("green"),
    FAILURE("red"),
    NOT_BUILT("gray"),
    BACK_TO_NORMAL("green"),
    UNSTABLE("yellow"),
    UNKNOWN("purple");
    private static final Logger logger = Logger.getLogger(NotificationType.class.getName());
    private final String color;

    private NotificationType(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    protected String getStatusMessage(AbstractBuild<?, ?> build) {
        MessageBuilder message = new MessageBuilder(build);
        message.appendStatusMessage();
        message.appendDuration();
        return message.appendOpenLink().toString();
    }

    private static String getChanges(AbstractBuild<?, ?> build) {
        if (!build.hasChangeSetComputed()) {
            logger.log(Level.FINE, "No change set computed for job {0}", build.getProject().getFullDisplayName());
            return null;
        }

        Set<String> authors = new HashSet<String>();
        int changedFiles = 0;
        ChangeLogSet changeSet = build.getChangeSet();
        for (Object o : changeSet.getItems()) {
            ChangeLogSet.Entry entry = (ChangeLogSet.Entry) o;
            logger.log(Level.FINER, "Entry {0}", entry);
            authors.add(entry.getAuthor().getDisplayName());
            try {
                changedFiles += entry.getAffectedFiles().size();
            } catch (UnsupportedOperationException e) {
                logger.log(Level.INFO, "Unable to collect the affected files for job {0}",
                        build.getProject().getFullDisplayName());
                return null;
            }
        }
        if (changedFiles == 0) {
            logger.finer("No changes detected");
            return null;
        }

        MessageBuilder message = new MessageBuilder(build);
        message.append("Started by changes from ");
        message.append(StringUtils.join(authors, ", "));
        message.append(" (");
        message.append(changedFiles);
        message.append(" file(s) changed)");
        return message.appendOpenLink().toString();
    }
}
