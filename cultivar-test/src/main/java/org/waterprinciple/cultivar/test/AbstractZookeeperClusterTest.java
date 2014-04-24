package org.waterprinciple.cultivar.test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.curator.test.TestingCluster;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.Beta;

@Beta
public abstract class AbstractZookeeperClusterTest {
    protected static final int CLUSTER_SIZE = 3;

    private static final long MAX_TEST_TIME_SECONDS = 15L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractZookeeperClusterTest.class);

    @Rule
    public final ExternalResource clusterSetup = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            super.before();

            testingCluster.start();
        }

        @Override
        protected void after() {
            super.after();

            try {
                testingCluster.close();
            } catch (IOException ex) {
                LOGGER.warn("Exception shutting down cluster.", ex);
            }
        }
    };

    @Rule
    public final Timeout timeout = new Timeout((int) TimeUnit.MILLISECONDS.convert(MAX_TEST_TIME_SECONDS,
            TimeUnit.SECONDS));

    protected final TestingCluster testingCluster = new TestingCluster(CLUSTER_SIZE);
}