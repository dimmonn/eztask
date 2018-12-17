package demo.sequential;

import com.local.lb.balancing.algorythm.RoundRobiin;
import com.local.lb.model.Host;
import demo.GenericSeqRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoundRobinSeq {
    private static final List<Host> hosts = Arrays.asList(new Host("TEST_1"),
            new Host("TEST_2"),
            new Host("TEST_3"),
            new Host("TEST_4"));
    private static final Logger LOGGER = LogManager.getLogger(GenericSeqRunner.class);

    public static void main(String[] args) {

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        List<ObjectName> objectNames = new ArrayList<>();
        hosts.forEach(e -> {
            try {

                try {
                    ObjectName mbeanName = new ObjectName("host" + e.getName(), "isDamaged", "false");
                    objectNames.add(mbeanName);
                    if (!server.isRegistered(mbeanName)) {
                        server.registerMBean(e, mbeanName);

                    }
                } catch (MalformedObjectNameException exc) {
                    LOGGER.error("object is malformed", exc);
                }


            } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException ex) {
                LOGGER.error("failed to register mbean", ex);
            }

        });


        GenericSeqRunner genericSeqRunner = new GenericSeqRunner(new RoundRobiin());
        genericSeqRunner.runSeqTask(hosts);

    }


}
