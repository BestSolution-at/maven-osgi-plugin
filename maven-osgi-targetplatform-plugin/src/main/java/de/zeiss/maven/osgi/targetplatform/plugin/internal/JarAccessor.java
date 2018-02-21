package de.zeiss.maven.osgi.targetplatform.plugin.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarFile;

public class JarAccessor {

	private static final String JAR_URL_SUFFIX = "!/";
	private static final String JAR_URL_PREFIX = "jar:";

	public static InputStream readEntry(String jarUrl, String entryName) {

		try {
			URL url = new URL(convertToJarUrl(jarUrl));
			JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
			JarFile jarFile = jarConnection.getJarFile();
			return jarFile.getInputStream(jarFile.getEntry(entryName));
		} catch (IOException e) {
			e.printStackTrace();
			   // TODO: use logger
		}
		return null;
	}

	private static String convertToJarUrl(String httpUrl) {
		return JAR_URL_PREFIX + httpUrl + JAR_URL_SUFFIX;
	}

}
