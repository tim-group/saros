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

package de.fu_berlin.inf.dpp.core.util;

import de.fu_berlin.inf.dpp.core.exceptions.CoreException;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspaceRunnable;
import de.fu_berlin.inf.dpp.filesystem.*;
import de.fu_berlin.inf.dpp.util.Pair;
import de.fu_berlin.inf.dpp.util.StackTrace;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.zip.Adler32;

public class FileUtils
{

    private static Logger log = Logger.getLogger(FileUtils.class);

    private static final int BUFFER_SIZE = 32 * 1024;
    @Inject
    public static IWorkspace workspace;
    private FileUtils()
    {
        // no instantiation allowed
    }

    /**
     * Calculate Adler32 checksum for given file.
     *
     * @return checksum of file
     * @throws IOException if checksum calculation has been failed.
     */
    public static long checksum(IFile file) throws IOException
    {

        InputStream in;
        try
        {
            in = file.getContents();
        }
        catch (IOException e)
        {
            throw new IOException("failed to calculate checksum.", e);
        }

        byte[] buffer = new byte[BUFFER_SIZE];

        Adler32 adler = new Adler32();

        int read;

        try
        {
            while ((read = in.read(buffer)) != -1)
            {
                adler.update(buffer, 0, read);
            }
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }

        return adler.getValue();
    }

    /**
     * Makes the given file read-only (</code>readOnly == true</code>) or
     * writable (<code>readOnly == false</code>).
     *
     * @param file     the resource whose read-only attribute is to be set or removed
     * @param readOnly <code>true</code> to set the given file to be read-only,
     *                 <code>false</code> to make writable
     * @return The state before setting read-only to the given value.
     */
    public static boolean setReadOnly(IResource file, boolean readOnly)
    {

        IResourceAttributes attributes = file.getResourceAttributes();

        if (attributes == null)
        {
            // TODO Throw a FileNotFoundException and deal with it everywhere!
            log.error("File does not exist for setting readOnly == " + readOnly
                    + ": " + file, new StackTrace());
            return false;
        }
        boolean result = attributes.isReadOnly();

        // Already in desired state
        if (result == readOnly)
        {
            return result;
        }

        attributes.setReadOnly(readOnly);
        try
        {
            file.setResourceAttributes(attributes);
        }
        catch (IOException e)
        {
            // failure is not an option
            log.warn("Failed to set resource readonly == " + readOnly + ": "
                    + file);
        }
        return result;
    }

    /**
     * Writes the given input stream to the given file.
     * <p/>
     * This operation will removeAll a possible readOnly flag and re-set if after
     * the operation.
     *
     * @param input the input stream to write to the file
     * @param file  the file to create/overwrite
     * @throws CoreException if the file could not be written.
     * @blocking This operations blocks until the operation is reported as
     * finished by Eclipse.
     */
    public static void writeFile(InputStream input, IFile file,
            IProgressMonitor monitor) throws CoreException
    {
        if (file.exists())
        {
            updateFile(input, file, monitor);
        }
        else
        {
            createFile(input, file, monitor);
        }

    }

