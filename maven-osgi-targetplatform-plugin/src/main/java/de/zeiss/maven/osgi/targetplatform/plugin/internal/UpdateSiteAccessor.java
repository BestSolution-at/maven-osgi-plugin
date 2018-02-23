package de.zeiss.maven.osgi.targetplatform.plugin.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.pde.internal.core.isite.ISiteFeature;
import org.eclipse.pde.internal.core.site.WorkspaceSiteModel;

public class UpdateSiteAccessor {

    public static String readRelativeTargetPlatformFeatureJarUrl(String siteUrl, String targetJarUrlPrefix) {

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

    public static String extractRelativeTargetPlatformFeatureJarUrl(InputStream siteInputStream, String urlPrefix) {
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
