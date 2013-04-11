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
package org.jboss.osgi.framework.internal;

import static org.jboss.osgi.framework.FrameworkMessages.MESSAGES;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.jboss.msc.service.ServiceController;
import org.jboss.osgi.deployment.deployer.Deployment;
import org.jboss.osgi.framework.spi.BundleLifecycle;
import org.jboss.osgi.framework.spi.BundleManager;
import org.jboss.osgi.framework.spi.FutureServiceValue;
import org.jboss.osgi.resolver.XBundle;
import org.jboss.osgi.resolver.XBundleRevision;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.resolver.ResolutionException;

/**
 * A handler for the bundle lifecycle.
 *
 * @author thomas.diesler@jboss.com
 * @since 29-Mar-2011
 */
public class BundleLifecycleImpl implements BundleLifecycle {

        private final BundleManager bundleManager;
        private final BundleRefreshPolicy refreshPolicy;

        public BundleLifecycleImpl(BundleManager bundleManager) {
            this.bundleManager = bundleManager;

            //refreshPolicy = new RecreateCurrentRevisionPolicy(bundleManager);
            refreshPolicy = new KeepCurrentRevisionPolicy();
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public ServiceController<? extends XBundleRevision> createBundleRevision(BundleContext context, Deployment dep) throws BundleException {
            ServiceController<? extends XBundleRevision> controller = bundleManager.createBundleRevision(context, dep, null, null);
            FutureServiceValue<XBundleRevision> future = new FutureServiceValue(controller);
            try {
                future.get(30, TimeUnit.SECONDS);
                return controller;
            } catch (Exception ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof BundleException) {
                    throw (BundleException) cause;
                }
                throw MESSAGES.cannotInstallBundleRevisionFromDeployment(ex, dep);
            }
        }

        @Override
        public void resolve(XBundle bundle) throws ResolutionException {
            bundleManager.resolveBundle(bundle);
        }

        @Override
        public void start(XBundle bundle, int options) throws BundleException {
            bundleManager.startBundle(bundle, options);
        }

        @Override
        public void stop(XBundle bundle, int options) throws BundleException {
            bundleManager.stopBundle(bundle, options);
        }

        @Override
        public void update(XBundle bundle, InputStream input) throws BundleException {
            bundleManager.updateBundle(bundle, input);
        }

        @Override
        public void uninstall(XBundle bundle, int options) throws BundleException {
            bundleManager.uninstallBundle(bundle, options);
        }

        @Override
        public void removeRevision(XBundleRevision brev, int options) {
            bundleManager.removeRevision(brev, options);
        }

        @Override
        public BundleRefreshPolicy getBundleRefreshPolicy() {
            return refreshPolicy;
        }
    }