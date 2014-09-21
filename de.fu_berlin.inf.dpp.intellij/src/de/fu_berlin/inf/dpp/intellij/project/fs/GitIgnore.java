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

package de.fu_berlin.inf.dpp.intellij.project.fs;

import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IVcsIgnore;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.eclipse.jgit.lib.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;

import static org.eclipse.jgit.ignore.IgnoreNode.MatchResult;
import static org.eclipse.jgit.ignore.IgnoreNode.MatchResult.NOT_IGNORED;

public final class GitIgnore implements IVcsIgnore {

    @Override public boolean isIgnored(IResource resource) {

        if (isInDotGitDirectory(resource)) {
            return true;
        }

        IResource directoryContainingFileToCheck;
        switch (resource.getType()) {
        case IResource.FILE:
            directoryContainingFileToCheck = resource.getParent() == null ? resource.getProject() : resource.getParent();
            break;
        case IResource.FOLDER:
        case IResource.PROJECT:
            directoryContainingFileToCheck = resource;
            break;
        default:
            return false;
        }

        return descendInSearchOfGitIgnoreFile(
            resource,
            createPathSegmentsFromRootTo(directoryContainingFileToCheck));
    }

    private boolean isInDotGitDirectory(IResource resource) {
        return resource.getProjectRelativePath().toPortableString()
            .startsWith(Constants.DOT_GIT);
    }

    private Iterator<IResource> createPathSegmentsFromRootTo(IResource resource) {
        LinkedList<IResource> files = new LinkedList<IResource>();
        IResource currentResource = resource;
        files.addFirst(currentResource);

        while (currentResource.getParent() != null) {
            IResource parentResource = currentResource.getParent();
            files.addFirst(parentResource);
            currentResource = parentResource;
        }
        files.addFirst(resource.getProject());

        return files.iterator();
    }

    private boolean descendInSearchOfGitIgnoreFile(IResource fileToCheck, Iterator<IResource> pathSegments) {
        return pathSegments.hasNext()
            ? checkAgainstCurrentGitIgnoreAndDescendIfNecessary(fileToCheck, pathSegments)
            : false;
    }

    private boolean checkAgainstCurrentGitIgnoreAndDescendIfNecessary(IResource fileToCheck, Iterator<IResource> pathSegments) {
        IFile currentGitIgnore = new FileImp(
            ((ProjectImp)fileToCheck.getProject()),
            new File(pathSegments.next().getProjectRelativePath().toFile(), Constants.GITIGNORE_FILENAME));

        if (currentGitIgnore.exists()) {
            switch (getMatchResult(fileToCheck, currentGitIgnore)) {
            case CHECK_PARENT:
                return descendInSearchOfGitIgnoreFile(fileToCheck, pathSegments);
            case IGNORED:
                return true;
            case NOT_IGNORED:
            default:
                return false;
            }
        } else {
            return descendInSearchOfGitIgnoreFile(fileToCheck, pathSegments);
        }
    }

    private MatchResult getMatchResult(IResource fileToCheck, IFile currentGitIgnore) {
        try {
            InputStream in = currentGitIgnore.getContents();
            IgnoreNode ignoreNode = new IgnoreNode();
            ignoreNode.parse(in);
            String path = fileToCheck.getProjectRelativePath().toPortableString();
            boolean isDirectory = fileToCheck.getType() == IResource.FOLDER;
            return ignoreNode.isIgnored(path, isDirectory);
        } catch (IOException e) {
            return NOT_IGNORED;
        }
    }

}
