package at.bestsolution.maven.osgi.exec.test;

import java.io.File;

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;

import junit.framework.TestCase;

public class MVNExecOSGITest extends TestCase {
	@Test
	public void testExecOsgi() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(), "/exec-osgi");

		Verifier verifier = new Verifier(testDir.getAbsolutePath());

		verifier.executeGoal("test");
		verifier.resetStreams();
		
		verifier.verifyErrorFreeLog();
		verifier.assertFilePresent("command");
		
		verifier.setAutoclean(false);
		verifier.setSystemProperty("exec.mode", "restart");
		verifier.executeGoal("test");
		verifier.verifyErrorFreeLog();
	}
}
