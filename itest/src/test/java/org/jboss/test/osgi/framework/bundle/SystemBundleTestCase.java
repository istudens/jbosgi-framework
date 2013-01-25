package org.jboss.test.osgi.framework.bundle;
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

//

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;

import org.jboss.osgi.framework.Constants;
import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * SystemBundleTest.
 *
 * @author thomas.diesler@jboss.com
 */
public class SystemBundleTestCase extends OSGiFrameworkTest {

    @Test
    public void testBundleId() throws Exception {
        assertEquals(0, getFramework().getBundleId());
    }

    @Test
    public void testSymbolicName() throws Exception {
        assertEquals(Constants.JBOSGI_FRAMEWORK_SYMBOLIC_NAME, getFramework().getSymbolicName());
    }

    @Test
    public void testLocation() throws Exception {
        assertEquals(Constants.JBOSGI_FRAMEWORK_LOCATION, getFramework().getLocation());
    }

    @Test
    public void testState() throws Exception {
        assertEquals(Bundle.ACTIVE, getFramework().getState());
    }

    @Test
    public void testUninstall() throws Exception {
        try {
            getFramework().uninstall();
            fail("Should not be here!");
        } catch (BundleException t) {
            // expected
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testGetHeaders() throws Exception {
        Dictionary dictionary = getFramework().getHeaders();
        assertEquals(Constants.SYSTEM_BUNDLE_SYMBOLICNAME, dictionary.get(Constants.BUNDLE_SYMBOLICNAME));
    }

    @Test
    public void testGetEntry() throws BundleException {
        URL was = getFramework().getEntry("META-INF/services");
        assertNull("Entry null", was);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testGetEntryPaths() throws BundleException {
        Enumeration was = getFramework().getEntryPaths("META-INF/services");
        assertNull("Entries null", was);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testFindEntries() throws BundleException {
        Enumeration was = getFramework().findEntries("META-INF/services", "*.xml", true);
        assertNull("Entries null", was);
    }

    @Test
    public void testLoadClass() throws ClassNotFoundException, BundleException {
        Class<?> was = getFramework().loadClass(Bundle.class.getName());
        assertNotNull("Class not null", was);
    }

    @Test
    public void testGetResource() throws BundleException {
        URL was = getFramework().getResource("META-INF/services/jboss-osgi-bootstrap-system.xml");
        assertNull("Resource not null", was);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testGetResources() throws BundleException, IOException {
        Enumeration was = getFramework().getResources("META-INF/services/jboss-osgi-bootstrap-system.xml");
        assertNotNull("Resources not null", was);
    }

    @Test
    public void testBundleZero() throws BundleException {

        Bundle systemBundle = getSystemContext().getBundle(0);
        systemBundle.start();
        assertBundleState(Bundle.ACTIVE, systemBundle.getState());

        try {
            systemBundle.uninstall();
            fail("bundle(0).uninstall returned without Exception");
        } catch (BundleException e) {
            // expected
        }
    }
}
