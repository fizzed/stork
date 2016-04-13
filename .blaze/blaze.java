
import com.fizzed.blaze.Config;
import com.fizzed.blaze.Contexts;
import static com.fizzed.blaze.Systems.exec;
import org.slf4j.Logger;

public class blaze {
    private final Logger log = Contexts.logger();
    private final Config config = Contexts.config();
    
    private String getTestHost() {
        String host = config.value("host").getOr("");
        
        if (host.equals("")) {
            log.info("NOTE: you can limit which host your unit tests run on");
            log.info(" by adding a -Dhost=HOST to your command. Valid hosts");
            log.info(" are either 'local' or run 'vagrant status' for a list");
        } else {
            log.info("NOTE: limiting unit tests to host {}", host);
        }
        
        return host;
    }
    
    public void test_launcher() {
        String host = getTestHost();
        exec("mvn", "test", "-am", "-pl", "stork-launcher-test", "-Dhost=" + host).run();
    }

    public void test_deploy() {
        String host = getTestHost();
        exec("mvn", "test", "-am", "-pl", "stork-deploy", "-Dhost=" + host).run();
    }
    
    public void demo_launcher() {
        exec("mvn", "package", "-DskipTests=true", "-am", "-pl", "stork-cli").run();
        exec("stork-launcher", "-o", "target/stork-fake", "stork-cli/src/main/launchers")
            .path("stork-cli/target/stork/bin")
            .run();
    }
    
    public void demo_deploy() {
        String host = getTestHost();
        exec("mvn", "package", "-DskipTests=true", "-am", "-pl", "stork-cli").run();
        exec("stork-deploy", "-a", "stork-deploy/src/test/resources/fixtures/hello-console-1.2.4.tar.gz", "vagrant+ssh://" + host)
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