    /**
     * Move the file to the same location, adding the file extension "BACKUP" or
     * "_BACKUP_X" on file name where X is a number that matches a not used file
     * name.
     *
     * @param file    the {@link IFile} to rename
     * @param monitor a progress monitor to show progress to user
     * @throws CoreException
     * @throws FileNotFoundException
     */
    public static void backupFile(IFile file, IProgressMonitor monitor)
            throws CoreException, FileNotFoundException
    {

        if (!file.exists())
        {
            throw new FileNotFoundException();
        }

        IProject project = file.getProject();

        IPath originalBackupPath = file.getProjectRelativePath().addFileExtension("BACKUP");

        IPath backupPath = originalBackupPath;

        for (int i = 0; i < 1000; i++)
        {
            if (!project.exists(backupPath))
            {
                break;
            }

            backupPath = originalBackupPath.removeFileExtension()
                    .addFileExtension("BACKUP_" + i);
        }

        try
        {
            file.move(file.getFullPath().removeLastSegments(1).append(backupPath.lastSegment()), true);
        }
        catch (IOException e)
        {
            throw new CoreException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Creates the given file and any missing parent directories.
     * <p/>
     * This method will try to removeAll read-only settings on the parent
     * directories and reset them at the end of the operation.
     *
     * @pre the file must not exist. Use writeFile() for getting this cases
     * handled.
     */
    public static void createFile(final InputStream input, final IFile file,
            IProgressMonitor monitor) throws CoreException
    {

        IWorkspaceRunnable createFileProcedure = new IWorkspaceRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {
                // Make sure directory exists
                mkdirs(file);

                // Make sure that parent is writable
                IContainer parent = file.getParent();
                boolean wasReadOnly = false;
                if (parent != null)
                {
                    wasReadOnly = setReadOnly(parent, false);
                }

                try
                {
                    file.create(input, true);
                }
                catch (IOException e)
                {
                    throw new CoreException(e.getMessage(), e.getCause());
                }

                // Reset permissions on parent
                if (parent != null && wasReadOnly)
                {
                    setReadOnly(parent, true);
                }

            }
        };


        workspace.run(createFileProcedure, workspace.getRoot(),
                IWorkspace.AVOID_UPDATE, monitor);

    }

    /**
     * Updates the data in the file with the data from the given InputStream.
     *
     * @pre the file must exist
     */
    public static void updateFile(final InputStream input, final IFile file,
            IProgressMonitor monitor) throws CoreException
    {

        IWorkspaceRunnable replaceFileProcedure = new IWorkspaceRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {
                try
                {
                    file.setContents(input, true, true);
                }
                catch (IOException e)
                {
                    throw new CoreException(e.getMessage(), e.getCause());
                }
            }
        };


        workspace.run(replaceFileProcedure, workspace.getRoot(),
                IWorkspace.AVOID_UPDATE, monitor);
    }

    /**
     * Makes sure that the parent directories of the given IResource exist,
     * possibly removing write protection.
     */


    public static IFolder getParentFolder(IResource resource)
    {

        if (resource == null)
        {
            return null;
        }
        IContainer parent = resource.getParent();
        if (parent == null || parent.getType() != IResource.FOLDER)
        {
            return null;
        }
        return (IFolder) parent;
    }

    public static void create(final IFolder folder) throws CoreException
    {

        // if ((folder == null) || (folder.exists())) {
        // log.debug(".create() Creating folder not possible");
        // return;
        // }

        if (folder == null)
        {
            log.warn(".create() Creating folder not possible -  it is null");
            throw new IllegalArgumentException();
        }
        if (folder.exists())
        {
            log.debug(".create() Creating folder " + folder.getName()
                    + " not possible - it already exists");
            return;
        }
        IWorkspaceRunnable createFolderProcedure = new IWorkspaceRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {

                // recursively create folders until parent folder exists
                // or project root is reached
                IFolder parentFolder = getParentFolder(folder);
                if (parentFolder != null)
                {
                    create(parentFolder);
                }

                try
                {
                    folder.create(IResource.NONE, true);
                }
                catch (IOException e)
                {
                    throw new CoreException(e.getMessage(), e.getCause());
                }

                if (monitor.isCanceled())
                {
                    log.warn("Creating folder failed: " + folder);
                }

            }
        };


