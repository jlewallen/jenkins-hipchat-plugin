package jenkins.plugins.hipchat;

import hudson.Launcher;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;

import java.io.IOException;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

@SuppressWarnings({ "unchecked" })
public class HipChatNotifier extends Notifier {

   private static final Logger logger = Logger.getLogger(HipChatNotifier.class.getName());

   private String jenkinsUrl;
   private String authToken;
   private String roomId;

   @Override
   public DescriptorImpl getDescriptor() {
      return (DescriptorImpl)super.getDescriptor();
   }

   /**
    * Used by the config.jelly
    */
   public String getProjectSpecificRoomId() {
      if(getDescriptor().getRoom().equals(roomId)) {
         return null;
      }
      else {
         return roomId;
      }
   }

   public String getRoomId() {
      return roomId;
   }

   public String getAuthToken() {
      return authToken;
   }

   public String getJenkinsUrl() {
      return jenkinsUrl;
   }

   public void setJenkinsUrl(String jenkinsUrl) {
      this.jenkinsUrl = jenkinsUrl;
   }

   public void setAuthToken(String authToken) {
      this.authToken = authToken;
   }

   public void setRoomId(String roomId) {
      this.roomId = roomId;
   }

   @DataBoundConstructor
   public HipChatNotifier(String authToken, String roomId, String jenkinsUrl) {
      super();
      this.authToken = authToken;
      this.jenkinsUrl = jenkinsUrl;
      this.roomId = roomId;
   }

   @Override
   public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
      FineGrainedNotifier notifier = new ActiveNotifier(this);
      notifier.started(build);
      return super.prebuild(build, listener);
   }

   public boolean perform(Build<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
      return true;
   }

   public BuildStepMonitor getRequiredMonitorService() {
      return BuildStepMonitor.BUILD;
   }

   public HipChatService newHipChatService() {
      return new StandardHipChatService(getAuthToken(), getRoomId(), "Jenkins");
   }
}
