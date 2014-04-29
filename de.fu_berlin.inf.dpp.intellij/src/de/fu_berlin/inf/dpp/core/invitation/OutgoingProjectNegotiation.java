package de.fu_berlin.inf.dpp.core.invitation;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.core.context.ISarosContext;
import de.fu_berlin.inf.dpp.core.editor.IEditorManager;
import de.fu_berlin.inf.dpp.core.exceptions.CoreException;
import de.fu_berlin.inf.dpp.core.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.core.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.core.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.core.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.ISubMonitor;
import de.fu_berlin.inf.dpp.core.project.IChecksumCache;
import de.fu_berlin.inf.dpp.core.zip.FileZipper;
import de.fu_berlin.inf.dpp.core.zip.ZipProgressMonitor;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.invitation.FileList;
import de.fu_berlin.inf.dpp.invitation.FileListFactory;
import de.fu_berlin.inf.dpp.invitation.ProjectNegotiationData;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.extensions.ProjectNegotiationMissingFilesExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.ProjectNegotiationOfferingExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.StartActivityQueuingRequest;
import de.fu_berlin.inf.dpp.net.internal.extensions.StartActivityQueuingResponse;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.picocontainer.annotations.Inject;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CancellationException;

public class OutgoingProjectNegotiation extends ProjectNegotiation
{

    private static Logger log = Logger
            .getLogger(OutgoingProjectNegotiation.class);

    private List<IProject> projects;

    private ISarosSession sarosSession;

    private final static Random PROCESS_ID_GENERATOR = new Random();

    @Inject
    private IEditorManager editorManager;

    @Inject
    private IChecksumCache checksumCache;

    private SarosPacketCollector remoteFileListResponseCollector;

    private SarosPacketCollector startActivityQueuingResponseCollector;

    public OutgoingProjectNegotiation(JID to, ISarosSession sarosSession,
            List<IProject> projects, ISarosContext sarosContext)
    {
        super(to, sarosSession.getID(), sarosContext);

        this.processID = String.valueOf(PROCESS_ID_GENERATOR.nextLong());
        this.sarosSession = sarosSession;
        this.projects = projects;
    }

    public Status start(IProgressMonitor monitor)
    {

        createCollectors();
        File zipArchive = null;

        List<File> zipArchives = new ArrayList<File>();

        observeMonitor(monitor);

        Exception exception = null;
        try
        {
            if (fileTransferManager == null)
            // FIXME: the logic will try to send this to the remote contact
            {
                throw new IOException("not connected to a XMPP server");
            }

            List<ProjectNegotiationData> data = createProjectExchangeInfoList(projects, monitor);

            sendFileList(data, monitor);

            monitor.subTask("");

            List<FileList> fileLists = getRemoteFileList(monitor);
            monitor.subTask("");

            /*
             * FIXME why do we unlock the editors here when we are going to
             * block ourself in the next call ?!
             */
            editorManager.setAllLocalOpenedEditorsLocked(false);

            List<StartHandle> stoppedUsers = null;
            try
            {
                stoppedUsers = stopUsers(monitor);
                monitor.subTask("");

                sendAndAwaitActivityQueueingActivation(monitor);
                monitor.subTask("");

                /*
                 * inform all listeners that the peer has started queuing and
                 * can therefore process IResourceActivities now
                 *
                 * TODO this needs a review as this is called inside the
                 * "blocked" section and so it is not allowed to send resource
                 * activities at this time. Maybe change the description of the
                 * listener interface ?
                 */

                User user = sarosSession.getUser(peer);

                if (user == null)
                {
                    throw new LocalCancellationException(null,
                            CancelOption.DO_NOT_NOTIFY_PEER);
                }

                sarosSession.userStartedQueuing(user);

                zipArchives = createProjectArchives(fileLists, monitor);

                monitor.subTask("");
            }
            finally
            {
                if (stoppedUsers != null)
                {
                    startUsers(stoppedUsers);
                }
            }

            checkCancellation(CancelOption.NOTIFY_PEER);

            if (zipArchives.size() > 0)
            {

                // pack all archive files into one big archive
                zipArchive = File.createTempFile("SarosSyncArchive", ".zip");
                try
                {
                    FileZipper.zipFiles(zipArchives, zipArchive, false,
                            new ZipProgressMonitor(monitor, zipArchives.size(),
                                    false)
                    );

                    monitor.subTask("");
                    monitor.done();

                }
                catch (OperationCanceledException e)
                {
                    throw new LocalCancellationException();
                }
                zipArchives.add(zipArchive);

                sendArchive(zipArchive, peer, ARCHIVE_TRANSFER_ID + processID,
                        monitor);
          }

            User user = sarosSession.getUser(peer);

            if (user == null)
            {
                throw new LocalCancellationException(null,
                        CancelOption.DO_NOT_NOTIFY_PEER);
            }

             sarosSession.userFinishedProjectNegotiation(user);

        }
        catch (Exception e)
        {
            e.printStackTrace();

            exception = e;
        }
        finally
        {

            for (File archive : zipArchives)
            {
                if (!archive.delete())
                {
                    log.warn("could not archive file: "
                            + archive.getAbsolutePath());
                }
            }
            deleteCollectors();
            monitor.done();
        }

        return terminateProcess(exception);
    }

