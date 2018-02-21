package de.zeiss.maven.osgi.targetplatform.plugin.internal;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

public class UpdateSiteAccessorTest {

	private static final String JAR_PREFIX = "features/org.eclipse.fx.target.feature_";
	private static final String JAR_PATH = "features/org.eclipse.fx.target.feature_3.0.0.201706050601.jar";
	private static final String SITE_TEST_XML = "/site-test.xml";

	@Test
	public void testExctractRelativeUrl() {

		String jarPath = UpdateSiteAccessor.extractRelativeTargetPlatformFeatureJarUrl(getClass().getResourceAsStream(SITE_TEST_XML),
				JAR_PREFIX);

		assertThat(jarPath, equalTo(JAR_PATH));

	}

	@Test
	public void testReadRelativeUrl() {

		try {
			URL resource = getClass().getResource(SITE_TEST_XML);
			String jarPath = UpdateSiteAccessor.readRelativeTargetPlatformFeatureJarUrl(resource.toURI().toString(), JAR_PREFIX);
			assertThat(jarPath, equalTo(JAR_PATH));
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}

	}
}
