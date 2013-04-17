package org.jboss.test.osgi.framework.bundle.activation;
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

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.framework.bundle.tbchain1.MultiChain1;
import org.jboss.test.osgi.framework.bundle.tbchain1.MultiChain2;
import org.jboss.test.osgi.framework.bundle.tbchain2.AMultiChain1;
import org.jboss.test.osgi.framework.bundle.tbchain2.AMultiChain2;
import org.jboss.test.osgi.framework.bundle.tbchain2.AMultiChain3;
import org.jboss.test.osgi.framework.bundle.tbchain3.BMultiChain1;
import org.jboss.test.osgi.framework.bundle.tbchain3.BMultiChain2;
import org.jboss.test.osgi.framework.bundle.tbchain3.BMultiChain3;
import org.jboss.test.osgi.framework.bundle.tbchain4.CMultipleChain1;
import org.jboss.test.osgi.framework.bundle.tbchain4.CMultipleChain2;
import org.jboss.test.osgi.framework.bundle.tbchain4.CMultipleChain3;
import org.jboss.test.osgi.framework.bundle.tbchain5.DMultipleChain1;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.SynchronousBundleListener;

/**
 * More advanced chain test that contains multiple class hierachies
 *
 * @author thomas.diesler@jboss.com
 */
public class MultiChainActivationTestCase extends OSGiFrameworkTest {

    private final List<BundleEvent> events = new ArrayList<BundleEvent>();

    @Test
    @Ignore("fails with JDK1.7")
    public void testComplexGraph() throws Exception {

        Bundle tbchain1 = installBundle(getArchive1());
        Bundle tbchain2 = installBundle(getArchive2());
        Bundle tbchain3 = installBundle(getArchive3());
        Bundle tbchain4 = installBundle(getArchive4());
        Bundle tbchain5 = installBundle(getArchive5());

        tbchain1.start(Bundle.START_ACTIVATION_POLICY);
        tbchain2.start(Bundle.START_ACTIVATION_POLICY);
        tbchain3.start(Bundle.START_ACTIVATION_POLICY);
        tbchain4.start(Bundle.START_ACTIVATION_POLICY);
        tbchain5.start(Bundle.START_ACTIVATION_POLICY);

        ActivationListener listener = new ActivationListener(BundleEvent.STARTED);
        getSystemContext().addBundleListener(listener);

        try {
            tbchain1.loadClass(MultiChain1.class.getName()).newInstance();
            assertEquals(5, events.size());
            assertEquals(tbchain5, events.get(0).getBundle());
            assertEquals(tbchain3, events.get(1).getBundle());
            assertEquals(tbchain4, events.get(2).getBundle());
            assertEquals(tbchain2, events.get(3).getBundle());
            assertEquals(tbchain1, events.get(4).getBundle());
        } finally {
            getSystemContext().removeBundleListener(listener);
            tbchain5.uninstall();
            tbchain4.uninstall();
            tbchain3.uninstall();
            tbchain2.uninstall();
            tbchain1.uninstall();
        }
    }

    @Test
    public void testSimpleGraph() throws Exception {

        Bundle tbchain1 = installBundle(getArchive1());
        Bundle tbchain2 = installBundle(getArchive2());
        Bundle tbchain3 = installBundle(getArchive3());
        Bundle tbchain4 = installBundle(getArchive4());
        Bundle tbchain5 = installBundle(getArchive5());

        tbchain1.start(Bundle.START_ACTIVATION_POLICY);
        tbchain2.start(Bundle.START_ACTIVATION_POLICY);
        tbchain3.start(Bundle.START_ACTIVATION_POLICY);
        tbchain4.start(Bundle.START_ACTIVATION_POLICY);
        tbchain5.start(Bundle.START_ACTIVATION_POLICY);

        ActivationListener listener = new ActivationListener(BundleEvent.STARTED);
        getSystemContext().addBundleListener(listener);

        try {
            tbchain1.loadClass(MultiChain2.class.getName()).newInstance();
            assertEquals(5, events.size());
            assertEquals(tbchain5, events.get(0).getBundle());
            assertEquals(tbchain4, events.get(1).getBundle());
            assertEquals(tbchain3, events.get(2).getBundle());
            assertEquals(tbchain2, events.get(3).getBundle());
            assertEquals(tbchain1, events.get(4).getBundle());
        } finally {
            getSystemContext().removeBundleListener(listener);
            tbchain5.uninstall();
            tbchain4.uninstall();
            tbchain3.uninstall();
            tbchain2.uninstall();
            tbchain1.uninstall();
        }
    }

    class ActivationListener implements SynchronousBundleListener {
        private final int eventTypes;

        public ActivationListener(int eventTypes) {
            this.eventTypes = eventTypes;
        }

        @Override
        public void bundleChanged(BundleEvent event) {
            if ((eventTypes & event.getType()) != 0)
                events.add(event);
        }
    }

    private static JavaArchive getArchive1() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "tbchain1");
        archive.addClasses(MultiChain1.class, MultiChain2.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addBundleActivationPolicy(Constants.ACTIVATION_LAZY);
                builder.addImportPackages(AMultiChain1.class, BMultiChain2.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    private static JavaArchive getArchive2() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "tbchain2");
        archive.addClasses(AMultiChain1.class, AMultiChain2.class, AMultiChain3.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addBundleActivationPolicy(Constants.ACTIVATION_LAZY);
                builder.addExportPackages(AMultiChain1.class);
                builder.addImportPackages(CMultipleChain1.class, BMultiChain1.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    private static JavaArchive getArchive3() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "tbchain3");
        archive.addClasses(BMultiChain1.class, BMultiChain2.class, BMultiChain3.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addBundleActivationPolicy(Constants.ACTIVATION_LAZY);
                builder.addExportPackages(BMultiChain1.class);
                builder.addImportPackages(CMultipleChain1.class, DMultipleChain1.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    private static JavaArchive getArchive4() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "tbchain4");
        archive.addClasses(CMultipleChain1.class, CMultipleChain2.class, CMultipleChain3.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addBundleActivationPolicy(Constants.ACTIVATION_LAZY);
                builder.addExportPackages(CMultipleChain1.class);
                builder.addImportPackages(BMultiChain1.class, DMultipleChain1.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    private static JavaArchive getArchive5() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "tbchain5");
        archive.addClasses(DMultipleChain1.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addExportPackages(DMultipleChain1.class);
                builder.addBundleActivationPolicy(Constants.ACTIVATION_LAZY);
                return builder.openStream();
            }
        });
        return archive;
    }
}
