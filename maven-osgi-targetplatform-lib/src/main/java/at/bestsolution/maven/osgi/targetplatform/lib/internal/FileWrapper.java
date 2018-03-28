package at.bestsolution.maven.osgi.targetplatform.lib.internal;

import java.io.InputStream;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Wraps an input stream.
 * 
 *
 */
class FileWrapper extends File {

    private final InputStream inputStream;

    protected FileWrapper(InputStream inputStream) {
        super(new Path("."), null);
        this.inputStream = inputStream;
    }

    @Override
    public IPath getLocation() {
        return new Path(".");
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public InputStream getContents(boolean force) throws CoreException {
        return inputStream;
    }

}
