package at.bestsolution.maven.osgi.pack;

import org.codehaus.plexus.component.annotations.Component;

@Component(role = P2RepositoryPackager.class)
public class DefaultP2RepositoryPackager implements P2RepositoryPackager{

    @Override
    public void execute() {
        System.out.println("###### HELLLLLLOOOOO");
    }
}
