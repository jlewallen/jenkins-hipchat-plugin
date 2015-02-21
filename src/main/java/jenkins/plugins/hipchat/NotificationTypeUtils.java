package jenkins.plugins.hipchat;

import com.google.common.collect.Sets;
import hudson.model.AbstractBuild;
import hudson.model.CauseAction;
import hudson.scm.ChangeLogSet;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import java.util.Set;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;


public final class NotificationTypeUtils {
    private static final Logger logger = Logger.getLogger(NotificationTypeUtils.class.getName());

    private NotificationTypeUtils() {
        /* utility method */
    }

    public static String getCause(AbstractBuild build) {
        CauseAction cause = build.getAction(CauseAction.class);
        if (cause != null) {
            return cause.getShortDescription();
        } else {
            return null;
        }
    }

    public static String getUrl(AbstractBuild build) {
        return Jenkins.getInstance().getRootUrl() + build.getUrl();
    }

    public static String getChanges(AbstractBuild build) {
        if (!build.hasChangeSetComputed()) {
            return null;
        }
        Set<String> authors = Sets.newHashSet();
        int changedFiles = 0;
        for (Object o : build.getChangeSet().getItems()) {
            ChangeLogSet.Entry entry = (ChangeLogSet.Entry) o;
            authors.add(entry.getAuthor().getDisplayName());
            try {
                changedFiles += entry.getAffectedFiles().size();
            } catch (UnsupportedOperationException e) {
                logger.log(INFO, "Unable to collect the affected files for job {0}",
                        build.getProject().getFullDisplayName());
                return null;
            }
        }
        if (changedFiles == 0) {
            return null;
        }

        return Messages.StartWithChanges(StringUtils.join(authors, ", "), changedFiles);
    }
}
