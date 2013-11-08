package jenkins.plugins.hipchat;

import hudson.Util;
import hudson.model.*;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.ChangeLogSet.Entry;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@SuppressWarnings("rawtypes")
public class ActiveNotifier implements FineGrainedNotifier {

    private static final Logger logger = Logger.getLogger(HipChatListener.class.getName());

    HipChatNotifier notifier;

    public ActiveNotifier(HipChatNotifier notifier) {
        super();
        this.notifier = notifier;
    }

    private HipChatService getHipChat(AbstractBuild r) {
        AbstractProject<?, ?> project = r.getProject();
        String projectRoom = Util.fixEmpty(project.getProperty(HipChatNotifier.HipChatJobProperty.class).getRoom());
        return notifier.newHipChatService(projectRoom);
    }

    public void deleted(AbstractBuild r) {
    }

    public void started(AbstractBuild build) {
        AbstractProject<?, ?> project = build.getProject();
        HipChatNotifier.HipChatJobProperty jobProperty = project.getProperty(HipChatNotifier.HipChatJobProperty.class);
        if (MessageBuilder.shouldNotify(jobProperty.getStartNotification(), jobProperty.getConditionalNotify(), build)) {
            getHipChat(build).publish(getBuildStatusMessage(build), "green");
        }
    }

    public void finalized(AbstractBuild r) {
    }

    public void completed(AbstractBuild r) {
        AbstractProject<?, ?> project = r.getProject();
        HipChatNotifier.HipChatJobProperty jobProperty = project.getProperty(HipChatNotifier.HipChatJobProperty.class);
        Result result = r.getResult();
        AbstractBuild<?, ?> previousBuild = project.getLastBuild().getPreviousBuild();
        Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
        if ((result == Result.ABORTED && MessageBuilder.shouldNotify(jobProperty.getNotifyAborted(), jobProperty.getConditionalNotify(), r))
                || (result == Result.FAILURE && MessageBuilder.shouldNotify(jobProperty.getNotifyFailure(), jobProperty.getConditionalNotify(), r))
                || (result == Result.NOT_BUILT && MessageBuilder.shouldNotify(jobProperty.getNotifyNotBuilt(), jobProperty.getConditionalNotify(), r))
                || (result == Result.SUCCESS && previousResult == Result.FAILURE && MessageBuilder.shouldNotify(jobProperty.getNotifyBackToNormal(), jobProperty.getConditionalNotify(), r))
                || (result == Result.SUCCESS && MessageBuilder.shouldNotify(jobProperty.getNotifySuccess(), jobProperty.getConditionalNotify(), r))
                || (result == Result.UNSTABLE && MessageBuilder.shouldNotify(jobProperty.getNotifyUnstable(), jobProperty.getConditionalNotify(), r))) {
            getHipChat(r).publish(getBuildStatusMessage(r), getBuildColor(r));
        }
    }

    String getChanges(AbstractBuild r) {
        if (!r.hasChangeSetComputed()) {
            logger.info("No change set computed...");
            return null;
        }
        ChangeLogSet changeSet = r.getChangeSet();
        List<Entry> entries = new LinkedList<Entry>();
        Set<AffectedFile> files = new HashSet<AffectedFile>();
        for (Object o : changeSet.getItems()) {
            Entry entry = (Entry) o;
            logger.info("Entry " + o);
            entries.add(entry);
            files.addAll(entry.getAffectedFiles());
        }
        if (entries.isEmpty()) {
            logger.info("Empty change...");
            return null;
        }
        Set<String> authors = new HashSet<String>();
        for (Entry entry : entries) {
            authors.add(entry.getAuthor().getDisplayName());
        }
        MessageBuilder message = new MessageBuilder(notifier, r);
        message.append("Started by changes from ");
        message.append(StringUtils.join(authors, ", "));
        message.append(" (");
        message.append(files.size());
        message.append(" file(s) changed)");
        return message.toString();
    }

    String getCulprits(AbstractBuild r) {
        Set<User> culprits = r.getCulprits();
        Set<String> culpritNames = new HashSet<String>();
        for (User culprit : culprits) {
            culpritNames.add(culprit.getFullName());
        }

        return "Who's to blame? " + StringUtils.join(culpritNames, ", ");
    }

