/*
 * #%L
 * JBossOSGi Framework
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.jboss.test.osgi.framework.bundle.tracker;

import static java.util.jar.JarFile.MANIFEST_NAME;
import static org.jboss.osgi.spi.ConstantsHelper.bundleEvent;
import static org.jboss.osgi.spi.ConstantsHelper.bundleState;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.net.URL;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.SynchronousBundleListener;

/**
 * [JBOSGI-450] Bundle containing a BundleTracker causes subsequent bundle install/starts to fail
 *
 * https://jira.jboss.org/jira/browse/JBOSGI-450
 *
 * @author thomas.diesler@jboss.com
 * @since 19-May-2011
 */
public class BundleTrackerTestCase extends OSGiFrameworkTest {

    private final List<BundleEvent> bundleEvents = new CopyOnWriteArrayList<BundleEvent>();
    private final static Map<String, String> eventMap = new HashMap<String, String>();
    static {
        eventMap.put(bundleEvent(BundleEvent.INSTALLED), bundleState(Bundle.INSTALLED));
        eventMap.put(bundleEvent(BundleEvent.RESOLVED), bundleState(Bundle.RESOLVED));
        eventMap.put(bundleEvent(BundleEvent.STARTED), bundleState(Bundle.ACTIVE));
        eventMap.put(bundleEvent(BundleEvent.STOPPED), bundleState(Bundle.RESOLVED));
        eventMap.put(bundleEvent(BundleEvent.UNRESOLVED), bundleState(Bundle.RESOLVED));
        eventMap.put(bundleEvent(BundleEvent.UNINSTALLED), bundleState(Bundle.UNINSTALLED));
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        bundleEvents.clear();
    }

    @Test
    public void testBundleListener() throws Exception {

        BundleContext context = getFramework().getBundleContext();
        BundleListener listener = new BundleListener() {
            @Override
            public void bundleChanged(BundleEvent event) {
                synchronized (bundleEvents) {
                    bundleEvents.add(event);
                    bundleEvents.notifyAll();
                }
            }
        };
        context.addBundleListener(listener);
        try {
            JavaArchive archiveA = getBundleArchive();
            Bundle bundleA = installBundle(archiveA);
            assertBundleEvent(BundleEvent.INSTALLED);

            URL url = bundleA.getResource(MANIFEST_NAME);
            assertBundleEvent(BundleEvent.RESOLVED);
            assertNotNull("URL not null", url);

            bundleA.start();
            assertBundleEvent(BundleEvent.STARTED);

            bundleA.stop();
            assertBundleEvent(BundleEvent.STOPPED);

            bundleA.uninstall();
            assertBundleEvent(BundleEvent.UNRESOLVED);
            assertBundleEvent(BundleEvent.UNINSTALLED);
        } finally {
            context.removeBundleListener(listener);
        }
    }

    @Test
    public void testBundleListenerError() throws Exception {

        BundleContext context = getFramework().getBundleContext();
        BundleListener listener = new BundleListener() {
            @Override
            public void bundleChanged(BundleEvent event) {
                synchronized (bundleEvents) {
                    bundleEvents.add(event);
                    bundleEvents.notifyAll();
                }
                throw new RuntimeException("Error for: " + event);
            }
        };
        context.addBundleListener(listener);
        try {
            JavaArchive archiveA = getBundleArchive();
            Bundle bundleA = installBundle(archiveA);
            assertBundleEvent(BundleEvent.INSTALLED);

            URL url = bundleA.getResource(MANIFEST_NAME);
            assertBundleEvent(BundleEvent.RESOLVED);
            assertNotNull("URL not null", url);

            bundleA.start();
            assertBundleEvent(BundleEvent.STARTED);

            bundleA.stop();
            assertBundleEvent(BundleEvent.STOPPED);

            bundleA.uninstall();
            assertBundleEvent(BundleEvent.UNRESOLVED);
            assertBundleEvent(BundleEvent.UNINSTALLED);
        } finally {
            context.removeBundleListener(listener);
        }
    }

    @Test
    public void testBundleListenerAccess() throws Exception {

        BundleContext context = getFramework().getBundleContext();
        BundleListener listener = new SynchronousBundleListener() {
            @Override
            public void bundleChanged(BundleEvent event) {
                synchronized (bundleEvents) {
                    bundleEvents.add(event);
                    bundleEvents.notifyAll();
                }
                Bundle bundle = event.getBundle();
                String wasState = bundleState(bundle.getState());
                String expState = eventMap.get(bundleEvent(event.getType()));
                if (expState.equals(wasState) == false) {
                    System.err.println("Expected " + expState + " but was " + wasState);
                    bundleEvents.remove(event);
                }
            }
        };
        context.addBundleListener(listener);
        try {
            JavaArchive archiveA = getBundleArchive();
            Bundle bundleA = installBundle(archiveA);
            assertBundleEvent(BundleEvent.INSTALLED);

            URL url = bundleA.getResource(MANIFEST_NAME);
            assertBundleEvent(BundleEvent.RESOLVED);
            assertNotNull("URL not null", url);

            bundleA.start();
            assertBundleEvent(BundleEvent.STARTED);

            bundleA.stop();
            assertBundleEvent(BundleEvent.STOPPED);

            bundleA.uninstall();
            assertBundleEvent(BundleEvent.UNRESOLVED);
            assertBundleEvent(BundleEvent.UNINSTALLED);
        } finally {
            context.removeBundleListener(listener);
        }
    }

