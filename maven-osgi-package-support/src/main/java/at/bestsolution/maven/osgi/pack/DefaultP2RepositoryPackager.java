package at.bestsolution.maven.osgi.pack;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component(role = P2RepositoryPackager.class)
public class DefaultP2RepositoryPackager implements P2RepositoryPackager{

//    @Requirement
//    private P2ApplicationLauncher launcher;

    @Override
    public void execute(GenerationParameters parameters) {
        System.out.println("###### HELLLLLLOOOOO");
    }
}
