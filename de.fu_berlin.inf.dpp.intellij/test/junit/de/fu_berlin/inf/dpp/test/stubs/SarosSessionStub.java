package de.fu_berlin.inf.dpp.test.stubs;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;

import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.session.*;
import de.fu_berlin.inf.dpp.session.User.Permission;
import de.fu_berlin.inf.dpp.synchronize.StopManager;

public class SarosSessionStub implements ISarosSession {

    private boolean hasWriteAccess;
    private boolean useVersionControl;

    public void setUseVersionControl(boolean b) {
        useVersionControl = b;
    }

    public void setWriteAccess(boolean b) {
        hasWriteAccess = b;
    }

    @Override
    public boolean useVersionControl() {
        return useVersionControl;
    }

    @Override
    public void start() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    @Override
    public void setPermission(User user, Permission permission) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void sendActivity(List<User> recipient, IActivity activity) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    @Override
    public void removeUser(User user) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    @Override
    public void removeListener(ISharedProjectListener listener) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    @Override
    public void removeActivityProducerAndConsumer(
        IActivityProducerAndConsumer producerAndConsumer) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    @Override
    public boolean isShared(IResource resource) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public boolean isHost() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public boolean hasExclusiveWriteAccess() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public boolean hasWriteAccess() {
        return hasWriteAccess;
    }

    @Override
    public void initiatePermissionChange(User user, Permission newPermission)
        throws CancellationException, InterruptedException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    @Override
    public User getUser(JID jid) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public ITransmitter getTransmitter() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public JID getResourceQualifiedJID(JID jid) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public List<User> getRemoteUsers() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public List<User> getRemoteUsersWithReadOnlyAccess() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public Set<IProject> getProjects() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public String getProjectID(IProject project) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public IProject getProject(String projectID) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public List<User> getUsers() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public List<User> getUsersWithReadOnlyAccess() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public User getLocalUser() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public User getHost() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public List<User> getUsersWithWriteAccess() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public ConcurrentDocumentServer getConcurrentDocumentServer() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public ConcurrentDocumentClient getConcurrentDocumentClient() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public void exec(List<IActivityDataObject> activityDataObjects) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    @Override
    public void addActivityProvider(IActivityProvider provider)
    {

    }

    @Override
    public void removeActivityProvider(IActivityProvider provider)
    {

    }

    @Override
    public void addUser(User user) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void addSharedProject(IProject project, String projectID) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    @Override
    public void addListener(ISharedProjectListener listener) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    @Override
    public void addActivityProducerAndConsumer(
        IActivityProducerAndConsumer producerAndConsumer) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void execQueuedActivities() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public void addSharedResources(IProject project, String projectID,
        List<IResource> dependentResources) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    @Override
    public List<IResource> getSharedResources() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public HashMap<IProject, List<IResource>> getProjectResourcesMapping() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public boolean isCompletelyShared(IProject project) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public void stopQueue() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public void startQueue() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public List<IResource> getSharedResources(IProject project) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public void addSharedResources(IProject project, String projectID,
        List<IResource> dependentResources, JID jid) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public void addProjectOwnership(String projectID, IProject project,
        JID ownerJID) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public void removeProjectOwnership(String projectID, IProject project,
        JID ownerJID) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public StopManager getStopManager() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public void kickUser(User user) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public void changeColor(int colorID) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public Set<Integer> getUnavailableColors() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public void enableQueuing(String projectId) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public void disableQueuing() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public void userStartedQueuing(User user) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    @Override
    public void userFinishedProjectNegotiation(User user) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    @Override
    public boolean userHasProject(User user, IProject project) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    @Override
    public String getID() {
        throw new RuntimeException("Unexpected call to Stub");
    }
}
