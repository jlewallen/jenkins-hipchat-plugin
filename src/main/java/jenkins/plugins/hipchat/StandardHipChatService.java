package jenkins.plugins.hipchat;

import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StandardHipChatService implements HipChatService {

    /**
     * HTTP Connection timeout when making calls to HipChat
     */
    public static final Integer DEFAULT_TIMEOUT = 10000;
    private static final Logger logger = Logger.getLogger(StandardHipChatService.class.getName());
    private static final String[] DEFAULT_ROOMS = new String[0];

    private final String server;
    private final String token;
    private final String[] roomIds;
    private final String sendAs;

    public StandardHipChatService(String server, String token, String roomIds, String sendAs) {
        super();
        this.server = server;
        this.token = token;
        this.roomIds = roomIds == null ? DEFAULT_ROOMS : roomIds.split("\\s*,\\s*");
        this.sendAs = sendAs;
    }

    public void publish(String message) {
        publish(message, "yellow");
    }

    public void publish(String message, String color) {
        for (String roomId : roomIds) {
            logger.log(Level.INFO, "Posting: {0} to {1}: {2} {3}", new Object[]{sendAs, roomId, message, color});
            HttpClient client = getHttpClient();
            String url = "https://" + server + "/v1/rooms/message?auth_token=" + token;
            PostMethod post = new PostMethod(url);

            try {
                post.addParameter("from", sendAs);
                post.addParameter("room_id", roomId);
                post.addParameter("message", message);
                post.addParameter("color", color);
                post.addParameter("notify", shouldNotify(color));
                post.getParams().setContentCharset("UTF-8");
                int responseCode = client.executeMethod(post);
                String response = post.getResponseBodyAsString();
                if (responseCode != HttpStatus.SC_OK || !response.contains("\"sent\"")) {
                    logger.log(Level.WARNING, "HipChat post may have failed. Response: {0}", response);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error posting to HipChat", e);
            } finally {
                post.releaseConnection();
            }
        }
    }

    private HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(DEFAULT_TIMEOUT);
        client.getHttpConnectionManager().getParams().setSoTimeout(DEFAULT_TIMEOUT);

        if (Jenkins.getInstance() != null) {
            ProxyConfiguration proxy = Jenkins.getInstance().proxy;

            if (proxy != null) {
                client.getHostConfiguration().setProxy(proxy.name, proxy.port);
            }
        }

        return client;
    }

    private String shouldNotify(String color) {
        return color.equalsIgnoreCase("green") ? "0" : "1";
    }

    public String getServer() {
        return server;
    }

    public String[] getRoomIds() {
        return roomIds;
    }

    public String getSendAs() {
        return sendAs;
    }
}