    @Test
    public void testSynchronousBundleListener() throws Exception {

        BundleContext context = getFramework().getBundleContext();
        BundleListener listener = new SynchronousBundleListener() {
            @Override
            public void bundleChanged(BundleEvent event) {
                synchronized (bundleEvents) {
                    bundleEvents.add(event);
                    bundleEvents.notifyAll();
                }
            }
        };
        context.addBundleListener(listener);
        try {
            JavaArchive archiveA = getBundleArchive();
            Bundle bundleA = installBundle(archiveA);
            assertBundleEvent(BundleEvent.INSTALLED);

            URL url = bundleA.getResource(MANIFEST_NAME);
            assertBundleEvent(BundleEvent.RESOLVED);
            assertNotNull("URL not null", url);

            bundleA.start();
            assertBundleEvent(BundleEvent.STARTED);

            bundleA.stop();
            assertBundleEvent(BundleEvent.STOPPED);

            bundleA.uninstall();
            assertBundleEvent(BundleEvent.UNRESOLVED);
            assertBundleEvent(BundleEvent.UNINSTALLED);
        } finally {
            context.removeBundleListener(listener);
        }
    }

    @Test
    public void testSynchronousBundleListenerError() throws Exception {

        BundleContext context = getFramework().getBundleContext();
        BundleListener listener = new SynchronousBundleListener() {
            @Override
            public void bundleChanged(BundleEvent event) {
                synchronized (bundleEvents) {
                    bundleEvents.add(event);
                    bundleEvents.notifyAll();
                }
                throw new RuntimeException("Error for: " + event);
            }
        };
        context.addBundleListener(listener);
        try {
            JavaArchive archiveA = getBundleArchive();
            Bundle bundleA = installBundle(archiveA);
            assertBundleEvent(BundleEvent.INSTALLED);

            URL url = bundleA.getResource(MANIFEST_NAME);
            assertBundleEvent(BundleEvent.RESOLVED);
            assertNotNull("URL not null", url);

            bundleA.start();
            assertBundleEvent(BundleEvent.STARTED);

            bundleA.stop();
            assertBundleEvent(BundleEvent.STOPPED);

            bundleA.uninstall();
            assertBundleEvent(BundleEvent.UNRESOLVED);
            assertBundleEvent(BundleEvent.UNINSTALLED);
        } finally {
            context.removeBundleListener(listener);
        }
    }

    @Test
    public void testSynchronousBundleListenerAccess() throws Exception {

        BundleContext context = getFramework().getBundleContext();
        BundleListener listener = new SynchronousBundleListener() {
            @Override
            public void bundleChanged(BundleEvent event) {
                synchronized (bundleEvents) {
                    bundleEvents.add(event);
                    bundleEvents.notifyAll();
                }
                String expState = eventMap.get(bundleEvent(event.getType()));
                String wasState;
                try {
                    Bundle bundle = event.getBundle();
                    wasState = bundleState(bundle.getState());
                } catch (Throwable th) {
                    th.printStackTrace();
                    wasState = null;
                }
                if (expState.equals(wasState) == false) {
                    System.err.println("Expected " + expState + " but was " + wasState);
                    bundleEvents.remove(event);
                }
            }
        };
        context.addBundleListener(listener);
        try {
            JavaArchive archiveA = getBundleArchive();
            Bundle bundleA = installBundle(archiveA);
            assertBundleEvent(BundleEvent.INSTALLED);

            URL url = bundleA.getResource(MANIFEST_NAME);
            assertBundleEvent(BundleEvent.RESOLVED);
            assertNotNull("URL not null", url);

            bundleA.start();
            assertBundleEvent(BundleEvent.STARTED);

            bundleA.stop();
            assertBundleEvent(BundleEvent.STOPPED);

            bundleA.uninstall();
            assertBundleEvent(BundleEvent.UNRESOLVED);
            assertBundleEvent(BundleEvent.UNINSTALLED);
        } finally {
            context.removeBundleListener(listener);
        }
    }

    private void assertBundleEvent(int type) throws Exception {
        synchronized (bundleEvents) {
            BundleEvent event = (BundleEvent) waitForEvent(type);
            assertNotNull("Event not null", event);
            for (int i = 0; i < bundleEvents.size(); i++) {
                BundleEvent aux = bundleEvents.get(i);
                if (type == aux.getType()) {
                    bundleEvents.remove(aux);
                    event = aux;
                    break;
                }
            }
            if (event == null)
                fail("Cannot find event " + bundleEvent(type));
        }
    }

    private EventObject waitForEvent(int type) throws InterruptedException {
        synchronized (bundleEvents) {
            // Timeout for event delivery: 3 sec
            int timeout = 30;
            EventObject eventFound = null;
            while (eventFound == null && 0 < timeout) {
                for (BundleEvent event : bundleEvents) {
                    if (type == event.getType()) {
                        eventFound = event;
                        break;
                    }
                }
                bundleEvents.wait(100);
                timeout--;
            }
            return eventFound;
        }
    }

    private JavaArchive getBundleArchive() {
        final JavaArchive archiveA = ShrinkWrap.create(JavaArchive.class, "jbosgi450-bundleA");
        archiveA.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archiveA.getName());
                return builder.openStream();
            }
        });
        return archiveA;
    }
}
