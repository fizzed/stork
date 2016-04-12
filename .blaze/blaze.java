
import com.fizzed.blaze.Config;
import com.fizzed.blaze.Contexts;
import static com.fizzed.blaze.Systems.exec;
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
    
    public void demo_hellod() {
        exec("mvn", "package", "-am", "-pl", "stork-demo/stork-demo-hellod").run();
        exec("stork-demo-hellod", "--run")
            .path("stork-demo/stork-demo-hellod/target/stork/bin")
            .run();
    }
    
    public void demo_dropwizard() {
        exec("mvn", "package", "-am", "-pl", "stork-demo/stork-demo-dropwizard").run();
        exec("stork-demo-dropwizard", "--run")
            .path("stork-demo/stork-demo-dropwizard/target/stork/bin")
            .run();
    }

}
