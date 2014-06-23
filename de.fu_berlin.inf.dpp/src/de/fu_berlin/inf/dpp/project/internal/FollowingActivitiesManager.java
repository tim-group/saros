package de.fu_berlin.inf.dpp.project.internal;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.activities.StartFollowingActivity;
import de.fu_berlin.inf.dpp.activities.StopFollowingActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.awareness.AwarenessInformationCollector;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

/**
 * This manager is responsible for distributing knowledge about changes in
 * follow modes between session participants. It both produces and consumes
 * activities.
 * 
 * @author Alexander Waldmann (contact@net-corps.de)
 */
@Component(module = "core")
public class FollowingActivitiesManager extends AbstractActivityProducer
    implements Startable {

    private static final Logger log = Logger
        .getLogger(FollowingActivitiesManager.class);

    protected final List<IFollowModeChangesListener> internalListeners = new LinkedList<IFollowModeChangesListener>();
    protected ISarosSession sarosSession;
    protected AwarenessInformationCollector awarenessInformationCollector;

    public FollowingActivitiesManager(ISarosSessionManager sessionManager,
        EditorManager editorManager,
        AwarenessInformationCollector awarenessInformationCollector) {
        this.awarenessInformationCollector = awarenessInformationCollector;
        sessionManager.addSarosSessionListener(sessionListener);
        editorManager
            .addSharedEditorListener(new AbstractSharedEditorListener() {
                @Override
                public void followModeChanged(User followedUser,
                    boolean isFollowed) {
                    if (sarosSession == null) {
                        log.error("FollowModeChanged Event listener got a call without a running session.");
                        return;
                    }

                    if (isFollowed) {
                        fireActivity(new StartFollowingActivity(sarosSession
                            .getLocalUser(), followedUser));
                    } else {
                        fireActivity(new StopFollowingActivity(sarosSession
                            .getLocalUser()));

                    }
                }
            });
    }

    @Override
    public void exec(IActivity activity) {
        activity.dispatch(receiver);
    }

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {
        @Override
        public void receive(StartFollowingActivity activity) {
            User user = activity.getSource();
            if (!user.isInSarosSession()) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "illegal follow mode activity received", user));
            }

            log.info("Received new follow mode from: "
                + user.getHumanReadableName() + " followed User: "
                + activity.getFollowedUser().getHumanReadableName());

            awarenessInformationCollector.setUserFollowing(user,
                activity.getFollowedUser());
            notifyListeners();
        }

        @Override
        public void receive(StopFollowingActivity activity) {
            User user = activity.getSource();
            if (!user.isInSarosSession()) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "illegal follow mode activity received", user));
            }

            log.info("User " + user.getHumanReadableName()
                + " stopped follow mode");

            awarenessInformationCollector.setUserFollowing(user, null);
            notifyListeners();
        }
    };


    public FollowingActivitiesManager(final ISarosSession session,
        final AwarenessInformationCollector collector,
        final EditorManager editor) {
        this.session = session;
        this.collector = collector;
        this.editor = editor;
    }

    @Override
    public void start() {
        collector.flushFollowModes();
        session.addActivityProducer(this);
        session.addActivityConsumer(consumer);
        editor.addSharedEditorListener(followModeListener);
    }

    @Override
    public void stop() {
        session.removeActivityProducer(this);
        session.removeActivityConsumer(consumer);
        editor.removeSharedEditorListener(followModeListener);
        collector.flushFollowModes();
    }

    private void notifyListeners() {
        for (IFollowModeChangesListener listener : listeners)
            listener.followModeChanged();
        }
    }

    public void addIinternalListener(IFollowModeChangesListener listener) {
        this.internalListeners.add(listener);
    }

    public void removeIinternalListener(IFollowModeChangesListener listener) {
        this.internalListeners.remove(listener);
    }

}
