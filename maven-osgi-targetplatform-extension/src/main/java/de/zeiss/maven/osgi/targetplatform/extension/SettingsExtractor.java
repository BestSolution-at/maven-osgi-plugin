/*******************************************************************************
 * Copyright (c) 2017 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Fahrmeyer - initial API and implementation
 *******************************************************************************/
package de.zeiss.maven.osgi.targetplatform.extension;

import java.util.List;

import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.settings.Proxy;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = EventSpy.class, hint = "getsettings")
public class SettingsExtractor extends AbstractEventSpy {



    public SettingsExtractor() {
    }

    @Override
    public void init(Context context) throws Exception {
        super.init(context);
    }

    @Override
    public void onEvent(Object event) throws Exception {

        if (event instanceof ExecutionEvent) {
            onEvent((ExecutionEvent) event);
        }
    }

    private void onEvent(ExecutionEvent event) throws Exception {
        switch (event.getType()) {

        case ProjectDiscoveryStarted:
            List<Proxy> proxies = event.getSession().getRequest().getProxies();

            proxies.stream().filter(p -> p.isActive()).findFirst().ifPresent(p -> {
                System.setProperty("http.proxyHost", p.getHost());
                System.setProperty("http.proxyPort", String.valueOf(p.getPort()));
            });

            break;

        default:

        }
    }

}
