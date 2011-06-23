package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.IRefactorC;
import de.fu_berlin.inf.dpp.stf.server.util.Util;

public final class RefactorC extends StfRemoteObject implements IRefactorC {

    private static final RefactorC INSTANCE = new RefactorC();

    private IRemoteBotTreeItem treeItem;
    private TreeItemType type;

    public static RefactorC getInstance() {
        return INSTANCE;
    }

    public void setTreeItem(IRemoteBotTreeItem treeItem) {
        this.treeItem = treeItem;
    }

    public void setTreeItemType(TreeItemType type) {
        this.type = type;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void moveClassTo(String targetProject, String targetPkg)
        throws RemoteException {
        moveTo(SHELL_MOVE, OK, Util.getPkgNodes(targetProject, targetPkg));
    }

    public void rename(String newName) throws RemoteException {
        switch (type) {
        case JAVA_PROJECT:
            rename(SHELL_RENAME_JAVA_PROJECT, OK, newName);
            break;
        case PKG:
            rename(SHELL_RENAME_PACKAGE, OK, newName);
            break;
        case CLASS:
            rename(SHELL_RENAME_COMPIIATION_UNIT, FINISH, newName);
            break;
        default:
            rename(SHELL_RENAME_RESOURCE, OK, newName);
            break;
        }
    }

    /**************************************************************
     * 
     * inner functions
     * 
     **************************************************************/
    private void rename(String shellTitle, String buttonName, String newName)
        throws RemoteException {
        treeItem.contextMenus(MENU_REFACTOR, MENU_RENAME).click();
        IRemoteBotShell shell = RemoteWorkbenchBot.getInstance().shell(
            shellTitle);
        shell.activate();
        shell.bot().textWithLabel(LABEL_NEW_NAME).setText(newName);
        RemoteWorkbenchBot.getInstance().shell(shellTitle).bot()
            .button(buttonName).waitUntilIsEnabled();
        shell.bot().button(buttonName).click();
        // if (bot().isShellOpen("Rename Compilation Unit")) {
        // bot().shell("Rename Compilation Unit").bot().button(buttonName)
        // .waitUntilIsEnabled();
        // bot().shell("Rename Compilation Unit").bot().button(buttonName)
        // .click();
        // }
        if (RemoteWorkbenchBot.getInstance().isShellOpen(shellTitle))
            RemoteWorkbenchBot.getInstance().waitUntilShellIsClosed(shellTitle);
    }

    private void moveTo(String shellTitle, String buttonName, String... nodes)
        throws RemoteException {
        treeItem.contextMenus(MENU_REFACTOR, MENU_MOVE).click();
        RemoteWorkbenchBot.getInstance().shell(shellTitle)
            .confirmWithTree(buttonName, nodes);
        RemoteWorkbenchBot.getInstance().waitUntilShellIsClosed(shellTitle);
    }

}