    static String getBuildColor(AbstractBuild r) {
        Result result = r.getResult();
        if (result == Result.SUCCESS) {
            return "green";
        } else if (result == Result.FAILURE) {
            return "red";
        } else {
            return "yellow";
        }
    }

    String getBuildStatusMessage(AbstractBuild build) {
        String changes = getChanges(build);
        CauseAction cause = build.getAction(CauseAction.class);
        AbstractProject<?, ?> project = build.getProject();
        String customMessage = Util.fixEmpty(project.getProperty(HipChatNotifier.HipChatJobProperty.class).getCustomMessage());
        MessageBuilder message = new MessageBuilder(notifier, build);

        message.appendOpenLink().append("<br />");
        message.appendStatusMessage();
        if (!build.isBuilding()) message.appendDuration();

        if (changes != null) {
            message.append("<br />");
            message.append(changes);
        } else if (cause != null) {
            message.append("<br />");
            message.append(cause.getShortDescription());
        }

        if (build.getResult() == Result.FAILURE) {
            message.append("<br />");
            message.append(getCulprits(build));
        }

        return message.appendCustomMessage(customMessage, build).toString();
    }

    public static class MessageBuilder {
        private StringBuffer message;
        private HipChatNotifier notifier;
        private AbstractBuild build;

        public MessageBuilder(HipChatNotifier notifier, AbstractBuild build) {
            this.notifier = notifier;
            this.message = new StringBuffer();
            this.build = build;
            startMessage();
        }

        public MessageBuilder appendStatusMessage() {
            message.append("Build status: ");
            message.append(getStatusMessage(build));
            return this;
        }

        static String getStatusMessage(AbstractBuild r) {
            if (r.isBuilding()) {
                return "Starting...";
            }
            Result result = r.getResult();
            Run previousBuild = r.getProject().getLastBuild().getPreviousBuild();
            Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
            if (result == Result.SUCCESS && previousResult == Result.FAILURE) return "Back to normal";
            if (result == Result.SUCCESS) return "Success";
            if (result == Result.FAILURE) return "<b>FAILURE</b>";
            if (result == Result.ABORTED) return "ABORTED";
            if (result == Result.NOT_BUILT) return "Not built";
            if (result == Result.UNSTABLE) return "Unstable";
            return "Unknown";
        }

        public MessageBuilder append(String string) {
            message.append(string);
            return this;
        }

        public MessageBuilder append(Object string) {
            message.append(string.toString());
            return this;
        }

        private MessageBuilder startMessage() {
            message.append(build.getProject().getDisplayName());
            message.append(" - ");
            message.append(build.getDisplayName());
            message.append(" ");
            return this;
        }

        public MessageBuilder appendOpenLink() {
            String url = notifier.getBuildServerUrl() + build.getUrl();
            message.append(" (<a href='").append(url).append("'>Open</a>)");
            return this;
        }

        public MessageBuilder appendDuration() {
            message.append(" after ");
            message.append(build.getDurationString());
            return this;
        }

        public MessageBuilder appendCustomMessage(String customMessage, AbstractBuild build) {
            if (customMessage != null) {
                message.append("<br />");
                message.append(getParameterString(customMessage, build));
            }

            return this;
        }

        public String toString() {
            return message.toString();
        }

        public static boolean shouldNotify(boolean jobNotifyProperty, String conditionalProperty, AbstractBuild r) {
            boolean blnConditionalProperty = true;
            conditionalProperty = MessageBuilder.getParameterString(conditionalProperty, r);
            if (conditionalProperty.equalsIgnoreCase("true") || conditionalProperty.equalsIgnoreCase("false")) {
                blnConditionalProperty = Boolean.valueOf(conditionalProperty);
            }
            return jobNotifyProperty && blnConditionalProperty;
        }

        public static String getParameterString(String original, AbstractBuild<?, ?> r) {
            ParametersAction parameters = r.getAction(ParametersAction.class);
            if (parameters != null) {
                original = parameters.substitute(r, original);
            }

            return original;
        }
    }
}
