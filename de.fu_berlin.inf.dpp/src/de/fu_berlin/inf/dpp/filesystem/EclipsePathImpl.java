package de.fu_berlin.inf.dpp.filesystem;

import java.io.File;

public class EclipsePathImpl implements IPath {

    private final org.eclipse.core.runtime.IPath delegate;

    EclipsePathImpl(org.eclipse.core.runtime.IPath delegate) {
        if (delegate == null)
            throw new NullPointerException("delegate is null");

        this.delegate = delegate;
    }

    @Override
    public IPath append(IPath path) {
        org.eclipse.core.runtime.IPath myPath = ((EclipsePathImpl) path)
            .getDelegate();

        return new EclipsePathImpl(delegate.append(myPath));
    }

    @Override
    public boolean isAbsolute() {
        return delegate.isAbsolute();
    }

    @Override
    public boolean isPrefixOf(IPath path) {
        org.eclipse.core.runtime.IPath myPath = ((EclipsePathImpl) path)
            .getDelegate();

        return delegate.isPrefixOf(myPath);
    }

    @Override
    public String toOSString() {
        return delegate.toOSString();
    }

    @Override
    public String toPortableString() {
        return delegate.toPortableString();
    }

    @Override
    public String lastSegment() {
        return delegate.lastSegment();
    }

    @Override
    public boolean hasTrailingSeparator() {
        return delegate.hasTrailingSeparator();
    }

    @Override
    public int segmentCount() {
        return delegate.segmentCount();
    }

    @Override
    public IPath removeLastSegments(int count) {
        return new EclipsePathImpl(delegate.removeLastSegments(count));
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public String[] segments() {
        return delegate.segments();
    }

    @Override
    public IPath append(String path) {
        return new EclipsePathImpl(delegate.append(path));
    }

    @Override
    public IPath addTrailingSeparator() {
        return new EclipsePathImpl(delegate.addTrailingSeparator());
    }

    @Override
    public IPath addFileExtension(String extension) {
        return new EclipsePathImpl(delegate.addFileExtension(extension));
    }

    @Override
    public IPath removeFileExtension() {
        return new EclipsePathImpl(delegate.removeFileExtension());
    }

    @Override
    public IPath makeAbsolute() {
        return new EclipsePathImpl(delegate.makeAbsolute());
    }

    @Override
    public File toFile() {
        return delegate.toFile();
    }

    /**
     * Returns the original {@link org.eclipse.core.runtime.IPath IPath} object.
     * 
     * @return
     */
    public org.eclipse.core.runtime.IPath getDelegate() {
        return delegate;
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof EclipsePathImpl))
            return false;

        return delegate.equals(((EclipsePathImpl) obj).delegate);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
