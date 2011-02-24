package jenkins.plugins.hipchat;

import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;

import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

public class DescriptorImpl extends BuildStepDescriptor<Publisher> {
   private static final Logger logger = Logger.getLogger(HipChatNotifier.class.getName());

   private String token;
   private String room;
   private String jenkinsUrl;

   public DescriptorImpl() {
      super(HipChatNotifier.class);
      load();
   }

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

   /**
    * @see hudson.model.Descriptor#newInstance(org.kohsuke.stapler.StaplerRequest)
    */
   @Override
   public HipChatNotifier newInstance(StaplerRequest req, JSONObject formData) throws FormException {
      String projectRoom = req.getParameter("hipChatProjectRoom");
      if(projectRoom == null || projectRoom.trim().length() == 0) {
         projectRoom = getRoom();
      }
      try {
         return new HipChatNotifier(token, projectRoom, jenkinsUrl);
      }
      catch(Exception e) {
         throw new FormException("Failed to initialize notifier - check your notifier configuration settings", e, "");
      }
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

   /**
    * @see hudson.model.Descriptor#getDisplayName()
    */
   @Override
   public String getDisplayName() {
      return "HipChat Notification";
   }

   /**
    * @see hudson.model.Descriptor#getHelpFile()
    */
   @Override
   public String getHelpFile() {
      return "/plugin/jenkins/help.html";
   }
}
