package jenkins.plugins.hipchat.impl;

import hudson.model.AbstractBuild;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.plugins.hipchat.HipChatService;
import jenkins.plugins.hipchat.NotificationType;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

public class HipChatV2Service extends HipChatService {

    private static final Logger logger = Logger.getLogger(HipChatV2Service.class.getName());
    private static final String[] DEFAULT_ROOMS = new String[0];

    private final String server;
    private final String token;
    private final String[] roomIds;

    public HipChatV2Service(String server, String token, String roomIds) {
        this.server = server;
        this.token = token;
        this.roomIds = roomIds == null ? DEFAULT_ROOMS : roomIds.split("\\s*,\\s*");
    }

    public void publish(String message, String color) {
        publish(message, color, shouldNotify(color));
    }

    public void publish(String message, String color, boolean notify) {
        for (String roomId : roomIds) {
            logger.log(Level.INFO, "Posting: {0} to {1}: {2}", new Object[]{roomId, message, color});
            HttpClient client = getHttpClient();

            PostMethod post = null;
            try {
                String url = "https://" + server + "/v2/room/" + URLEncoder.encode(roomId, "UTF-8") + "/notification";
                post = new PostMethod(url);
                post.getParams().setContentCharset("UTF-8");
                post.addRequestHeader("Authorization", "Bearer " + token);

                JSONObject notification = new JSONObject();
                notification.put("message", message);
                notification.put("color", color);
                notification.put("notify", notify);
                post.setRequestEntity(new StringRequestEntity(notification.toString(), "application/json", "UTF-8"));
                int responseCode = client.executeMethod(post);
                if (responseCode != HttpStatus.SC_NO_CONTENT) {
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.log(Level.WARNING, "HipChat post may have failed. ResponseCode: {0}, Response: {1}",
                                new Object[]{responseCode, post.getResponseBodyAsString()});
                    }
                }
            } catch (IllegalArgumentException iae) {
                logger.log(Level.WARNING, "Invalid argument provided", iae);
            } catch (UnsupportedEncodingException uee) {
                logger.log(Level.WARNING, "Unable to construct notification URL", uee);
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "An IO error occurred while posting HipChat notification", ioe);
            } finally {
                if (post != null) {
                    post.releaseConnection();
                }
            }
        }
    }

    private boolean shouldNotify(String color) {
        return !color.equalsIgnoreCase("green");
    }
}
