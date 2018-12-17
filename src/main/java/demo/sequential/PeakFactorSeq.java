package demo.sequential;

import com.local.lb.balancing.algorythm.PeakFactor;
import com.local.lb.model.Host;
import demo.GenericSeqRunner;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

class PeakFactorSeq {
    private static final List<Host> hosts = Arrays.asList(new Host("TEST_1"),
            new Host("TEST_2"),
            new Host("TEST_3"),
            new Host("TEST_4"));

    public static void main(String[] args) {

        GenericSeqRunner genericSeqRunner = new GenericSeqRunner(new PeakFactor());
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        genericSeqRunner.registerMBeans(server,hosts);
        genericSeqRunner.runSeqTask(hosts);

    }
}