    private void sendFileList(
            List<ProjectNegotiationData> projectExchangeInfos,
            IProgressMonitor monitor) throws IOException,
            SarosCancellationException
    {

        /*
         * FIXME display the remote side something that will it receive
         * something in the near future
         */

        checkCancellation(CancelOption.NOTIFY_PEER);

        log.debug(this + " : sending file list");

        /*
         * file lists are normally very small so we "accept" the circumstance
         * that this step cannot be cancelled.
         */

        monitor.setTaskName("Sending file list...");



        /*
         * The Remote receives this message at the InvitationHandler which calls
         * the SarosSessionManager which creates a IncomingProjectNegotiation
         * instance and pass it to the installed callback handler (which in the
         * current implementation opens a wizard on the remote side)
         */
        ProjectNegotiationOfferingExtension offering = new ProjectNegotiationOfferingExtension(
                sessionID, processID, projectExchangeInfos);

        transmitter
                .sendToSessionUser(ISarosSession.SESSION_CONNECTION_ID, peer,
                        ProjectNegotiationOfferingExtension.PROVIDER.create(offering));
    }

    /**
     * Retrieve the peer's partial file list and remember which files need to be
     * sent to that user
     *
     * @param monitor
     * @throws IOException
     * @throws SarosCancellationException
     */
    private List<FileList> getRemoteFileList(IProgressMonitor monitor)
            throws IOException, SarosCancellationException
    {

        log.debug(this + " : waiting for remote file list");

        monitor.beginTask("Waiting for " + peer.getName()
                + " to choose project(s) location", IProgressMonitor.UNKNOWN);

        checkCancellation(CancelOption.NOTIFY_PEER);

        Packet packet = collectPacket(remoteFileListResponseCollector,
                60 * 60 * 1000);

        if (packet == null)
        {
            throw new LocalCancellationException("received no response from "
                    + peer + " while waiting for the file list",
                    CancelOption.DO_NOT_NOTIFY_PEER
            );
        }

        List<FileList> remoteFileLists = ProjectNegotiationMissingFilesExtension.PROVIDER
                .getPayload(packet).getFileLists();

        log.debug(this + " : remote file list has been received");

        checkCancellation(CancelOption.NOTIFY_PEER);

        monitor.done();

        return remoteFileLists;
    }

    @Override
    public Map<String, String> getProjectNames()
    {
        Map<String, String> result = new HashMap<String, String>();
        for (IProject project : projects)
        {
            result.put(sarosSession.getProjectID(project), project.getName());
        }

        return result;
    }

    @Override
    public String getProcessID()
    {
        return this.processID;
    }

    @Override
    protected void executeCancellation()
    {
        if (sarosSession.getRemoteUsers().isEmpty())
        {
            sessionManager.stopSarosSession();
        }
    }

