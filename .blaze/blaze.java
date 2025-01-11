import com.fizzed.blaze.Config;
import com.fizzed.blaze.Contexts;
import static com.fizzed.blaze.Contexts.fail;
import static com.fizzed.blaze.Systems.exec;
import static com.fizzed.blaze.Systems.which;
import static java.util.Arrays.asList;

import com.fizzed.blaze.Task;
import com.fizzed.buildx.Buildx;
import com.fizzed.buildx.Target;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;

public class blaze {
    private final Logger log = Contexts.logger();
    private final Config config = Contexts.config();
    
    private String getTestHost(boolean required) {
        String host = config.value("host").getOr("");
        
        if (host.equals("")) {
            log.info("NOTE: you can limit which host your unit tests run on");
            log.info(" by adding a -Dhost=HOST to your command. Valid hosts");
            log.info(" are either 'local' or run 'vagrant status' for a list");
            if (required) {
                fail("host is required");
            }
        } else {
            log.info("NOTE: limiting unit tests to host {}", host);
        }
        
        return host;
    }
    
    public void test_launcher() {
        String host = getTestHost(false);
        exec("mvn", "test", "-am", "-pl", "stork-launcher-test", "-Dhost=" + host).run();
    }

    public void test_deploy() {
        String host = getTestHost(false);
        exec("mvn", "test", "-am", "-pl", "stork-deploy", "-Dhost=" + host).run();
    }
    
    public void demo_launcher() {
        exec("mvn", "package", "-DskipTests=true", "-am", "-pl", "stork-cli").run();
        exec("stork-launcher", "-o", "target/stork-fake", "stork-cli/src/main/launchers")
            .workingDir("stork-cli/target/stork/bin")
            .run();
    }
    
    public void demo_deploy() {
        String host = getTestHost(true);
        exec("mvn", "package", "-DskipTests=true", "-am", "-pl", "stork-cli").run();
        exec("stork-deploy", "-a", "stork-deploy/src/test/resources/fixtures/hello-console-1.2.4.tar.gz", "vagrant+ssh://" + host)
            .workingDir("stork-cli/target/stork/bin")
            .run();
    }
    
    public void demo_hellod() {
        exec("mvn", "package", "-DskipTests=true", "-am", "-pl", "stork-demo/stork-demo-hellod").run();
        final Path command = which("stork-demo-hellod")
            .path("stork-demo/stork-demo-hellod/target/stork/bin")
            .run();
        exec(command, "--run")
            .run();
    }
    
    public void demo_dropwizard() {
        exec("mvn", "package", "-DskipTests=true", "-am", "-pl", "stork-demo/stork-demo-dropwizard").run();
        final Path command = which("stork-demo-dropwizard")
            .path("stork-demo/stork-demo-dropwizard/target/stork/bin")
            .run();
        exec(command, "--run")
            .env("EXTRA_JAVA_ARGS", "-Da=1")
            .run();
    }

    private final List<Target> crossTestTargets = asList(
        new Target("linux", "x64").setTags("test").setHost("bmh-build-x64-linux-latest"),
        new Target("linux", "arm64").setTags("test").setHost("bmh-build-arm64-linux-latest"),
        new Target("linux", "riscv64").setTags("test").setHost("bmh-build-riscv64-linux-latest"),
        new Target("linux_musl", "x64").setTags("test").setHost("bmh-build-x64-linux-musl-latest"),
        new Target("macos", "x64").setTags("test").setHost("bmh-build-x64-macos-latest"),
        new Target("macos", "arm64").setTags("test").setHost("bmh-build-arm64-macos-latest"),
        new Target("windows", "x64").setTags("test").setHost("bmh-build-x64-windows-latest"),
        new Target("windows", "arm64").setTags("test").setHost("bmh-build-arm64-windows-latest"),
        new Target("freebsd", "x64").setTags("test").setHost("bmh-build-x64-freebsd-latest"),
        new Target("openbsd", "x64").setTags("test").setHost("bmh-build-x64-openbsd-latest")
    );

    @Task(order = 53)
    public void cross_tests() throws Exception {
        new Buildx(crossTestTargets)
            .tags("test")
            .execute((target, project) -> {
                project.action("mvn", "clean", "test")
                    .run();
            });
    }

}
