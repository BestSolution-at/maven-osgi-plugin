package de.zeiss.maven.osgi.targetplatform.lib.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.net.URISyntaxException;

import org.junit.Test;

public class JarAccessorTest {

    private static final String FEATURE_TEST_XML = "feature-test.xml";
    private static final String FEATURE_TEST_JAR = "/feature-test.jar";

    @Test
    public void testRead() {
        try {
            InputStream inputStream = JarAccessor.readEntry(JarAccessorTest.class.getResource(FEATURE_TEST_JAR).toURI().toString(), FEATURE_TEST_XML);

            assertNotNull(inputStream);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            fail();
        }
    }
}