    private List<StartHandle> stopUsers(IProgressMonitor monitor)
            throws SarosCancellationException
    {
        Collection<User> usersToStop;

        /*
         * Make sure that all users are fully registered, otherwise failures
         * might occur while a user is currently joining and has not fully
         * initialized yet.
         *
         * See also OutgoingSessionNegotiation#completeInvitation
         */

        synchronized (CancelableProcess.SHARED_LOCK)
        {
            usersToStop = new ArrayList<User>(sarosSession.getUsers());
        }

        log.debug(this + " : stopping users " + usersToStop);

        List<StartHandle> startHandles;

        monitor.beginTask("Locking the session...", IProgressMonitor.UNKNOWN);

        /*
         * FIMXE the StopManager should use a timeout as it can happen that a
         * user leaves the session during the stop request. Currently it is up
         * to the user to press the cancel button because the StopManager did
         * not check if the user already left the session.
         *
         * Stefan Rossbach: The StopManager should not check for the absence of
         * a user and so either retry again or just stop the sharing (which
         * currently would lead to a broken session because we have no proper
         * cancellation logic !
         */
        try
        {
            startHandles = sarosSession.getStopManager().stop(usersToStop,
                    "Synchronizing invitation");
        }
        catch (CancellationException e)
        {
            e.printStackTrace();

            checkCancellation(CancelOption.NOTIFY_PEER);
            return null;
        }

        monitor.done();
        return startHandles;
    }

    private void startUsers(List<StartHandle> startHandles)
    {
        for (StartHandle startHandle : startHandles)
        {
            log.debug(this + " : restarting user " + startHandle.getUser());
            startHandle.start();
        }
    }

    /**
     * @param fileLists a list of file lists containing the files to archive
     * @return List of project archives
     */
    private List<File> createProjectArchives(List<FileList> fileLists,
            IProgressMonitor monitor) throws IOException,
            SarosCancellationException
    {

        log.debug(this + " : creating archive(s)");

        ISubMonitor subMonitor = monitor.convert(monitor);
        /* SubMonitor subMonitor = SubMonitor.convert(monitor,
     "Creating project archives...", fileLists.size());*/

        /*
         * Use editorManager.saveText() because the EditorAPI.saveProject() will
         * not save files which were modified in the background. This is what
         * happens for example if a user edits a file which is not opened by the
         * local user.
         *
         * Stefan Rossbach: this will still fail if a user edited a file and
         * then closes the editor without saving it.
         */

        // FIXME this throws a NPE if the session has already been stopped
        for (SPath path : editorManager.getOpenEditorsOfAllParticipants())
        {
            editorManager.saveText(path);
        }

        checkCancellation(CancelOption.NOTIFY_PEER);

        List<File> archivesToSend = new LinkedList<File>();

        for (FileList fileList : fileLists)
        {

            File projectArchive = createProjectArchive(subMonitor.newChild(1),
                    fileList.getPaths(), fileList.getProjectID());

            if (projectArchive != null)
            {
                archivesToSend.add(projectArchive);
            }

        }

        subMonitor.done();

        return archivesToSend;
    }

    private File createProjectArchive(IProgressMonitor monitor,
            List<IPath> toSend, String projectID) throws IOException,
            SarosCancellationException
    {

        IProject project = sarosSession.getProject(projectID);
        /*
         * TODO: Ask the user whether to save the resources, but only if they
         * have changed. How to ask Eclipse whether there are resource changes?
         * if (outInvitationUI.confirmProjectSave(peer)) getOpenEditors =>
         * filter per Project => if dirty ask to save
         */
        //todo????
//        EditorAPI.saveProject(((EclipseProjectImpl) project).getDelegate(),
//                false);

        String prefix = projectID + projectIDDelimiter;

        File tempArchive = null;

        try
        {
            if (toSend.size() > 0)
            {
                tempArchive = File.createTempFile(prefix, ".zip");

                FileZipper.createProjectZipArchive(project, toSend,
                        tempArchive, new ZipProgressMonitor(monitor, toSend.size(),
                                true)
                );
            }
        }
        catch (OperationCanceledException e)
        {
            throw new LocalCancellationException();
        }

        monitor.done();

        return tempArchive;
    }

    private void createCollectors()
    {
        remoteFileListResponseCollector = xmppReceiver
                .createCollector(ProjectNegotiationMissingFilesExtension.PROVIDER
                        .getPacketFilter(sessionID, processID));

        startActivityQueuingResponseCollector = xmppReceiver
                .createCollector(StartActivityQueuingResponse.PROVIDER
                        .getPacketFilter(sessionID, processID));
    }

