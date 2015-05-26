package jenkins.plugins.hipchat;

import jenkins.plugins.hipchat.impl.HipChatV1Service;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class HipChatV1ServiceTest {
    @Test
    public void publishWithBadHostShouldNotRethrowExceptions() {
        HipChatV1Service service = new HipChatV1Service("badhost", "token", "room", "from");
        service.publish("message", "yellow");
    }

    @Test
    public void shouldBeAbleToOverrideHost() {
        HipChatV1Service service = new HipChatV1Service("some.other.host", "token", "room", "from");
        assertEquals("some.other.host", service.getServer());
    }

    @Test
    public void shouldSplitTheRoomIds() {
        HipChatV1Service service = new HipChatV1Service(null, "token", "room1,room2", "from");
        assertArrayEquals(new String[]{"room1", "room2"}, service.getRoomIds());
    }

    @Test
    public void shouldTrimTheRoomIds() {
        HipChatV1Service service = new HipChatV1Service(null, "token", "room1, room2", "from");
        assertArrayEquals(new String[]{"room1", "room2"}, service.getRoomIds());
    }

    @Test
    public void shouldNotSplitTheRoomsIfNullIsPassed() {
        HipChatV1Service service = new HipChatV1Service(null, "token", null, "from");
        assertArrayEquals(new String[0], service.getRoomIds());
    }

    @Test
    public void shouldBeAbleToOverrideFrom() {
        HipChatV1Service service = new HipChatV1Service(null, "token", "room", "from");
        assertEquals("from", service.getSendAs());
    }
}
