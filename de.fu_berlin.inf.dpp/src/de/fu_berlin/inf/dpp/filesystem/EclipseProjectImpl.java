package de.fu_berlin.inf.dpp.filesystem;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;

public class EclipseProjectImpl extends EclipseContainerImpl implements
    IProject {

    EclipseProjectImpl(org.eclipse.core.resources.IProject delegate) {
        super(delegate);
    }

    @Override
    public IResource findMember(IPath path) {
        org.eclipse.core.runtime.IPath myPath = ((EclipsePathImpl) path)
            .getDelegate();
        org.eclipse.core.resources.IResource resource = getDelegate()
            .findMember(myPath);

        return ResourceAdapterFactory.create(resource);
    }

    @Override
    public IFile getFile(String name) {
        return new EclipseFileImpl(getDelegate().getFile(name));
    }

    @Override
    public IFile getFile(IPath path) {
        org.eclipse.core.runtime.IPath myPath = ((EclipsePathImpl) path)
            .getDelegate();

        return new EclipseFileImpl(getDelegate().getFile(myPath));
    }

    @Override
    public IFolder getFolder(String name) {
        return new EclipseFolderImpl(getDelegate().getFolder(name));
    }

    @Override
    public IFolder getFolder(IPath path) {
        org.eclipse.core.runtime.IPath myPath = ((EclipsePathImpl) path)
            .getDelegate();

        return new EclipseFolderImpl(getDelegate().getFolder(myPath));
    }

    @Override
    public boolean isOpen() {
        return getDelegate().isOpen();
    }

    @Override
    public void open() throws IOException {
        try {
            getDelegate().open(null);
        } catch (CoreException e) {
            throw new IOException(e);
        }
    }

    /**
     * Returns the original {@link org.eclipse.core.resources.IProject IProject}
     * object.
     * 
     * @return
     */
    @Override
    public org.eclipse.core.resources.IProject getDelegate() {
        return (org.eclipse.core.resources.IProject) delegate;
    }
}
