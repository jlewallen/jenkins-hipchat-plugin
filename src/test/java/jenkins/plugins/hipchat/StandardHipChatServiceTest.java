package jenkins.plugins.hipchat;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class StandardHipChatServiceTest {
    @Test
    public void publishWithBadHostShouldNotRethrowExceptions() {
        StandardHipChatService service = new StandardHipChatService("badhost", "token", "room", "from");
        service.publish("message");
    }

    @Test
    public void shouldSetADefaultHost() {
        StandardHipChatService service = new StandardHipChatService(null, "token", "room", "from");
        assertEquals("api.hipchat.com", service.getHost());
    }

    @Test
    public void shouldBeAbleToOverrideHost() {
        StandardHipChatService service = new StandardHipChatService("some.other.host", "token", "room", "from");
        assertEquals("some.other.host", service.getHost());
    }

    @Test
    public void shouldSplitTheRoomIds() {
        StandardHipChatService service = new StandardHipChatService(null, "token", "room1,room2", "from");
        assertArrayEquals(new String[]{"room1", "room2"}, service.getRoomIds());
    }

    @Test
    public void shouldNotSplitTheRoomsIfNullIsPassed() {
        StandardHipChatService service = new StandardHipChatService(null, "token", null, "from");
        assertArrayEquals(new String[0], service.getRoomIds());
    }

    @Test
    public void shouldProvideADefaultFrom() {
        StandardHipChatService service = new StandardHipChatService(null, "token", "room", null);
        assertEquals("Build Server", service.getFrom());
    }

    @Test
    public void shouldBeAbleToOverrideFrom() {
        StandardHipChatService service = new StandardHipChatService(null, "token", "room", "from");
        assertEquals("from", service.getFrom());
    }
}
