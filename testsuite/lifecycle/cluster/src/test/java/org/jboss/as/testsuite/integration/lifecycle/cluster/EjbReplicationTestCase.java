/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.testsuite.integration.lifecycle.cluster;

import java.io.File;

import javax.inject.Inject;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.managed.ManagedContainerConfiguration;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
@RunWith(Arquillian.class)
public class EjbReplicationTestCase {

    @Deployment(name = "dep.active-1")
    @TargetsContainer("container.active-1")
    public static WebArchive createTestDeployment() {
        return createTestWar();
    }

    @Deployment(name = "dep.active-2")
    @TargetsContainer("container.active-2")
    public static WebArchive createTestDeployment2() {
        return createTestWar();
    }

    @Deployment(name = "dep.active-3")
    @TargetsContainer("container.active-3")
    public static WebArchive createTestDeployment3() {
        return createTestWar();
    }

    @Inject
    private SampleStatefulBean stateful;

    @Test
    @OperateOnDeployment("dep.active-1")
    public void call1Active1() throws Exception {
        stateful.setCounter(1);
        Assert.assertEquals(1, stateful.getCounter());
    }

    @Test
    @OperateOnDeployment("dep.active-2")
    public void call2Active2() throws Exception {
        Assert.assertEquals(1, stateful.getCounter());
        stateful.setCounter(2);
        Assert.assertEquals(2, stateful.getCounter());
    }

    @Test
    @OperateOnDeployment("dep.active-3")
    public void call3Active3() throws Exception {
        Assert.assertEquals(2, stateful.getCounter());
        stateful.setCounter(3);
    }

    @Ignore("AS7-1011 Unable to stop a started container manually")
    @Test
    @RunAsClient
    @OperateOnDeployment("dep.active-2")
    public void call4StopServer2(@ArquillianResource DeployableContainer<ManagedContainerConfiguration> jbossas2) throws Exception {
        jbossas2.stop();
    }

    @Test
    @OperateOnDeployment("dep.active-1")
    public void call5Active1AfterStop2() throws Exception {
        Assert.assertEquals(3, stateful.getCounter());
    }

    @Test
    @OperateOnDeployment("dep.active-3")
    public void call6Active3AfterStop2() throws Exception {
        Assert.assertEquals(3, stateful.getCounter());
        stateful.setCounter(4);
    }

    @Ignore("AS7-1011 Unable to kill a started container manually")
    @Test
    @RunAsClient
    @OperateOnDeployment("dep.active-3")
    public void call7KillServer3(@ArquillianResource DeployableContainer<ManagedContainerConfiguration> jbossas3) throws Exception {

        // kill by a nasty way
        // jbossas3.kill();
    }

    @Test
    @OperateOnDeployment("dep.active-1")
    public void call8Active1AfterKill3() throws Exception {
        Assert.assertEquals(4, stateful.getCounter());
    }

    // using JavaArchive doesn't work, because of a bug in Arquillian, it only deploys wars properly
    private static WebArchive createTestWar() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "clustering-ejb-test.war").addClasses(SampleStateful.class,
                SampleStatefulBean.class, SampleStateless.class, SampleStatelessBean.class);

        war.as(ZipExporter.class).exportTo(new File("target/clustering-ejb-test.war"), true);

        return war;
    }
}
