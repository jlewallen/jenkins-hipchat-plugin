package hudson.plugins.hipchat;

import hudson.tasks.Notifier;
import hudson.tasks.BuildStepMonitor;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.Method;
import java.io.*;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

public class HipChatNotifier extends Notifier {

  private boolean smartNotify;
  private String hudsonUrl;
  private String roomId;

  public String getConfiguredRoomId() {
    if (DESCRIPTOR.getRoom().equals(roomId) ) {
      return null;   
    } else {
      return roomId;  
    }
  }

  @Extension
  public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

  private static final Logger LOGGER = Logger.getLogger(HipChatNotifier.class.getName());

  public HipChatNotifier() throws IOException {
    super();
    initialize();
  }

  public HipChatNotifier(String token, String room, String hudsonUrl, boolean ssl, boolean smartNotify) throws IOException {
    super();
    initialize(token, room, hudsonUrl, ssl, smartNotify);
  }

  public BuildStepMonitor getRequiredMonitorService() {
    return BuildStepMonitor.BUILD;
  }

  private void publish(AbstractBuild<?, ?> build) throws IOException {
    Result result = build.getResult();
    String resultString = result.toString();
    if (!smartNotify && result == Result.SUCCESS) resultString = resultString.toLowerCase();
    String message = build.getProject().getName() + " " + build.getDisplayName() + " \"" + "" + "\": " + resultString;
    if (hudsonUrl != null && hudsonUrl.length() > 1 && (smartNotify || result != Result.SUCCESS)) {
      message = message + " (" + hudsonUrl + build.getUrl() + ")";
    }
    HttpClient client = new HttpClient();
    String url = "https://api.hipchat.com/v1/rooms/message?auth_token=" + DESCRIPTOR.getToken();
    PostMethod post = new PostMethod(url);
    post.addParameter("from", "Hudson");
    post.addParameter("room_id", DESCRIPTOR.getRoom());
    post.addParameter("message", message);
    try {
      client.executeMethod(post);
    } finally {
      post.releaseConnection();
    }
  }

  private void initialize() throws IOException {
    initialize(DESCRIPTOR.getToken(), DESCRIPTOR.getRoom(), DESCRIPTOR.getHudsonUrl(), DESCRIPTOR.getSsl(), DESCRIPTOR.getSmartNotify());
  }

  private void initialize(String token, String roomId, String hudsonUrl, boolean ssl, boolean smartNotify) throws IOException {
    this.hudsonUrl = hudsonUrl;
    this.smartNotify = smartNotify;
    this.roomId = roomId;
  }

  @Override
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
    // If SmartNotify is enabled, only notify if:
    //  (1) there was no previous build, or
    //  (2) the current build did not succeed, or
    //  (3) the previous build failed and the current build succeeded.
    if (smartNotify) {
      AbstractBuild previousBuild = build.getPreviousBuild();
      if (previousBuild == null || build.getResult() != Result.SUCCESS || previousBuild.getResult() != Result.SUCCESS) {
        publish(build);
      }
    } else {
      publish(build);
    }
    return true;
  }
}
