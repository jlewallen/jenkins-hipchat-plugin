package jenkins.plugins.hipchat;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.model.Jenkins;

public class MessageBuilder {

    private final StringBuilder message;
    private final AbstractBuild<?, ?> build;

    public MessageBuilder(AbstractBuild<?, ?> build) {
        this.message = new StringBuilder();
        this.build = build;
        startMessage();
    }

    public MessageBuilder appendStatusMessage() {
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
        if (result == Result.SUCCESS && previousResult == Result.FAILURE) {
            return "Back to normal";
        }
        if (result == Result.SUCCESS) {
            return "Success";
        }
        if (result == Result.FAILURE) {
            return "<b>FAILURE</b>";
        }
        if (result == Result.ABORTED) {
            return "ABORTED";
        }
        if (result == Result.NOT_BUILT) {
            return "Not built";
        }
        if (result == Result.UNSTABLE) {
            return "Unstable";
        }
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
        String url = Jenkins.getInstance().getRootUrl() + build.getUrl();
        message.append(" (<a href='").append(url).append("'>Open</a>)");
        return this;
    }

    public MessageBuilder appendDuration() {
        message.append(" after ");
        message.append(build.getDurationString());
        return this;
    }

    @Override
    public String toString() {
        return message.toString();
    }
}
