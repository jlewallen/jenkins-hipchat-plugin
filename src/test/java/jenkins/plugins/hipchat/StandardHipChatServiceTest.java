package jenkins.plugins.hipchat;

import org.junit.Before;
import org.junit.Test;

public class StandardHipChatServiceTest {
    private StandardHipChatService service;

    @Before
    public void setUp() throws Exception {
        service = new StandardHipChatService("token", "room", "from");
        service.setHost("localhost");
    }

    @Test
    public void publishShouldNotRethrowExceptions() {
        service.publish("message");
    }
}
