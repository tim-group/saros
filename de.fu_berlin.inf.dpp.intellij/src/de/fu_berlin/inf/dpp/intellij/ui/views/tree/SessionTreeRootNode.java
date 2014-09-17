/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.intellij.ui.views.tree;

import com.intellij.util.ui.UIUtil;
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.core.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.ui.util.IconManager;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;
import org.picocontainer.annotations.Inject;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Session tree root node.
 */
public class SessionTreeRootNode extends DefaultMutableTreeNode {
    public static final String TREE_TITLE = "Sessions";
    public static final String TREE_TITLE_NO_SESSIONS = "No Sessions Running";

    private final SarosTreeView treeView;
    private final Map<ISarosSession, DefaultMutableTreeNode> sessionNodeList = new HashMap<ISarosSession, DefaultMutableTreeNode>();
    private final Map<User, DefaultMutableTreeNode> userNodeList = new HashMap<User, DefaultMutableTreeNode>();
    private final DefaultTreeModel treeModel;

    @Inject
    private ISarosSessionManager sessionManager;

    private final ISharedProjectListener userListener = new AbstractSharedProjectListener() {
        @Override
        public void userLeft(final User user) {
            UIUtil.invokeAndWaitIfNeeded(new Runnable() {
                @Override
                public void run() {
                    removeUserNode(user);
                }
            });

        }

        @Override
        public void userJoined(final User user) {
            UIUtil.invokeAndWaitIfNeeded(new Runnable() {
                @Override
                public void run() {
                    addUserNode(user);
                }
            });
        }
    };

    private final ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarted(final ISarosSession newSarosSession) {
            UIUtil.invokeAndWaitIfNeeded(new Runnable() {
                @Override
                public void run() {
                    newSarosSession.addListener(userListener);
                    createSessionNode(newSarosSession);
                }
            });
        }

        @Override
        public void sessionEnded(final ISarosSession oldSarosSession) {

            UIUtil.invokeAndWaitIfNeeded(new Runnable() {
                @Override
                public void run() {
                    oldSarosSession.removeListener(userListener);
                    removeSessionNode(oldSarosSession);
                }
            });
        }

        @Override
        public void projectAdded(final String projectID) {
            UIUtil.invokeAndWaitIfNeeded(new Runnable() {
                @Override
                public void run() {
                    addProjectNode(projectID);
                }
            });
        }
    };

    public SessionTreeRootNode(SarosTreeView treeView) {
        super(treeView);
        SarosPluginContext.initComponent(this);
        this.treeView = treeView;
        treeModel = (DefaultTreeModel) this.treeView.getModel();
        setUserObject(TREE_TITLE_NO_SESSIONS);

        //register listener
        sessionManager.addSarosSessionListener(sessionListener);
    }

    private void createSessionNode(ISarosSession newSarosSession) {

        DefaultMutableTreeNode newSession = new DefaultMutableTreeNode(
            new SessionInfo(newSarosSession));

        sessionNodeList.put(newSarosSession, newSession);

        treeModel.insertNodeInto(newSession, this, getChildCount());
        treeModel.reload(this);

        add(newSession);

        setUserObject(TREE_TITLE);

        if (!newSarosSession.isHost()) {
            addUserNode(newSarosSession.getLocalUser());
        }

        treeView.expandRow(1);
    }

    private void removeSessionNode(ISarosSession oldSarosSession) {

        DefaultMutableTreeNode nSession = sessionNodeList.get(oldSarosSession);
        if (nSession != null) {
            treeModel.removeNodeFromParent(nSession);
            sessionNodeList.remove(oldSarosSession);
            removeAllUserNodes();
        }

        if (sessionNodeList.size() == 0) {
            setUserObject(TREE_TITLE_NO_SESSIONS);
        }

        treeModel.reload(this);
        treeView.expandRow(2);
    }

    private void addProjectNode(String projectID) {

        //iterate projects in sessions
        for (DefaultMutableTreeNode nSession : sessionNodeList.values()) {
            ISarosSession session = ((SessionInfo) nSession.getUserObject())
                .getSession();
            IProject p = session.getProject(projectID);
            if (p != null) {
                ProjectInfo projInfo;
                if (session.isCompletelyShared(p)) {
                    projInfo = new ProjectInfo(p);
                } else {
                    projInfo = new ProjectInfo(p,
                        session.getSharedResources(p));

                }
                DefaultMutableTreeNode nProject = new DefaultMutableTreeNode(
                    projInfo);
                treeModel.insertNodeInto(nProject, nSession,
                    nSession.getChildCount());

                treeModel.reload(nSession);
            }
        }
    }

    private void addUserNode(User user) {
        DefaultMutableTreeNode nUser = new DefaultMutableTreeNode(
            new UserInfo(user));
        userNodeList.put(user, nUser);
        treeModel.insertNodeInto(nUser, this, this.getChildCount());

        treeView.getContactTreeRootNode()
            .hideContact(user.getJID().getBareJID().toString());

        treeModel.reload(this);
    }

    private void removeUserNode(User user) {
        DefaultMutableTreeNode nUser = userNodeList.get(user);
        if (nUser != null) {
            remove(nUser);
            userNodeList.remove(user);

            treeView.getContactTreeRootNode()
                .showContact(user.getJID().getBareJID().toString());

            treeModel.reload();
        }

    }

    private void removeAllUserNodes() {
        for (DefaultMutableTreeNode nUser : userNodeList.values()) {
            removeUserNode(((UserInfo) nUser.getUserObject()).getUser());
        }

        userNodeList.clear();
    }

    protected class SessionInfo extends LeafInfo {
        private final ISarosSession session;

        private SessionInfo(ISarosSession session) {
            super(session.getHost().getNickname(), IconManager.CONTACT_ONLINE_ICON);
            this.session = session;
        }

        public ISarosSession getSession() {
            return session;
        }

        public String toString() {
            return "Host " + title;
        }

    }

    protected class UserInfo extends LeafInfo {
        private final User user;

        public UserInfo(User user) {
            super(user.getNickname(), IconManager.CONTACT_ONLINE_ICON);
            this.user = user;
        }

        public User getUser() {
            return user;
        }
    }

    protected class ProjectInfo extends LeafInfo {
        private final IProject project;
        private List<IResource> resList;

        public ProjectInfo(IProject project) {
            super(project.getName());
            this.project = project;
        }

        public ProjectInfo(IProject project, List<IResource> resources) {
            this(project);
            resList = resources;

        }

        public IProject getProject() {
            return project;
        }

        public String toString() {
            if (resList != null) {
                StringBuilder sbOut = new StringBuilder();
                sbOut.append(project.getName());
                sbOut.append(" : ");
                for (IResource res : resList) {
                    if (res.getType() == IResource.FILE) {
                        sbOut.append(res.getName());
                        sbOut.append("; ");
                    }
                }

                return sbOut.toString();
            } else {
                return project.getName();
            }
        }
    }
}
