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
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.core.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.ui.resource.IconManager;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Session tree part
 */
public class SessionTree extends AbstractTree {
    public static final String TREE_TITLE = "Sessions";
    public static final String TREE_TITLE_NO_SESSIONS = "No Sessions Running";

    private RootTree rootTree;
    private Map<ISarosSession, DefaultMutableTreeNode> sessionNodeList = new HashMap<ISarosSession, DefaultMutableTreeNode>();
    private Map<User, DefaultMutableTreeNode> userNodeList = new HashMap<User, DefaultMutableTreeNode>();
    private DefaultTreeModel treeModel;

    private ISharedProjectListener userListener = new AbstractSharedProjectListener() {
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


    private ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
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


    /**
     * @param parent
     */
    public SessionTree(RootTree parent) {
        super(parent);

        this.rootTree = parent;

        this.treeModel = (DefaultTreeModel) rootTree.getJtree().getModel();
        setUserObject(new CategoryInfo(TREE_TITLE_NO_SESSIONS));

        create();

        //register listener
        Saros.getInstance().getSessionManager().addSarosSessionListener(sessionListener);
    }

    protected void create() {

    }

    public CategoryInfo getUserObject() {
        return (CategoryInfo) super.getUserObject();
    }

    public void setTitle(String title) {
        getUserObject().title = title;
    }


    private void createSessionNode(ISarosSession newSarosSession) {

        DefaultMutableTreeNode nSession = new DefaultMutableTreeNode(new SessionInfo(newSarosSession));

        this.sessionNodeList.put(newSarosSession, nSession);

        treeModel.insertNodeInto(nSession, this, this.getChildCount());
        treeModel.reload(this);

        add(nSession);

        setTitle(TREE_TITLE);

        if (!newSarosSession.isHost()) {
            addUserNode(newSarosSession.getLocalUser());
        }

        rootTree.getJtree().expandRow(1);

    }


    private void removeSessionNode(ISarosSession oldSarosSession) {

        DefaultMutableTreeNode nSession = sessionNodeList.get(oldSarosSession);
        if (nSession != null) {
            treeModel.removeNodeFromParent(nSession);
            sessionNodeList.remove(oldSarosSession);
            removeAllUserNodes();
        }

        if (sessionNodeList.size() == 0) {
            setUserObject(new CategoryInfo(TREE_TITLE_NO_SESSIONS));
        }

        treeModel.reload(this);
        rootTree.getJtree().expandRow(2);
    }


    private void addProjectNode(String projectID) {

        //iterate projects in sessions
        for (DefaultMutableTreeNode nSession : sessionNodeList.values()) {
            ISarosSession session = ((SessionInfo) nSession.getUserObject()).getSession();
            IProject p = session.getProject(projectID);
            if (p != null) {
                ProjectInfo projInfo;
                if (session.isCompletelyShared(p)) {
                    projInfo = new ProjectInfo(p);
                } else {
                    projInfo = new ProjectInfo(p, session.getSharedResources(p));

                }
                DefaultMutableTreeNode nProject = new DefaultMutableTreeNode(projInfo);
                treeModel.insertNodeInto(nProject, nSession, nSession.getChildCount());

                treeModel.reload(nSession);
            }
        }
    }

    private void addUserNode(User user) {
        DefaultMutableTreeNode nUser = new DefaultMutableTreeNode(new UserInfo(user));
        userNodeList.put(user, nUser);
        treeModel.insertNodeInto(nUser, this, this.getChildCount());

        saros.getMainPanel().getSarosTree().getContactTree().hideContact(user.getJID().getBareJID().toString());

        treeModel.reload(this);
    }

    private void removeUserNode(User user) {
        DefaultMutableTreeNode nUser = userNodeList.get(user);
        if (nUser != null) {
            remove(nUser);
            userNodeList.remove(user);

            saros.getMainPanel().getSarosTree().getContactTree().showContact(user.getJID().getBareJID().toString());

            treeModel.reload();
        }

    }

    private void removeAllUserNodes() {
        for (DefaultMutableTreeNode nUser : userNodeList.values()) {
            removeUserNode(((UserInfo) nUser.getUserObject()).getUser());
        }

        userNodeList.clear();
    }

    /**
     * Class to keep session information
     */
    protected class SessionInfo extends LeafInfo {
        private ISarosSession session;

        private SessionInfo(ISarosSession session) {
            super(session.getID(), session.getHost().getNickname());
            this.session = session;
        }

        public ImageIcon getIcon() {
            return IconManager.CONTACT_ONLINE_ICON;
        }

        public ISarosSession getSession() {
            return session;
        }

        public String toString() {
            return "Host " + title;
        }

    }

    protected class UserInfo extends LeafInfo {
        private User user;
        private boolean isOnline = false;

        public UserInfo(User user) {
            super(user.getNickname(), user.getNickname());
            this.user = user;
            this.setIcon(IconManager.CONTACT_ONLINE_ICON);
        }

        public User getUser() {
            return user;
        }

        public boolean isOnline() {
            return isOnline;
        }

        public void setOnline(boolean isOnline) {
            this.isOnline = isOnline;
        }
    }

    /**
     *
     */
    protected class ProjectInfo extends LeafInfo {
        private IProject project;
        private List<IResource> resList;

        public ProjectInfo(IProject project) {
            super(project.getFullPath().toString(), project.getName());
            this.project = project;
        }

        public ProjectInfo(IProject project, List<IResource> resources) {
            this(project);
            this.resList = resources;

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
