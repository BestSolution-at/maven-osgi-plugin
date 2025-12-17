package at.bestsolution.maven.osgi.targetplatform.lib.internal;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.pde.internal.core.feature.WorkspaceFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;

/**
 * Responsible for the extraction of the feature plugins from a feature.xml file.
 *
 */
class FeaturePluginExtractor {

    /**
     * Does the extraction of the feature plugins.
     * 
     * @param featureInputStream
     * @return
     */
    static Set<IFeaturePlugin> extractFeaturePlugins(InputStream featureInputStream) {

        WorkspaceFeatureModel fmodel = new WorkspaceFeatureModel(new FileWrapper(featureInputStream));

        fmodel.load();

        Map<String, IFeaturePlugin> map = new HashMap<>();
        Arrays.stream(fmodel.getFeature().getPlugins()).forEach(p -> map.put(p.getId(), p));
        return new HashSet<>(map.values());
    }

}
