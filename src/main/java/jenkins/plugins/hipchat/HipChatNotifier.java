package jenkins.plugins.hipchat;

import hudson.Extension;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.JobPropertyDescriptor;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.util.Map;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

@SuppressWarnings({ "unchecked" })
public class HipChatNotifier extends Notifier {

   private static final Logger logger = Logger.getLogger(HipChatNotifier.class.getName());

   private String jenkinsUrl;
   private String authToken;
   private String room;

   @Override
   public DescriptorImpl getDescriptor() {
      return (DescriptorImpl)super.getDescriptor();
   }

   public String getRoom() {
      return room;
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

   public void setRoom(String room) {
      this.room = room;
   }

   @DataBoundConstructor
   public HipChatNotifier(String authToken, String room, String jenkinsUrl) {
      super();
      this.authToken = authToken;
      this.jenkinsUrl = jenkinsUrl;
      this.room = room;
   }

   public BuildStepMonitor getRequiredMonitorService() {
      return BuildStepMonitor.BUILD;
   }

   public HipChatService newHipChatService(String room) {
      return new StandardHipChatService(getAuthToken(), room == null ? getRoom() : room, "Jenkins");
   }

   @Extension
   public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
      private String token;
      private String room;
      private String jenkinsUrl;

      public String getToken() {
         return token;
      }

      public String getRoom() {
         return room;
      }

      public String getJenkinsUrl() {
         return jenkinsUrl;
      }

      public boolean isApplicable(Class<? extends AbstractProject> aClass) {
         return true;
      }

      @Override
      public HipChatNotifier newInstance(StaplerRequest sr) {
         if(token == null) token = sr.getParameter("hipChatToken");
         if(jenkinsUrl == null) jenkinsUrl = sr.getParameter("hipChatJenkinsUrl");
         if(room == null) room = sr.getParameter("hipChatRoom");
         return new HipChatNotifier(token, room, jenkinsUrl);
      }

      @Override
      public boolean configure(StaplerRequest sr, JSONObject formData) throws FormException {
         token = sr.getParameter("hipChatToken");
         room = sr.getParameter("hipChatRoom");
         jenkinsUrl = sr.getParameter("hipChatJenkinsUrl");
         if(jenkinsUrl != null && !jenkinsUrl.endsWith("/")) {
            jenkinsUrl = jenkinsUrl + "/";
         }
         try {
            new HipChatNotifier(token, room, jenkinsUrl);
         }
         catch(Exception e) {
            throw new FormException("Failed to initialize notifier - check your global notifier configuration settings", e, "");
         }
         save();
         return super.configure(sr, formData);
      }

      @Override
      public String getDisplayName() {
         return "HipChat Notifications";
      }
   }

   public static class HipChatJobProperty extends hudson.model.JobProperty<AbstractProject<?, ?>> {
      private String room;
      private boolean startNotification;

      @DataBoundConstructor
      public HipChatJobProperty(String room, boolean startNotification) {
         this.room = room;
         this.startNotification = startNotification;
      }

      @Exported
      public String getRoom() {
         return room;
      }

      @Exported
      public boolean getStartNotification() {
         return startNotification;
      }

      @Override
      public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
         if(startNotification) {
            Map<Descriptor<Publisher>, Publisher> map = build.getProject().getPublishersList().toMap();
            for(Publisher publisher : map.values()) {
               if(publisher instanceof HipChatNotifier) {
                  logger.info("Invoking Started...");
                  new ActiveNotifier((HipChatNotifier)publisher).started(build);
               }
            }
         }
         return super.prebuild(build, listener);
      }

      @Extension
      public static final class DescriptorImpl extends JobPropertyDescriptor {
         public String getDisplayName() {
            return "HipChat Notifications";
         }

         @Override
         public boolean isApplicable(Class<? extends Job> jobType) {
            return true;
         }

         @Override
         public HipChatJobProperty newInstance(StaplerRequest sr, JSONObject formData) throws hudson.model.Descriptor.FormException {
            return new HipChatJobProperty(sr.getParameter("hipChatProjectRoom"), sr.getParameter("hipChatStartNotification") != null);
         }
      }
   }
}
