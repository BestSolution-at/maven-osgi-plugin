package de.zeiss.maven.osgi.targetplatform.lib;

import java.util.List;

import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;

/**
 * Extract the proxy settings from the maven settings.
 * 
 *
 */
public class ProxySettingsExtractor {

    /**
     * Get proxy from settings
     *
     * @param settings
     * @return proxy or null if none matching
     */
    public static Proxy getProxy(final Settings settings) {
        if (settings == null)
            return null;
        List<Proxy> proxies = settings.getProxies();
        if (proxies == null || proxies.isEmpty())
            return null;

        // search active proxy
        for (Proxy proxy : proxies)
            if (proxy.isActive() && ("http".equalsIgnoreCase(proxy.getProtocol()) || "https".equalsIgnoreCase(proxy.getProtocol())))
                return proxy;
        return null;
    }
}
