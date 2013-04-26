package jenkins.plugins.hipchat;

import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.plugins.hipchat.StandardHipChatService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StandardHipChatServiceTest {

    private Level loggingLevel;

    @Before
    public void saveLoggingLevel() {
        loggingLevel = Logger.getLogger(StandardHipChatService.class.getName()).getLevel();
    }

    /**
     * Publish should generally not rethrow exceptions, or it will cause a build job to fail at end.
     */
    @Test
    public void publishWithBadHostShouldNotRethrowExceptions() {
        dropLoggingLevel();
        StandardHipChatService service = new StandardHipChatService("token", "room", "from");
        service.setHost("hostvaluethatwillcausepublishtofail");
        service.publish("message");
    }

    private void dropLoggingLevel() {
        Logger.getLogger(StandardHipChatService.class.getName()).setLevel(Level.SEVERE);
    }

    @After
    public void restoreLoggingLevel() {
        Logger.getLogger(StandardHipChatService.class.getName()).setLevel(loggingLevel);
    }
}
