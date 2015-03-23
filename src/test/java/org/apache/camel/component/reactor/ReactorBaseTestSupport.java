package org.apache.camel.component.reactor;

import org.apache.camel.test.AvailablePortFinder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mmcalabro
 */
public class ReactorBaseTestSupport extends CamelTestSupport {

    protected static final Logger logger = LoggerFactory.getLogger(ReactorBaseTestSupport.class);

    protected static String LOG_STRING = "STEP-%d [headers=${in.headers}, body=${in.body}]";
    protected static int port;
    protected int step = 0;

    @BeforeClass
    public static void initPort() throws Exception {
        port = AvailablePortFinder.getNextAvailable(20000);
    }

    public static int getPort() {
        return port;
    }
}
