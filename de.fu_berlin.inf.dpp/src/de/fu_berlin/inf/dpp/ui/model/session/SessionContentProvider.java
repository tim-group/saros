package de.fu_berlin.inf.dpp.ui.model.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.awareness.AwarenessInformationCollector;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.net.mdns.MDNSService;
import de.fu_berlin.inf.dpp.net.xmpp.roster.AbstractRosterListener;
import de.fu_berlin.inf.dpp.project.internal.FollowingActivitiesManager;
import de.fu_berlin.inf.dpp.project.internal.IFollowModeChangesListener;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.model.HeaderElement;
import de.fu_berlin.inf.dpp.ui.model.TreeContentProvider;
import de.fu_berlin.inf.dpp.ui.model.mdns.MDNSContentProvider;
import de.fu_berlin.inf.dpp.ui.model.mdns.MDNSHeaderElement;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterContentProvider;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterHeaderElement;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.ViewerUtils;

public class SessionContentProvider extends TreeContentProvider {

    private Viewer viewer;

    private TreeContentProvider additionalContentProvider;

    private HeaderElement sessionHeaderElement;
    private HeaderElement contentHeaderElement;

    private Roster currentRoster;
    private ISarosSession currentSession;

    private FollowingActivitiesManager followingTracker;

    @Inject
    private EditorManager editorManager;

    @Inject
    private AwarenessInformationCollector collector;

    public SessionContentProvider(TreeContentProvider additionalContent) {
        SarosPluginContext.initComponent(this);

        this.additionalContentProvider = additionalContent;

        editorManager.addSharedEditorListener(sharedEditorListener);
    }

    private final IFollowModeChangesListener followModeChangesListener = new IFollowModeChangesListener() {

        @Override
        public void followModeChanged() {
            ViewerUtils.refresh(viewer, true);
            // FIXME expand the sessionHeaderElement not the whole viewer
            ViewerUtils.expandAll(viewer);
        }
    };

    private final ISharedEditorListener sharedEditorListener = new AbstractSharedEditorListener() {
        @Override
        public void followModeChanged(User user, boolean isFollowed) {
            ViewerUtils.update(viewer, new UserElement(user, editorManager,
                collector), null);
        }

        @Override
        public void activeEditorChanged(final User user, SPath path) {
            SWTUtils.runSafeSWTAsync(null, new Runnable() {
                @Override
                public void run() {
                    if (viewer.getControl().isDisposed())
                        return;

                    viewer.refresh();
                    viewer.getControl().redraw();
                }
            });
        }

        @Override
        public void colorChanged() {

            // does not force a redraw
            // ViewerUtils.refresh(viewer, true);

            SWTUtils.runSafeSWTAsync(null, new Runnable() {
                @Override
                public void run() {
                    if (viewer.getControl().isDisposed())
                        return;

                    viewer.getControl().redraw();
                }
            });
        }
    };

    // TODO call update and not refresh
    private final RosterListener rosterListener = new AbstractRosterListener() {
        // update nicknames
        @Override
        public void entriesUpdated(Collection<String> addresses) {
            ViewerUtils.refresh(viewer, true);
        }

        // update away icons
        @Override
        public void presenceChanged(Presence presence) {
            ViewerUtils.refresh(viewer, true);
        }
    };

    /*
     * as we have a filter installed that will hide contacts from the contact
     * list that are currently part of the session we must currently do a full
     * refresh otherwise the viewer is not correctly updated
     */
    private final ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void userLeft(User user) {
            // UserElement userElement = getUserElement(currentRoster, user);
            // if (userElement != null)
            // ViewerUtils.remove(viewer, userElement);
            ViewerUtils.refresh(viewer, true);
        }

        @Override
        public void userJoined(User user) {
            // UserElement userElement = getUserElement(currentRoster, user);
            // if (userElement != null)
            // ViewerUtils.add(viewer, sessionHeaderElement, userElement);

            ViewerUtils.refresh(viewer, true);

            // FIXME expand the sessionHeaderElement not the whole viewer
            ViewerUtils.expandAll(viewer);
        }

