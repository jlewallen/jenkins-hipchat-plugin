package jenkins.plugins.hipchat;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.scm.ChangeLogSet;
import hudson.util.LogTaskListener;
import hudson.util.VariableResolver;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Maps.newHashMap;
import static hudson.Util.replaceMacro;
import static java.util.logging.Level.INFO;

public enum NotificationType {

    STARTED("green") {
        @Override
        protected String getConfiguredTemplateFor(HipChatNotifier notifier) {
            return notifier.getMessageStarted();
        }

        @Override
        public String getDefaultTemplate() {
            return Messages.Started();
        }
    },
    ABORTED("gray") {
        @Override
        protected String getConfiguredTemplateFor(HipChatNotifier notifier) {
            return notifier.getMessageAborted();
        }

        @Override
        public String getDefaultTemplate() {
            return Messages.Aborted();
        }
    },
    SUCCESS("green") {
        @Override
        protected String getConfiguredTemplateFor(HipChatNotifier notifier) {
            return notifier.getMessageSuccess();
        }

        @Override
        public String getDefaultTemplate() {
            return Messages.Success();
        }
    },
    FAILURE("red") {
        @Override
        protected String getConfiguredTemplateFor(HipChatNotifier notifier) {
            return notifier.getMessageFailure();
        }

        @Override
        public String getDefaultTemplate() {
            return Messages.Failure();
        }
    },
    NOT_BUILT("gray") {
        @Override
        protected String getConfiguredTemplateFor(HipChatNotifier notifier) {
            return notifier.getMessageNotBuilt();
        }

        @Override
        public String getDefaultTemplate() {
            return Messages.NotBuilt();
        }
    },
    BACK_TO_NORMAL("green") {
        @Override
        protected String getConfiguredTemplateFor(HipChatNotifier notifier) {
            return notifier.getMessageBackToNormal();
        }

        @Override
        public String getDefaultTemplate() {
            return Messages.BackToNormal();
        }
    },
    UNSTABLE("yellow") {
        @Override
        protected String getConfiguredTemplateFor(HipChatNotifier notifier) {
            return notifier.getMessageUnstable();
        }

        @Override
        public String getDefaultTemplate() {
            return Messages.Unstable();
        }
    },
    UNKNOWN("purple") {
        @Override
        protected String getConfiguredTemplateFor(HipChatNotifier notifier) {
            throw new IllegalStateException();
        }

        @Override
        public String getDefaultTemplate() {
            return null;
        }
    };

    private static final Logger logger = Logger.getLogger(NotificationType.class.getName());
    private final String color;

    private NotificationType(String color) {
        this.color = color;
    }

    protected abstract String getConfiguredTemplateFor(HipChatNotifier notifier);
    public abstract String getDefaultTemplate();

    public String getColor() {
        return color;
    }

    public final String getMessage(AbstractBuild<?, ?> build, HipChatNotifier notifier) {
        String format = getTemplateFor(notifier);
        Map<String, String> messageVariables = collectParametersFor(build);

        return replaceMacro(format, new VariableResolver.ByMap<String>(messageVariables));
    }

    private String getTemplateFor(HipChatNotifier notifier) {
        String userConfig = this.getConfiguredTemplateFor(notifier);
        String defaultConfig = this.getDefaultTemplate();
        if (userConfig == null || userConfig.trim().isEmpty()) {
            Preconditions.checkState(defaultConfig != null, "default config not set for %s", this);
            return defaultConfig;
        } else {
            return userConfig;
        }
    }

    private Map<String, String> collectParametersFor(AbstractBuild build) {
        Map<String, String> merged = newHashMap();
        merged.putAll(build.getBuildVariables());
        merged.putAll(getEnvVars(build));

        String cause = NotificationTypeUtils.getCause(build);
        String changes = NotificationTypeUtils.getChanges(build);

        merged.put("DURATION", build.getDurationString());
        merged.put("URL", NotificationTypeUtils.getUrl(build));
        merged.put("CAUSE", cause);
        merged.put("CHANGES_OR_CAUSE", changes != null ? changes : cause);
        merged.put("CHANGES", changes);
        merged.put("PRINT_FULL_ENV", merged.toString());
        return merged;
    }

    private EnvVars getEnvVars(AbstractBuild build) {
        try {
            return build.getEnvironment(new LogTaskListener(logger, INFO));
        } catch (IOException e) {
            throw propagate(e);
        } catch (InterruptedException e) {
            throw propagate(e);
        }
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
