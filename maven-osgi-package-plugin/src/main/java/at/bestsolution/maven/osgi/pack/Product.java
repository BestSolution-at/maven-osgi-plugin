/*******************************************************************************
 * Copyright (c) 2017 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *******************************************************************************/
package at.bestsolution.maven.osgi.pack;

import java.util.Map;

public class Product {
	public String uid;
	public String id;
	public String name;
	public String application;
	public String version;
	public boolean useFeatures;
	public boolean includeLaunchers;
	public LauncherArgs launcherArgs;
	public Launcher launcher;
	protected Map<String, Integer> startLevels;
}