        @Override
        public void permissionChanged(User user) {
            ViewerUtils.update(viewer, new UserElement(user, editorManager,
                collector), null);
        }
    };

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.viewer = viewer;

        final Roster oldRoster = getRoster(oldInput);

        final Roster newRoster = currentRoster = getRoster(newInput);

        final ISarosSession oldSession = getSession(oldInput);

        final ISarosSession newSession = currentSession = getSession(newInput);

        if (followingTracker != null)
            followingTracker.removeListener(followModeChangesListener);

        if (additionalContentProvider != null)
            additionalContentProvider.inputChanged(viewer,
                getContent(oldInput), getContent(newInput));

        if (oldRoster != null)
            oldRoster.removeRosterListener(rosterListener);

        if (oldSession != null)
            oldSession.removeListener(sharedProjectListener);

        disposeHeaderElements();

        if (!(newInput instanceof SessionInput))
            return;

        createHeaders((SessionInput) newInput);

        if (newRoster != null)
            newRoster.addRosterListener(rosterListener);

        if (newSession != null) {
            newSession.addListener(sharedProjectListener);

            followingTracker = (FollowingActivitiesManager) newSession
                .getComponent(FollowingActivitiesManager.class);

            if (followingTracker != null)
                followingTracker.addListener(followModeChangesListener);
        }

    }

    private void disposeHeaderElements() {
        if (sessionHeaderElement != null)
            sessionHeaderElement.dispose();

        if (contentHeaderElement != null)
            contentHeaderElement.dispose();

        sessionHeaderElement = null;
        contentHeaderElement = null;
    }

    // TODO abstract !
    private void createHeaders(SessionInput input) {

        sessionHeaderElement = new SessionHeaderElement(viewer.getControl()
            .getFont(), input, editorManager, collector);

        if (additionalContentProvider instanceof RosterContentProvider) {
            contentHeaderElement = new RosterHeaderElement(viewer.getControl()
                .getFont(), (RosterContentProvider) additionalContentProvider,
                (Roster) input.getCustomContent());
        } else if (additionalContentProvider instanceof MDNSContentProvider) {
            contentHeaderElement = new MDNSHeaderElement(viewer.getControl()
                .getFont(), (MDNSContentProvider) additionalContentProvider,
                (MDNSService) input.getCustomContent());
        }
    }

    @Override
    public void dispose() {
        if (currentSession != null)
            currentSession.removeListener(sharedProjectListener);

        if (currentRoster != null)
            currentRoster.removeRosterListener(rosterListener);

        editorManager.removeSharedEditorListener(sharedEditorListener);

        if (followingTracker != null)
            followingTracker.removeListener(followModeChangesListener);

        if (additionalContentProvider != null)
            additionalContentProvider.dispose();

        disposeHeaderElements();

        /* ENSURE GC */
        currentSession = null;
        currentRoster = null;
        editorManager = null;
        additionalContentProvider = null;
        followingTracker = null;
    }

    /**
     * Returns {@link RosterGroup}s followed by {@link RosterEntry}s which don't
     * belong to any {@link RosterGroup}.
     */
    @Override
    public Object[] getElements(Object inputElement) {

        if (!(inputElement instanceof SessionInput))
            return new Object[0];

        List<Object> elements = new ArrayList<Object>();

        if (sessionHeaderElement != null)
            elements.add(sessionHeaderElement);

        if (contentHeaderElement != null)
            elements.add(contentHeaderElement);

        return elements.toArray();
    }

    private ISarosSession getSession(Object input) {

        if (!(input instanceof SessionInput))
            return null;

        return ((SessionInput) input).getSession();
    }

    private Roster getRoster(Object input) {
        if (!(input instanceof SessionInput))
            return null;

        Object roster = ((SessionInput) input).getCustomContent();

        if (roster instanceof Roster)
            return (Roster) roster;

        return null;
    }

    private Object getContent(Object input) {
        if (!(input instanceof SessionInput))
            return null;

        return ((SessionInput) input).getCustomContent();
    }
}
