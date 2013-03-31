package jenkins.plugins.hipchat;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

@SuppressWarnings({"unchecked"})
public class HipChatNotifier extends Notifier {

    private static final Logger logger = Logger.getLogger(HipChatNotifier.class.getName());

    private String authToken;
    private String buildServerUrl;
    private String room;
    private String sendAs;
    private String messageTemplateStarted;
    private String messageTemplateCompleted;
    private String messageTemplateSuffix;

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public String getRoom() {
        return room;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getBuildServerUrl() {
        return buildServerUrl;
    }

    public String getSendAs() {
        return sendAs;
    }

    public void setBuildServerUrl(final String buildServerUrl) {
        this.buildServerUrl = buildServerUrl;
    }

    public void setAuthToken(final String authToken) {
        this.authToken = authToken;
    }

    public void setRoom(final String room) {
        this.room = room;
    }

    public void setSendAs(final String sendAs) {
        this.sendAs = sendAs;
    }

    public String getMessageTemplateStarted() {
        return messageTemplateStarted;
    }

    public void setMessageTemplateStarted(String messageTemplateStarted) {
        this.messageTemplateStarted = messageTemplateStarted;
    }

    public String getMessageTemplateCompleted() {
        return messageTemplateCompleted;
    }

    public void setMessageTemplateCompleted(String messageTemplateCompleted) {
        this.messageTemplateCompleted = messageTemplateCompleted;
    }

    public String getMessageTemplateSuffix() {
        return messageTemplateSuffix;
    }

    public void setMessageTemplateSuffix(String messageTemplateSuffix) {
        this.messageTemplateSuffix = messageTemplateSuffix;
    }



    @DataBoundConstructor
    public HipChatNotifier(final String authToken, final String room, String buildServerUrl, final String sendAs,
                           final String messageTemplateStarted, final String messageTemplateCompleted, final String messageTemplateSuffix) {
        super();
        this.authToken = authToken;
        this.buildServerUrl = buildServerUrl;
        this.room = room;
        this.sendAs = sendAs;
        this.messageTemplateStarted = messageTemplateStarted;
        this.messageTemplateCompleted = messageTemplateCompleted;
        this.messageTemplateSuffix = messageTemplateSuffix;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    public HipChatService newHipChatService(final String room) {
        return new StandardHipChatService(getAuthToken(), room == null ? getRoom() : room, getSendAs() == null ? "Build Server" : getSendAs());
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return true;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private String token;
        private String room;
        private String buildServerUrl;
        private String sendAs;
        private String messageTemplateStarted;
        private String messageTemplateCompleted;
        private String messageTemplateSuffix;

        public DescriptorImpl() {
            load();
        }

        public String getToken() {
            return token;
        }

        public String getRoom() {
            return room;
        }

        public String getBuildServerUrl() {
            return buildServerUrl;
        }

        public String getSendAs() {
            return sendAs;
        }

        public String getMessageTemplateStarted() {
            return messageTemplateStarted;
        }

        public String getMessageTemplateCompleted() {
            return messageTemplateCompleted;
        }

        public String getMessageTemplateSuffix() {
            return messageTemplateSuffix;
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public HipChatNotifier newInstance(StaplerRequest sr) {
            if (token == null) token = sr.getParameter("hipChatToken");
            if (buildServerUrl == null) buildServerUrl = sr.getParameter("hipChatBuildServerUrl");
            if (room == null) room = sr.getParameter("hipChatRoom");
            if (sendAs == null) sendAs = sr.getParameter("hipChatSendAs");
            if (messageTemplateStarted == null) messageTemplateStarted = sr.getParameter("hipChatMessageTemplateStarted");
            if (messageTemplateCompleted == null) messageTemplateCompleted = sr.getParameter("hipChatMessageTemplateCompleted");
            if (messageTemplateSuffix == null) messageTemplateSuffix = sr.getParameter("hipChatMessageTemplateSuffix");
            return new HipChatNotifier(token, room, buildServerUrl, sendAs, messageTemplateStarted, messageTemplateCompleted, messageTemplateSuffix);
        }

        @Override
        public boolean configure(StaplerRequest sr, JSONObject formData) throws FormException {
            token = sr.getParameter("hipChatToken");
            room = sr.getParameter("hipChatRoom");
            buildServerUrl = sr.getParameter("hipChatBuildServerUrl");
            sendAs = sr.getParameter("hipChatSendAs");
            messageTemplateStarted = sr.getParameter("hipChatMessageTemplateStarted");
            messageTemplateCompleted = sr.getParameter("hipChatMessageTemplateCompleted");

            if (buildServerUrl != null && !buildServerUrl.endsWith("/")) {
                buildServerUrl = buildServerUrl + "/";
            }
            try {
                new HipChatNotifier(token, room, buildServerUrl, sendAs, messageTemplateStarted, messageTemplateCompleted, messageTemplateSuffix);
            } catch (Exception e) {
                throw new FormException("Failed to initialize notifier - check your global notifier configuration settings", e, "");
            }
            save();
            return super.configure(sr, formData);
        }

        @Override
        public String getDisplayName() {
            return "HipChat Notifications";
        }
    }

    public static class HipChatJobProperty extends hudson.model.JobProperty<AbstractProject<?, ?>> {
        private String room;
        private boolean startNotification;
        private boolean notifySuccess;
        private boolean notifyAborted;
        private boolean notifyNotBuilt;
        private boolean notifyUnstable;
        private boolean notifyFailure;
        private String messageTemplateStarted;
        private String messageTemplateCompleted;
        private String messageTemplateSuffix;

        @DataBoundConstructor
        public HipChatJobProperty(String room, String messageTemplatedStarted, String messageTemplateCompleted, String messageTemplateSuffix, boolean startNotification, boolean notifyAborted, boolean notifyFailure, boolean notifyNotBuilt, boolean notifySuccess, boolean notifyUnstable) {
            this.room = room;
            this.messageTemplateStarted = messageTemplatedStarted;
            this.messageTemplateCompleted = messageTemplateCompleted;
            this.messageTemplateSuffix = messageTemplateSuffix;
            this.startNotification = startNotification;
            this.notifyAborted = notifyAborted;
            this.notifyFailure = notifyFailure;
            this.notifyNotBuilt = notifyNotBuilt;
            this.notifySuccess = notifySuccess;
            this.notifyUnstable = notifyUnstable;
        }

        @Exported
        public String getRoom() {
            return room;
        }

        @Exported
        public boolean getStartNotification() {
            return startNotification;
        }

        @Exported
        public boolean getNotifySuccess() {
            return notifySuccess;
        }

        @Exported
        public String getMessageTemplateStarted() {
            return messageTemplateStarted;
        }

        @Exported
        public String getMessageTemplateCompleted() {
            return messageTemplateCompleted;
        }

        @Exported
        public String getMessageTemplateSuffix() {
            return messageTemplateSuffix;
        }

        @Override
        public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
            if (startNotification) {
                Map<Descriptor<Publisher>, Publisher> map = build.getProject().getPublishersList().toMap();
                for (Publisher publisher : map.values()) {
                    if (publisher instanceof HipChatNotifier) {
                        logger.info("Invoking Started...");
                        new ActiveNotifier((HipChatNotifier) publisher).started(build);
                    }
                }
            }
            return super.prebuild(build, listener);
        }

        @Exported
        public boolean getNotifyAborted() {
            return notifyAborted;
        }

        @Exported
        public boolean getNotifyFailure() {
            return notifyFailure;
        }

        @Exported
        public boolean getNotifyNotBuilt() {
            return notifyNotBuilt;
        }

        @Exported
        public boolean getNotifyUnstable() {
            return notifyUnstable;
        }

        @Extension
        public static final class DescriptorImpl extends JobPropertyDescriptor {
            public String getDisplayName() {
                return "HipChat Notifications";
            }

            @Override
            public boolean isApplicable(Class<? extends Job> jobType) {
                return true;
            }

            @Override
            public HipChatJobProperty newInstance(StaplerRequest sr, JSONObject formData) throws hudson.model.Descriptor.FormException {
                return new HipChatJobProperty(sr.getParameter("hipChatProjectRoom"),
                        sr.getParameter("hipChatMessageTemplateStarted"),
                        sr.getParameter("hipChatMessageTemplateCompleted"),
                        sr.getParameter("hipChatMessageTemplateSuffix"),
                        sr.getParameter("hipChatStartNotification") != null,
                        sr.getParameter("hipChatNotifyAborted") != null,
                        sr.getParameter("hipChatNotifyFailure") != null,
                        sr.getParameter("hipChatNotifyNotBuilt") != null,
                        sr.getParameter("hipChatNotifySuccess") != null,
                        sr.getParameter("hipChatNotifyUnstable") != null);
            }
        }
    }
}
