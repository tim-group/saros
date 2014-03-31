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

package de.fu_berlin.inf.dpp.core.filesystem;

import de.fu_berlin.inf.dpp.filesystem.*;
import de.fu_berlin.inf.dpp.intellij.mock.MockInitializer;
import de.fu_berlin.inf.dpp.intellij.project.Project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * //todo Factory to create adapters from Eclipse
 * {@link org.eclipse.core.resources.IResource resources} to Saros Core
 * {@linkplain de.fu_berlin.inf.dpp.filesystem.IResource resources}.
 */
public class ResourceAdapterFactory {

    public static IPath create(IPath path) {

        if (path == null) {
            return null;
        }

        // return new EclipsePathImpl(path);
        return null; // todo
    }

    public static IProject create(IProject project) {
        return (IProject) adapt(project);
    }

    public static IFile create(IFile file) {
        return (IFile) adapt(file);
    }

    public static IFolder create(IFolder folder) {
        return (IFolder) adapt(folder);
    }

    public static IResource create(IResource resource) {
        return adapt(resource);
    }

    /**
     * Converts a collection of Eclipse resources to Saros Core file system
     * resources. The elements contained in the returned list have the same
     * order as returned by the iterator of the collection.
     * 
     * @param resources
     *            collection of Eclipse resources
     * @return list which will the contain the converted resources or
     *         <code>null</code> if resources was <code>null</code>
     */
    public static List<IResource> convertTo(
        Collection<? extends IResource> resources) {
        if (resources == null) {
            return null;
        }

        List<IResource> out = new ArrayList<IResource>(resources.size());
        convertTo(resources, out);
        return out;
    }

    /**
     * Converts a collection of Saros Core file system resources to Eclipse
     * resources.The elements contained in the returned list have the same order
     * as returned by the iterator of the collection.
     * 
     * @param resources
     *            collection of Saros Core resources
     * @return list which will the contain the converted resources or
     *         <code>null</code> if resources was <code>null</code>
     */
    public static List<IResource> convertBack(
        Collection<? extends IResource> resources) {
        if (resources == null) {
            return null;
        }

        List<IResource> out = new ArrayList<IResource>(resources.size());
        convertBack(resources, out);
        return out;
    }

    /**
     * Converts a collection of Eclipse resources to Saros Core file system
     * resources.
     * 
     * @param in
     *            collection of Eclipse resources
     * @param out
     *            collection which will the contain the converted resources
     */
    public static void convertTo(Collection<? extends IResource> in,
        Collection<? super IResource> out) {

        for (IResource resource : in) {
            out.add(adapt(resource));
        }
    }

    /**
     * Converts a collection of Saros Core file system resources to Eclipse
     * resources.
     * 
     * @param in
     *            collection of Saros Core file system resources
     * @param out
     *            collection which will the contain the converted resources
     */
    public static void convertBack(Collection<? extends IResource> in,
        Collection<? super IResource> out) {

        // for (IResource resource : in)
        // out.add(((EclipseResourceImpl) resource).getDelegate());

        // todo
    }

    private static IResource adapt(IResource resource) {
        if (resource == null) {
            return null;
        }

        // todo
        // switch (resource.getType()) {
        // case IResource.FILE:
        // return new EclipseFileImpl(
        // (org.eclipse.core.resources.IFile) resource
        // .getAdapter(org.eclipse.core.resources.IFile.class));
        // case IResource.FOLDER:
        // return new EclipseFolderImpl(
        // (org.eclipse.core.resources.IFolder) resource
        // .getAdapter(org.eclipse.core.resources.IFolder.class));
        // case IResource.PROJECT:
        // return new EclipseProjectImpl(
        // (org.eclipse.core.resources.IProject) resource
        // .getAdapter(org.eclipse.core.resources.IProject.class));
        // default:
        // // TODO workspace if needed ?
        // return new EclipseResourceImpl(resource);
        // }

       // return MockInitializer.testProject;
        return null;
    }
}
