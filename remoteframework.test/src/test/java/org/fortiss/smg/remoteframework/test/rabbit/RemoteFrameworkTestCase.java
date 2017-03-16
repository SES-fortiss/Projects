package org.fortiss.smg.remoteframework.test.rabbit;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.osgi.framework.BundleContext;


public class RemoteFrameworkTestCase {

    @Inject
    private BundleContext ctx;

    @Configuration
    public Option[] config() {
        return CoreOptions.options(
                CoreOptions.mavenBundle("org.fortiss.smartmicrogrid", "remoteframework.api"),
                CoreOptions.mavenBundle("org.fortiss.smartmicrogrid", "remoteframework.impl"),
                CoreOptions.junitBundles());
    }

    @Test
    public void getHelloService() {
        //ServiceReference ref = ctx.getServiceReference(RemoteFrameworkInterface.class.getName());
//        RemoteFrameworkInterface svc = (RemoteFrameworkInterface) ctx.getService(ref);

//        Assert.assertEquals("This service implementation should reverse the input",
//                "4321", svc.doSomething("1234"));
    }
}