    private void deleteCollectors()
    {
        remoteFileListResponseCollector.cancel();
        startActivityQueuingResponseCollector.cancel();
    }

    private void sendArchive(File archive, JID remoteContact,
            String transferID, IProgressMonitor monitor)
            throws SarosCancellationException, IOException
    {

        log.debug(this + " : sending archive");
        monitor.beginTask("Sending archive file...", 100);

        assert fileTransferManager != null;

        try
        {
            OutgoingFileTransfer transfer = fileTransferManager
                    .createOutgoingFileTransfer(remoteContact.toString());

            transfer.sendFile(archive, transferID);
            monitorFileTransfer(transfer, monitor);
        }
        catch (XMPPException e)
        {
            throw new IOException(e.getMessage(), e);
        }

        monitor.done();

        log.debug(this + " : archive send");
    }

    /**
     * Method to create list of ProjectExchangeInfo.
     *
     * @param projectsToShare List of projects to share
     */
    private List<ProjectNegotiationData> createProjectExchangeInfoList(
            List<IProject> projectsToShare, IProgressMonitor monitor)
            throws IOException, LocalCancellationException
    {

        /*
         * FIXME must be calculated while the session is blocked !
         */
        ISubMonitor subMonitor = monitor.convert(monitor);
//        ISubMonitor subMonitor = ISubMonitor
//                .convert(
//                        monitor,
//                        "Creating file list and calculating file checksums. This may take a while...",
//                        projectsToShare.size());

        List<ProjectNegotiationData> pInfos = new ArrayList<ProjectNegotiationData>(
                projectsToShare.size());

        for (IProject project : projectsToShare)
        {

            if (monitor.isCanceled())
            {
                throw new LocalCancellationException(null,
                        CancelOption.DO_NOT_NOTIFY_PEER);
            }
            try
            {
                String projectID = sarosSession.getProjectID(project);
                String projectName = project.getName();

                List<IResource> projectResources = sarosSession
                        .getSharedResources(project);

                FileList projectFileList = FileListFactory.createFileList(
                        project,
                        projectResources,
                        checksumCache, sarosSession.useVersionControl(),
                        subMonitor.newChild(1));

                projectFileList.setProjectID(projectID);
                boolean partial = !sarosSession.isCompletelyShared(project);

                ProjectNegotiationData pInfo = new ProjectNegotiationData(projectID, projectName, partial, projectFileList);

                pInfos.add(pInfo);

            }
            catch (CoreException e)
            {
                e.printStackTrace();
                /*
                 * avoid that the error is send to remote side (which is default
                 * for IOExceptions) at this point because the remote side has
                 * no existing project negotiation yet
                 */
                localCancel(e.getMessage(), CancelOption.DO_NOT_NOTIFY_PEER);
                // throw to log this error in the CancelableProcess class
                throw new IOException(e.getMessage(), e);
            }
        }

        subMonitor.done();

        return pInfos;
    }

    /**
     * Sends an activity queuing request to the remote side and awaits the
     * confirmation of the request.
     *
     * @param monitor
     */
    private void sendAndAwaitActivityQueueingActivation(IProgressMonitor monitor)
            throws IOException, SarosCancellationException
    {

        monitor.beginTask("Waiting for " + peer.getName()
                        + " to perform additional initialization...",
                IProgressMonitor.UNKNOWN
        );

        transmitter.sendToSessionUser(ISarosSession.SESSION_CONNECTION_ID,
                getPeer(), StartActivityQueuingRequest.PROVIDER
                        .create(new StartActivityQueuingRequest(sessionID, processID))
        );

        Packet packet = collectPacket(startActivityQueuingResponseCollector,
                PACKET_TIMEOUT);

        if (packet == null)
        {
            throw new LocalCancellationException("received no response from "
                    + peer + " while waiting to finish additional initialization",
                    CancelOption.DO_NOT_NOTIFY_PEER
            );
        }

        monitor.done();
    }

    @Override
    public String toString()
    {
        return "OPN [remote side: " + peer + "]";
    }
}