        workspace.run(createFolderProcedure, workspace.getRoot(),
                IWorkspace.AVOID_UPDATE, null);

    }

    public static void delete(final IResource resource) throws CoreException
    {
        if (!resource.exists())
        {
            log.warn("File not found for deletion: " + resource,
                    new StackTrace());
            return;
        }

        IWorkspaceRunnable deleteProcedure = new IWorkspaceRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {
                if (!resource.exists())
                {
                    return;
                }

                if (resource.getResourceAttributes() == null)
                {
                    return;
                }

                setReadOnly(resource, false);

                try
                {
                    resource.delete(IResource.FORCE | IResource.KEEP_HISTORY);
                }
                catch (IOException e)
                {
                    throw new CoreException(e.getMessage(), e.getCause());
                }

                if (monitor.isCanceled())
                {
                    log.warn("Removing resource failed: " + resource);
                }
            }
        };


        workspace.run(deleteProcedure, workspace.getRoot(),
                IWorkspace.AVOID_UPDATE, null);

    }

    /**
     * Moves the given {@link IResource} to the place, that is pointed by the
     * given {@link IPath}.
     * <p/>
     * This method excepts both variables to be relative to the workspace.
     *
     * @param destination Destination of moving the given resource.
     * @param source      Resource, that is going to be moved
     */
    public static void move(final IPath destination, final IResource source)
            throws CoreException
    {

        log.trace(".move(" + destination.toOSString() + " , "
                + source.getName() + ")");

        if (!source.isAccessible())
        {
            log.warn(".move Source file can not be accessed  "
                    + source.getFullPath());
            return;
        }

        IWorkspaceRunnable moveProcedure = new IWorkspaceRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {
                IPath absDestination = destination.makeAbsolute();

                try
                {
                    source.move(absDestination, false);
                }
                catch (IOException e)
                {
                    throw new CoreException(e.getMessage(), e.getCause());
                }

                if (monitor.isCanceled())
                {
                    log.warn("Moving resource failed (Cancel Button pressed).");
                }
            }
        };

        workspace.run(moveProcedure, workspace.getRoot(),
                IWorkspace.AVOID_UPDATE, null);

    }

    /**
     * Makes sure that the parent directories of the given IResource exist,
     * possibly removing write protection.
     */
    public static boolean mkdirs(IResource resource)
    {

        if (resource == null)
        {
            return true;
        }

        IFolder parent = getParentFolder(resource);
        if (parent == null || parent.exists())
        {
            return true;
        }

        IContainer root = parent;
        while (!root.exists())
        {
            IContainer temp = root.getParent();
            if (temp == null)
            {
                break;
            }
            root = temp;
        }
        boolean wasReadOnly = FileUtils.setReadOnly(root, false);

        try
        {
            create(parent);
        }
        catch (CoreException e)
        {
            log.error("Could not create Dir: " + parent.getFullPath());
            return false;
        }
        finally
        {
            if (wasReadOnly)
            {
                FileUtils.setReadOnly(root, true);
            }
        }
        return true;
    }


    /**
     * Calculates the total file count and size for all resources.
     *
     * @param resources      collection containing the resources that file sizes and file
     *                       count should be calculated
     * @param includeMembers <code>true</code> to include the members of resources that
     *                       represents a {@linkplain IContainer container}
     * @param flags          additional flags on how to process the members of containers
     * @return a pair containing the {@linkplain de.fu_berlin.inf.dpp.util.Pair#p file size} and
     * {@linkplain de.fu_berlin.inf.dpp.util.Pair#v file count} for the given resources
     */
    public static Pair<Long, Long> getFileCountAndSize(
            Collection<? extends IResource> resources, boolean includeMembers,
            int flags)
    {
        long totalFileSize = 0;
        long totalFileCount = 0;

        Pair<Long, Long> fileCountAndSize = new Pair<Long, Long>(0L, 0L);

        for (IResource resource : resources)
        {
            switch (resource.getType())
            {
                case IResource.FILE:
                    totalFileCount++;

                    try
                    {
                        long filesize = -1; //todo // EFS.getStore(resource.getLocationURI()).fetchInfo().getLength();

                        totalFileSize += filesize;
                    }
                    catch (Exception e)
                    {
                        log.warn(
                                "failed to retrieve file size of file "
                                        + resource.getLocationURI(), e
                        );
                    }
                    break;
                case IResource.PROJECT:
                case IResource.FOLDER:
                    if (!includeMembers)
                    {
                        break;
                    }

                    try
                    {
                        IContainer container = ((IContainer) resource
                                .getAdapter(IContainer.class));

                        Pair<Long, Long> subFileCountAndSize = FileUtils
                                .getFileCountAndSize(
                                        Arrays.asList(container.members(flags)),
                                        includeMembers, flags);

                        totalFileSize += subFileCountAndSize.p;
                        totalFileCount += subFileCountAndSize.v;

                    }
                    catch (Exception e)
                    {
                        log.warn("failed to process container: " + resource, e);
                    }
                    break;
                default:
                    break;
            }
        }
        fileCountAndSize.p = totalFileSize;
        fileCountAndSize.v = totalFileCount;
        return fileCountAndSize;
    }

    /**
     * Retrieves the content of a local file
     *
     * @param localFile
     * @return Byte array of the file contents. Is <code>null</code> if the file
     * does not exist or is out of sync, the reference points to no
     * file, or the conversion to a byte array failed.
     */
    public static byte[] getLocalFileContent(IFile localFile)
    {

        InputStream in = null;
        byte[] content = null;
        try
        {
            in = localFile.getContents();
        }
        catch (IOException e)
        {
            log.warn("could not get content of file " + localFile.getFullPath());
        }

        if (in == null)
        {
            return null;
        }


        try
        {
            content = IOUtils.toByteArray(in);
        }
        catch (IOException e)
        {
            log.warn("could not convert file content to byte array (file: "
                    + localFile.getFullPath() + ")");
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
        return content;
    }

}

