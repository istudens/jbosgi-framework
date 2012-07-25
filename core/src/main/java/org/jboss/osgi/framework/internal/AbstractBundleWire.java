package org.jboss.osgi.framework.internal;
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

import org.jboss.osgi.resolver.spi.AbstractWire;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

/**
 * The {@link BundleWire} implementation.
 *
 * @author thomas.diesler@jboss.com
 * @since 23-Feb-2012
 */
class AbstractBundleWire extends AbstractWire implements BundleWire {

    AbstractBundleWire(BundleCapability cap, BundleRequirement req, BundleRevision provider, BundleRevision requirer) {
        super(cap, req, provider, requirer);
    }

    @Override
    public BundleWiring getProviderWiring() {
        return getProvider().getWiring();
    }

    @Override
    public BundleWiring getRequirerWiring() {
        return getRequirer().getWiring();
    }

    public BundleRevision getProvider() {
        return (BundleRevision) super.getProvider();
    }

    public BundleRevision getRequirer() {
        return (BundleRevision) super.getRequirer();
    }

    @Override
    public BundleCapability getCapability() {
        return (BundleCapability) super.getCapability();
    }

    @Override
    public BundleRequirement getRequirement() {
        return (BundleRequirement) super.getRequirement();
    }
}