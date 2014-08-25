package jenkins.plugins.hipchat;

import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StandardHipChatService implements HipChatService {
    private static final Logger logger = Logger.getLogger(StandardHipChatService.class.getName());
    private static final String DEFAULT_HOST = "api.hipchat.com";
    private static final String[] DEFAULT_ROOMS = new String[0];
    private static final String DEFAULT_FROM = "Build Server";

    private String host;
    private String token;
    private String[] roomIds;
    private String from;

    public StandardHipChatService(String host, String token, String roomIds, String from) {
        super();
        this.host = host == null ? DEFAULT_HOST : host;
        this.token = token;
        this.roomIds = roomIds == null ? DEFAULT_ROOMS : roomIds.split(",");
        this.from = from == null ? DEFAULT_FROM : from;
    }

    public void publish(String message) {
        publish(message, "yellow");
    }

    public void publish(String message, String color) {
        for (String roomId : roomIds) {
            logger.info("Posting: " + from + " to " + roomId + ": " + message + " " + color);
            HttpClient client = getHttpClient();
            String url = "https://" + host + "/v1/rooms/message?auth_token=" + token;
            PostMethod post = new PostMethod(url);

            try {
                post.addParameter("from", from);
                post.addParameter("room_id", roomId);
                post.addParameter("message", message);
                post.addParameter("color", color);
                post.addParameter("notify", shouldNotify(color));
                post.getParams().setContentCharset("UTF-8");
                int responseCode = client.executeMethod(post);
                String response = post.getResponseBodyAsString();
                if (responseCode != HttpStatus.SC_OK || !response.contains("\"sent\"")) {
                    logger.log(Level.WARNING, "HipChat post may have failed. Response: " + response);
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

    public String getHost() {
        return host;
    }

    public String[] getRoomIds() {
        return roomIds;
    }

    public String getFrom() {
        return from;
    }
}
