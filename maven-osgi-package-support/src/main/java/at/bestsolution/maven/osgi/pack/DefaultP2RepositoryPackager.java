package at.bestsolution.maven.osgi.pack;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = P2RepositoryPackager.class)
public class DefaultP2RepositoryPackager implements P2RepositoryPackager{

//    @Requirement
//    private P2ApplicationLauncher launcher;



    @Override
    public void execute(GenerationParameters parameters) {
        System.out.println("###### HELLLLLLOOOOO");
        InvocationRequest request = new DefaultInvocationRequest();



    }
}
