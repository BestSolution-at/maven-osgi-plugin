package de.zeiss.maven.osgi.targetplatform.lib.internal;

import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;

/**
 * Filters the dependencies according to a white list given as a text file.
 * 
 *
 */
public class FeaturePluginFilter implements Predicate<IFeaturePlugin> {

    private final Set<String> whiteList;

    public FeaturePluginFilter(InputStream resource) {
        this.whiteList = getWhiteList(resource);
    }

    private static Set<String> getWhiteList(InputStream whiteListFile) {
        Set<String> whiteList = new HashSet<>();
        try (Scanner sc = new Scanner(Channels.newChannel(whiteListFile))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                whiteList.add(line);
            }
        }
        return whiteList;
    }

    @Override
    public boolean test(IFeaturePlugin p) {
        return whiteList.contains(p.getId());
    }

}
