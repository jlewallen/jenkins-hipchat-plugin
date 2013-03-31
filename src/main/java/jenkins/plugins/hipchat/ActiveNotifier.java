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

        String trigger;
        if (changes != null)
        {
            trigger = changes;
        }
        else if (cause != null)
        {
            trigger = cause.getShortDescription();
        }
        else
        {
            trigger = "Starting...";
        }

        Map<String, Object> messageParams = new HashMap<String,Object>();
        messageParams.put("build", build);
        messageParams.put("trigger", trigger);
        messageParams.put("link", getOpenLink(build));

        HipChatNotifier.HipChatJobProperty jobProperty = getJobPropertyForBuild(build);
        String messageTemplate = getMessageTemplate(jobProperty.getMessageTemplateStarted(),
                jobProperty.getMessageTemplateSuffix(),
                "{{build.project.displayName}} - {{build.displayName}}: {{trigger}} {{{link}}}");

        notifyStart(build, applyMessageTemplate(messageTemplate, messageParams));
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

            String messageTemplate = getMessageTemplate(jobProperty.getMessageTemplateCompleted(),
                    jobProperty.getMessageTemplateSuffix(),
                    "{{build.project.displayName}} - {{build.displayName}}: {{{status}}} after {{build.durationString}} {{{link}}}");

            Map<String,Object> messageParams = new HashMap<String, Object>();
            messageParams.put("build", r);
            messageParams.put("status", getStatusMessage(r));
            messageParams.put("link", getOpenLink(r));

            getHipChat(r).publish(applyMessageTemplate(messageTemplate, messageParams), getBuildColor(r));
        }

    }

    private HipChatNotifier.HipChatJobProperty getJobPropertyForBuild(AbstractBuild r)
    {
        AbstractProject<?, ?> project = r.getProject();
        return project.getProperty(HipChatNotifier.HipChatJobProperty.class);
    }

    private String getChanges(AbstractBuild r) {
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

    private String getMessageTemplate(String baseTemplate, String suffixTemplate, String defaultTemplate)
    {
        StringBuilder template = new StringBuilder();
        if (baseTemplate == null || StringUtils.isBlank(baseTemplate))
        {
            template.append(defaultTemplate);
        }
        if (suffixTemplate != null && StringUtils.isNotBlank(suffixTemplate)) {
            template.append(" ");
            template.append(suffixTemplate);
        }
        return template.toString();
    }


    String applyMessageTemplate(String messageTemplate, Map<String,Object> messageParams)
    {
        StringWriter messageWriter = new StringWriter();

        Mustache mustache = this.mustacheFactory.compile(new StringReader(messageTemplate), "message");
        mustache.execute(messageWriter, messageParams);
        return messageWriter.toString();
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
        StringBuilder builder = new StringBuilder("(<a href='");
        builder.append(notifier.getBuildServerUrl());
        builder.append(build.getUrl());
        builder.append("'>Open</a>)");
        return builder.toString();
    }
}
