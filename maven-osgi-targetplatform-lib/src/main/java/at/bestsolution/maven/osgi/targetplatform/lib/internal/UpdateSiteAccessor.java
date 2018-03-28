package at.bestsolution.maven.osgi.targetplatform.lib.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.pde.internal.core.isite.ISiteFeature;
import org.eclipse.pde.internal.core.site.WorkspaceSiteModel;

import at.bestsolution.maven.osgi.targetplatform.lib.LoggingSupport;

/**
 * Responsible for accessing the update site and providing the path to the target platform jar that is contained in the site.xml.
 * 
 * 
 *
 */
class UpdateSiteAccessor {

    static String readRelativeTargetPlatformFeatureJarUrl(String siteUrl, String targetJarUrlPrefix) {

        try {
            URL url = new URL(siteUrl);
            URLConnection connection = url.openConnection();
            InputStream siteInputStream = connection.getInputStream();
            return extractRelativeTargetPlatformFeatureJarUrl(siteInputStream, targetJarUrlPrefix);
        } catch (IOException e) {

            LoggingSupport.logErrorMessage(e.getMessage(), e);

            return null;
        }
    }

    static String extractRelativeTargetPlatformFeatureJarUrl(InputStream siteInputStream, String urlPrefix) {
        WorkspaceSiteModel model = new WorkspaceSiteModel(new FileWrapper(siteInputStream));
        model.load();
        for (ISiteFeature f : model.getSite().getFeatures()) {
            if (f.getURL().startsWith(urlPrefix)) {
                return f.getURL();
            }
        }
        return null;
    }

}
