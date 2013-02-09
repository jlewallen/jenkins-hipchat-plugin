package jenkins.plugins.hipchat;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.CauseAction;
import hudson.model.Result;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.ChangeLogSet.Entry;
import org.apache.commons.lang.StringUtils;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.logging.Logger;

@SuppressWarnings("rawtypes")
public class ActiveNotifier implements FineGrainedNotifier {

    private static final Logger logger = Logger.getLogger(HipChatListener.class.getName());

    HipChatNotifier notifier;
    MustacheFactory mustacheFactory;

    public ActiveNotifier(HipChatNotifier notifier) {
        super();
        this.notifier = notifier;
        this.mustacheFactory = new DefaultMustacheFactory();
    }

    private HipChatService getHipChat(AbstractBuild r) {
        AbstractProject<?, ?> project = r.getProject();
        String projectRoom = Util.fixEmpty(project.getProperty(HipChatNotifier.HipChatJobProperty.class).getRoom());
        return notifier.newHipChatService(projectRoom);
    }

    public void deleted(AbstractBuild r) {
    }

    public void started(AbstractBuild build) {
        String changes = getChanges(build);
        CauseAction cause = build.getAction(CauseAction.class);

        Map<String, Object> messageParams = new HashMap<String,Object>();
        messageParams.put("build", build);
        messageParams.put("cause", cause);
        messageParams.put("changes", changes);
        messageParams.put("link", getOpenLink(build));

        if (notifier.getMessageTemplateStarted() == null || "".equals(notifier.getMessageTemplateStarted()))
        {
            logger.warning("Started message template is not set, using default");
            notifier.setMessageTemplateStarted("{{build.project.displayName}} - {{build.displayName}}: {{#cause}}{{cause.shortDescription}}{{/cause}} {{#changes}}{{changes}}{{/changes}} {{{link}}}");
        }

        notifyStart(build, applyMessageTemplate(notifier.getMessageTemplateStarted(), messageParams));
    }

    private void notifyStart(AbstractBuild build, String message) {
        getHipChat(build).publish(message, "green");
    }

    public void finalized(AbstractBuild r) {
    }

    public void completed(AbstractBuild r) {

        AbstractProject<?, ?> project = r.getProject();
        HipChatNotifier.HipChatJobProperty jobProperty = project.getProperty(HipChatNotifier.HipChatJobProperty.class);
        Result result = r.getResult();
        if ((result == Result.ABORTED && jobProperty.getNotifyAborted())
                || (result == Result.FAILURE && jobProperty.getNotifyFailure())
                || (result == Result.NOT_BUILT && jobProperty.getNotifyNotBuilt())
                || (result == Result.SUCCESS && jobProperty.getNotifySuccess())
                || (result == Result.UNSTABLE && jobProperty.getNotifyUnstable())) {

            if (notifier.getMessageTemplateCompleted() == null || "".equals(notifier.getMessageTemplateCompleted()))
            {
                logger.warning("Completed message template is not set, using default");
                notifier.setMessageTemplateCompleted("{{build.project.displayName}} - {{build.displayName}}: {{status}} after {{build.durationString}} {{{link}}}");
            }

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
        StringBuilder message = new StringBuilder();
        message.append("Started by changes from ");
        message.append(StringUtils.join(authors, ", "));
        message.append(" (");
        message.append(files.size());
        message.append(" file(s) changed)");
        return message.toString();
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

    String applyMessageTemplate(String messageTemplate, Map<String,Object> messageParams)
    {
        StringWriter messageWriter = new StringWriter();

        String actualTemplate = messageTemplate;
        if (notifier.getMessageTemplateSuffix() != null)
        {
            actualTemplate += " " + notifier.getMessageTemplateSuffix();
        }

        Mustache mustache = this.mustacheFactory.compile(new StringReader(actualTemplate), "message");
        mustache.execute(messageWriter, messageParams);
        return messageWriter.toString();
    }

    String getBuildStatusMessage(AbstractBuild r) {
        Map<String,Object> messageParams = new HashMap<String, Object>();
        messageParams.put("build", r);
        messageParams.put("status", getStatusMessage(r));
        messageParams.put("link", getOpenLink(r));

        return applyMessageTemplate(notifier.getMessageTemplateCompleted(), messageParams);
    }

    private String getStatusMessage(AbstractBuild r) {
        if (r.isBuilding()) {
            return "Starting...";
        }
        Result result = r.getResult();
        if (result == Result.SUCCESS) return "Success";
        if (result == Result.FAILURE) return "<b>FAILURE</b>";
        if (result == Result.ABORTED) return "ABORTED";
        if (result == Result.NOT_BUILT) return "Not built";
        if (result == Result.UNSTABLE) return "Unstable";
        return "Unknown";
    }

    private String getOpenLink(AbstractBuild build)
    {
        String url = notifier.getBuildServerUrl() + build.getUrl();
        return new StringBuilder("(<a href='").append(url).append("'>Open</a>)").toString();
    }
}
