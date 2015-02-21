package jenkins.plugins.hipchat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.scm.EditType;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import static jenkins.plugins.hipchat.HipChatNotifierBuilder.notifier;
import static jenkins.plugins.hipchat.NotificationType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class NotificationTypeTest {
    @Mock
    Jenkins jenkins;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @PrepareForTest(Jenkins.class)
    public void testGetMessage() throws Exception {
        mockJenkins();
        HipChatNotifier notifier = notifier().createHipChatNotifier();

        testNormalConfiguration(build(), notifier);
    }

    @Test
    @PrepareForTest(Jenkins.class)
    public void testGetMessageWithConfig() throws Exception {
        mockJenkins();

        HipChatNotifier notifier = notifier()
                .setMessageFailure("you broke it $AUTHOR!")
                .createHipChatNotifier();
        String url = "(<a href=\"http://localhost:8080/jenkins/foo/123\">Open</a>";
        String prefix = "test-job #33";
        assertNotifierProduces(build(), notifier, SUCCESS, prefix + " Success after 42 sec " + url + ")");
        assertNotifierProduces(build(), notifier, FAILURE, "you broke it Mike!");
        assertNotifierProduces(build(), notifier, NOT_BUILT, prefix + " Not built after 42 sec " + url + ")");
    }

    @Test
    @PrepareForTest(Jenkins.class)
    public void testGetMessageBlankConfiguration() throws Exception {
        mockJenkins();

        HipChatNotifier notifier = notifier()
                .setMessageFailure("")
                .setMessageNotBuilt(" ")
                .setMessageSuccess(null)
                .createHipChatNotifier();
        testNormalConfiguration(build(), notifier);
    }

    @Test
    @PrepareForTest(Jenkins.class)
    public void testGetMessageAllOverride() throws Exception {
        mockJenkins();

        HipChatNotifier notifier = notifier()
                .setMessageStarting("MessageStarting")
                .setMessageAborted("MessageAborted")
                .setMessageSuccess("MessageSuccess")
                .setMessageFailure("MessageFailure")
                .setMessageNotBuilt("MessageNotBuilt")
                .setMessageBackToNormal("MessageBackToNormal")
                .setMessageUnstable("MessageUnstable")
                .createHipChatNotifier();
        assertNotifierProduces(build(), notifier, STARTED, "MessageStarting");
        assertNotifierProduces(build(), notifier, ABORTED, "MessageAborted");
        assertNotifierProduces(build(), notifier, SUCCESS, "MessageSuccess");
        assertNotifierProduces(build(), notifier, FAILURE, "MessageFailure");
        assertNotifierProduces(build(), notifier, NOT_BUILT, "MessageNotBuilt");
        assertNotifierProduces(build(), notifier, BACK_TO_NORMAL, "MessageBackToNormal");
        assertNotifierProduces(build(), notifier, UNSTABLE, "MessageUnstable");
    }

    private void testNormalConfiguration(AbstractBuild<?, ?> build, HipChatNotifier notifier) {
        String url = "(<a href=\"http://localhost:8080/jenkins/foo/123\">Open</a>";
        String prefix = "test-job #33";
        assertNotifierProduces(build, notifier, STARTED, prefix + " Starting... (Started by changes from john.doe@example.com (1 file(s) changed)) " + url + ")");
        assertNotifierProduces(build, notifier, ABORTED, prefix + " ABORTED after 42 sec " + url + ")");
        assertNotifierProduces(build, notifier, SUCCESS, prefix + " Success after 42 sec " + url + ")");
        assertNotifierProduces(build, notifier, FAILURE, prefix + " <b>FAILURE</b> after 42 sec " + url + ")");
        assertNotifierProduces(build, notifier, NOT_BUILT, prefix + " Not built after 42 sec " + url + ")");
        assertNotifierProduces(build, notifier, BACK_TO_NORMAL, prefix + " Back to normal after 42 sec " + url + ")");
        assertNotifierProduces(build, notifier, UNSTABLE, prefix + " UNSTABLE after 42 sec " + url + ")");
    }

    private AbstractBuild<?, ?> build() throws java.io.IOException, InterruptedException {
        AbstractBuild<?, ?> build = mock(AbstractBuild.class);
        when(build.getUrl()).thenReturn("foo/123");
        when(build.getBuildVariables()).thenReturn(ImmutableMap.of(
                "JOB_NAME", "test-job",
                "BUILD_NUMBER", "33",
                "AUTHOR", "Mike"));
        when(build.getDurationString()).thenReturn("42 sec");
        mockChanges(build);
        EnvVars envVar = mock(EnvVars.class);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(envVar);
        return build;
    }

    private void assertNotifierProduces(AbstractBuild<?, ?> build, HipChatNotifier notifier, NotificationType type, String expected) {
        String msg = type.getMessage(build, notifier);
        assertThat(msg, equalTo(expected));
    }

    private void mockJenkins() {
        PowerMockito.mockStatic(Jenkins.class);
        PowerMockito.when(Jenkins.getInstance()).thenReturn(jenkins);
        PowerMockito.when(jenkins.getRootUrl()).thenReturn("http://localhost:8080/jenkins/");
    }

    private void mockChanges(final AbstractBuild<?, ?> build) {
        when(build.hasChangeSetComputed()).thenReturn(true);
        when(build.getChangeSet()).thenAnswer(new Answer<DummyChangeSet>() {
            @Override
            public DummyChangeSet answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new DummyChangeSet(null);
            }
        });
    }

    public static class DummyChangeSet extends ChangeLogSet<DummyChangeSet.DummyChangeSetEntry> {
        private final LinkedList<DummyChangeSetEntry> entries;

        public DummyChangeSet(AbstractBuild<?, ?> build) {
            super(build);
            entries = Lists.newLinkedList();
            entries.add(new DummyChangeSetEntry());
        }

        @Override
        public boolean isEmptySet() {
            return false;
        }

        @Override
        public Iterator<DummyChangeSetEntry> iterator() {
            return entries.iterator();
        }

        public static class DummyChangeSetEntry extends ChangeLogSet.Entry {
            private final User user;

            public DummyChangeSetEntry() {
                this.user = mock(User.class);
                when(this.user.getDisplayName()).thenReturn("john.doe@example.com");
            }

            @Override
            public Collection<? extends AffectedFile> getAffectedFiles() {
                return ImmutableList.of(new DummyAffectedFile("dummy-path"));
            }

            @Override
            public String getMsg() {
                return null;
            }

            @Override
            public User getAuthor() {
                return user;
            }

            @Override
            public Collection<String> getAffectedPaths() {
                return null;
            }

            private class DummyAffectedFile implements AffectedFile {
                private final String path;

                public DummyAffectedFile(String path) {
                    this.path = path;
                }

                @Override
                public String getPath() {
                    return path;
                }

                @Override
                public EditType getEditType() {
                    return null;
                }
            }
        }
    }
}
