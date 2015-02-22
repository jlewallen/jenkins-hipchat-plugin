package jenkins.plugins.hipchat;

import hudson.model.AbstractBuild;
import hudson.model.CauseAction;
import hudson.model.Result;
import hudson.scm.ChangeLogSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

public enum NotificationType {

    STARTED("green") {

                @Override
                protected String getStatusMessage(AbstractBuild<?, ?> build) {
                    String changes = getStatusMessageWithChanges(build);
                    if (changes != null) {
                        return changes;
                    } else {
                        CauseAction cause = build.getAction(CauseAction.class);
                        if (cause != null) {
                            return cause.getShortDescription();
                        } else {
                            return Messages.Starting();
                        }
                    }
                }
            },
    ABORTED("gray") {

                @Override
                protected String getStatusMessage(AbstractBuild<?, ?> build) {
                    return Messages.Aborted(build.getDurationString());
                }
            },
    SUCCESS("green") {

                @Override
                protected String getStatusMessage(AbstractBuild<?, ?> build) {
                    return Messages.Success(build.getDurationString());
                }
            },
    FAILURE("red") {

                @Override
                protected String getStatusMessage(AbstractBuild<?, ?> build) {
                    return Messages.Failure(build.getDurationString());
                }
            },
    NOT_BUILT("gray") {

                @Override
                protected String getStatusMessage(AbstractBuild<?, ?> build) {
                    return Messages.NotBuilt();
                }
            },
    BACK_TO_NORMAL("green") {

                @Override
                protected String getStatusMessage(AbstractBuild<?, ?> build) {
                    return Messages.BackToNormal(build.getDurationString());
                }
            },
    UNSTABLE("yellow") {

                @Override
                protected String getStatusMessage(AbstractBuild<?, ?> build) {
                    return Messages.Unstable(build.getDurationString());
                }
            },
    UNKNOWN("purple") {

                @Override
                protected String getStatusMessage(AbstractBuild<?, ?> build) {
                    throw new IllegalStateException("Unable to generate status message for UNKNOWN notification type");
                }
            };
    private static final Logger logger = Logger.getLogger(NotificationType.class.getName());
    private final String color;

    private NotificationType(String color) {
        this.color = color;
    }

    protected abstract String getStatusMessage(AbstractBuild<?, ?> build);

    public String getColor() {
        return color;
    }

    private static String getStatusMessageWithChanges(AbstractBuild<?, ?> build) {
        if (!build.hasChangeSetComputed()) {
            logger.log(Level.FINE, "No changeset computed for job {0}", build.getProject().getFullDisplayName());
            return null;
        }

        Set<String> authors = new HashSet<String>();
        int changedFiles = 0;
        for (Object o : build.getChangeSet().getItems()) {
            ChangeLogSet.Entry entry = (ChangeLogSet.Entry) o;
            logger.log(Level.FINEST, "Entry {0}", entry);
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
            logger.log(Level.FINE, "No changes detected");
            return null;
        }

        return Messages.StartWithChanges(StringUtils.join(authors, ", "), changedFiles);
    }

    public final String getMessage(AbstractBuild<?, ?> build) {
        String rootUrl = Jenkins.getInstance().getRootUrl();
        StringBuilder sb = new StringBuilder(150);
        sb.append(Messages.MessageStart(build.getProject().getDisplayName(), build.getDisplayName()));
        sb.append(' ');
        sb.append(getStatusMessage(build));

        sb.append(" (<a href=\"").append(rootUrl).append(build.getUrl()).append("\">Open</a>)");

        return sb.toString();
    }

    public static final NotificationType fromResults(Result previousResult, Result result) {
        if (result == Result.ABORTED) {
            return ABORTED;
        } else if (result == Result.FAILURE) {
            return FAILURE;
        } else if (result == Result.NOT_BUILT) {
            return NOT_BUILT;
        } else if (result == Result.UNSTABLE) {
            return UNSTABLE;
        } else if (result == Result.SUCCESS) {
            if (previousResult == Result.FAILURE) {
                return BACK_TO_NORMAL;
            } else {
                return SUCCESS;
            }
        }

        return UNKNOWN;
    }
}
