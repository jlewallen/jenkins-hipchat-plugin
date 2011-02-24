package jenkins.plugins.hipchat;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.listeners.RunListener;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.ChangeLogSet.Entry;
import hudson.tasks.Publisher;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

@Extension
@SuppressWarnings("rawtypes")
public class HipChatListener extends RunListener<AbstractBuild> {

   private static final Logger logger = Logger.getLogger(HipChatListener.class.getName());

   public HipChatListener() {
      super(AbstractBuild.class);
   }

   @Override
   public void onCompleted(AbstractBuild r, TaskListener listener) {
      getNotifier(r.getProject()).completed(r);
      super.onCompleted(r, listener);
   }

   @Override
   public void onStarted(AbstractBuild r, TaskListener listener) {
      getNotifier(r.getProject()).started(r);
      super.onStarted(r, listener);
   }

   @Override
   public void onDeleted(AbstractBuild r) {
      // getNotifier(r.getProject()).deleted(r);
      // super.onDeleted(r);
   }

   @Override
   public void onFinalized(AbstractBuild r) {
      // getNotifier(r.getProject()).finalized(r);
      // super.onFinalized(r);
   }

   @SuppressWarnings("unchecked")
   Notifier getNotifier(AbstractProject project) {
      Map<Descriptor<Publisher>, Publisher> map = project.getPublishersList().toMap();
      for(Publisher publisher : map.values()) {
         if(publisher instanceof HipChatNotifier) {
            return new ActiveNotifier((HipChatNotifier)publisher);
         }
      }
      return new DisabledNotifier();
   }

   public class ActiveNotifier implements Notifier {

      HipChatNotifier notifier;
      HipChatService hipChat;

      public ActiveNotifier(HipChatNotifier notifier) {
         super();
         this.notifier = notifier;
         this.hipChat = notifier.newHipChatService();
      }

      public void deleted(AbstractBuild r) {}

      public void started(AbstractBuild r) {
         String changes = getChanges(r);
         if(changes != null) {
            this.hipChat.publish(changes);
         }
         this.hipChat.publish(getBuildStatusMessage(r));
      }

      public void finalized(AbstractBuild r) {
         this.hipChat.publish(getBuildStatusMessage(r));
      }

      public void completed(AbstractBuild r) {
         String changes = getChanges(r);
         if(changes != null) {
            this.hipChat.publish(changes);
         }
         this.hipChat.publish(getBuildStatusMessage(r));
      }

      String getChanges(AbstractBuild r) {
         if(!r.hasChangeSetComputed()) {
            logger.info("No change set computed...");
            return null;
         }
         ChangeLogSet changeSet = r.getChangeSet();
         List<Entry> entries = new LinkedList<Entry>();
         Set<AffectedFile> files = new HashSet<AffectedFile>();
         for(Object o : changeSet.getItems()) {
            Entry entry = (Entry)o;
            logger.info("Entry " + o);
            entries.add(entry);
            files.addAll(entry.getAffectedFiles());
         }
         if(entries.isEmpty()) {
            logger.info("Empty change...");
            return null;
         }
         Set<String> authors = new HashSet<String>();
         for(Entry entry : entries) {
            authors.add(entry.getAuthor().getDisplayName());
         }
         StringBuffer message = new StringBuffer();
         message.append(r.getProject().getDisplayName());
         message.append(" - ");
         message.append(r.getDisplayName());
         message.append(" ");
         message.append("Triggered by changes from ");
         message.append(StringUtils.join(authors, ", "));
         message.append(" (");
         message.append(files.size());
         message.append(" file(s) changed)");
         return message.toString();
      }

      String getBuildStatusMessage(AbstractBuild r) {
         StringBuffer message = new StringBuffer();
         message.append(r.getProject().getDisplayName());
         message.append(" - ");
         message.append(r.getDisplayName());
         message.append(" ");
         message.append(getStatusMessage(r));
         String url = notifier.getJenkinsUrl() + r.getUrl();
         message.append(" (<a href='").append(url).append("'>Open</a>)");
         return message.toString();
      }

      String getStatusMessage(AbstractBuild r) {
         if(r.isBuilding()) {
            return "Starting...";
         }
         Result result = r.getResult();
         if(result == Result.SUCCESS) return "Success";
         if(result == Result.FAILURE) return "<b>FAILURE</b>";
         if(result == Result.ABORTED) return "ABORTED";
         if(result == Result.NOT_BUILT) return "Not built";
         if(result == Result.UNSTABLE) return "Unstable";
         return "Unknown";
      }
   }

   public interface Notifier {
      public void started(AbstractBuild r);

      public void deleted(AbstractBuild r);

      public void finalized(AbstractBuild r);

      public void completed(AbstractBuild r);
   }

   public class DisabledNotifier implements Notifier {
      public void started(AbstractBuild r) {}

      public void deleted(AbstractBuild r) {}

      public void finalized(AbstractBuild r) {}

      public void completed(AbstractBuild r) {}
   }

}
