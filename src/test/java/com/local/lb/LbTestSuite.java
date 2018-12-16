package com.local.lb;

import com.local.lb.balancing.algorythm.PeakFactorTest;
import com.local.lb.balancing.algorythm.RoundRobiinTest;
import com.local.lb.connection.ConnectionPoolTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
        PeakFactorTest.class,
        RoundRobiinTest.class,
        ConnectionPoolTest.class
})

public class LbTestSuite {
}
