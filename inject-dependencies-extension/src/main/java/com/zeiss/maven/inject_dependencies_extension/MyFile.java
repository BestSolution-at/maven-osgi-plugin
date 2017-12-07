package com.zeiss.maven.inject_dependencies_extension;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class MyFile extends File {

	java.io.File file;
	
	protected MyFile(java.io.File file) {
		super(new Path("."),null);
		this.file = file;
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
	
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new CoreException(null);
		}
	}

}
