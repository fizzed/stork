
import com.fizzed.blaze.Config;
import com.fizzed.blaze.Contexts;
import static com.fizzed.blaze.Systems.exec;
import static com.fizzed.blaze.Systems.which;
import java.nio.file.Path;
import org.slf4j.Logger;

public class blaze {
    private final Logger log = Contexts.logger();
    private final Config config = Contexts.config();
    
    public void test_launcher() {
        // passthru 'host' system property
        String host = config.value("host").getOr("");
        
        if (host.equals("")) {
            log.info("You can limit which hosts unit tests run on with a -Dhost=name param");
        }

        exec("mvn", "test", "-am", "-pl", "stork-launcher-test", "-Dhost=" + host).run();
    }

    public void test_deploy() {
        // passthru 'host' system property
        String host = config.value("host").getOr("");

        if (host.equals("")) {
            log.info("You can limit which hosts unit tests run on with a -Dhost=name param");
        }

        exec("mvn", "test", "-am", "-pl", "stork-deploy", "-Dtarget=" + host).run();
    }
    
    public void demo_deploy() {
        /**
        // passthru 'host' system property
        String host = config.value("host").getOr("");
        if (host.equals("")) {
        log.info("You can limit which hosts unit tests run on with a -Dhost=name param");
        }
         */ 
        exec("mvn", "package", "-DskipTests=true", "-am", "-pl", "stork-cli").run();
        exec("stork-deploy", "-a", "stork-deploy/src/test/resources/fixtures/hello-console-1.2.4.tar.gz", "vagrant+ssh://ubuntu1404")
            .path("stork-cli/target/stork/bin")
            .run();
    }
    
    public void demo_hellod() {
        exec("mvn", "package", "-DskipTests=true", "-am", "-pl", "stork-demo/stork-demo-hellod").run();
        exec("stork-demo-hellod", "--run")
            .path("stork-demo/stork-demo-hellod/target/stork/bin")
            .run();
    }
    
    public void demo_dropwizard() {
        exec("mvn", "package", "-DskipTests=true", "-am", "-pl", "stork-demo/stork-demo-dropwizard").run();
        exec("stork-demo-dropwizard", "--run")
            .path("stork-demo/stork-demo-dropwizard/target/stork/bin")
            .run();
    }

}
