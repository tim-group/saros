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

package de.fu_berlin.inf.dpp.invitation;

import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.NotIgnoredFilter;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GitIgnore implements VcsIgnore {

    private final Set<String> unignoredResources;

    public GitIgnore(Set<String> unignoredResources) {
        this.unignoredResources = Collections
            .unmodifiableSet(unignoredResources);
    }

    @Override
    public boolean isIgnored(IResource resource) {
        return !unignoredResources
            .contains(resource.getProjectRelativePath().toPortableString());
    }

    public static GitIgnore fromRootDir(String path) throws IOException {
        File gitDir = new File(path + "/.git");
        Repository repository = new FileRepository(gitDir);
        FileTreeIterator fileTreeIterator = new FileTreeIterator(repository);
        TreeWalk tw = new TreeWalk(repository);
        tw.setRecursive(true);
        tw.addTree(fileTreeIterator);
        tw.setFilter(new NotIgnoredFilter(0));

        Set<String> unignoredResources = new HashSet<String>();
        while (tw.next()) {
            unignoredResources.add(tw.getPathString());
        }
        return new GitIgnore(unignoredResources);
    }

    public static GitIgnore forProject(IProject project) throws IOException {
        return fromRootDir(project.getFullPath().toPortableString());
    }
}
