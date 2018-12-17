package demo.sequential;

import com.local.lb.balancing.algorythm.RoundRobiin;
import com.local.lb.model.Host;
import demo.GenericSeqRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

public class RoundRobinSeq {
    private static final List<Host> hosts = Arrays.asList(new Host("TEST_1"),
            new Host("TEST_2"),
            new Host("TEST_3"),
            new Host("TEST_4"));
    private static final Logger LOGGER = LogManager.getLogger(GenericSeqRunner.class);

    public static void main(String[] args) {
        GenericSeqRunner genericSeqRunner = new GenericSeqRunner(new RoundRobiin());
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        GenericSeqRunner.registerMBeans(server,hosts);
        genericSeqRunner.runSeqTask(hosts);

    }


}
