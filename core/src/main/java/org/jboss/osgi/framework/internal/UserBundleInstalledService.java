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

import static org.jboss.osgi.framework.FrameworkLogger.LOGGER;
import static org.jboss.osgi.framework.FrameworkMessages.MESSAGES;

import java.io.IOException;

import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceListener;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.osgi.deployment.deployer.Deployment;
import org.jboss.osgi.framework.spi.BundleStorage;
import org.jboss.osgi.framework.spi.FrameworkEvents;
import org.jboss.osgi.framework.spi.IntegrationServices;
import org.jboss.osgi.framework.spi.NativeCode;
import org.jboss.osgi.framework.spi.StorageState;
import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.resolver.XBundle;
import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.vfs.VirtualFile;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;

/**
 * Represents the INSTALLED state of a user bundle.
 *
 * @author thomas.diesler@jboss.com
 * @since 04-Apr-2011
 */
abstract class UserBundleInstalledService<B extends UserBundleState<R>, R extends UserBundleRevision> extends AbstractBundleService<B> {

    private final Deployment initialDeployment;
    private final BundleContext sourceContext;
    private B bundleState;

    UserBundleInstalledService(FrameworkState frameworkState, BundleContext sourceContext, Deployment deployment) {
        super(frameworkState);
        this.initialDeployment = deployment;
        this.sourceContext = sourceContext;
    }

    ServiceName install(ServiceTarget serviceTarget, ServiceListener<XBundle> listener) {
        ServiceName serviceName = getBundleManager().getServiceName(initialDeployment, Bundle.INSTALLED);
        LOGGER.tracef("Installing %s %s", getClass().getSimpleName(), serviceName);
        ServiceBuilder<B> builder = serviceTarget.addService(serviceName, this);
        addServiceDependencies(builder);
        if (listener != null) {
            builder.addListener(listener);
        }
        return builder.install().getName();
    }

    protected void addServiceDependencies(ServiceBuilder<B> builder) {
        builder.addDependency(IntegrationServices.FRAMEWORK_CORE_SERVICES);
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        StorageState storageState = null;
        try {
            Deployment dep = initialDeployment;
            Long bundleId = dep.getAttachment(Long.class);
            storageState = createStorageState(dep, bundleId);
            OSGiMetaData metadata = dep.getAttachment(OSGiMetaData.class);
            R brev = createBundleRevision(dep, metadata, storageState);
            brev.addAttachment(Long.class, bundleId);
            ServiceName serviceName = startContext.getController().getName().getParent();
            bundleState = createBundleState(brev, serviceName, startContext.getChildTarget());
            dep.addAttachment(Bundle.class, bundleState);
            bundleState.initLazyActivation();
            validateBundle(bundleState, metadata);
            processNativeCode(bundleState, dep);
            installBundle(bundleState);
            // For the event type INSTALLED, this is the bundle whose context was used to install the bundle
            XBundle origin = (XBundle) sourceContext.getBundle();
            FrameworkEvents events = getFrameworkState().getFrameworkEvents();
            events.fireBundleEvent(origin, bundleState, BundleEvent.INSTALLED);
        } catch (BundleException ex) {
            if (storageState != null) {
                BundleStorage storagePlugin = getFrameworkState().getBundleStorage();
                storagePlugin.deleteStorageState(storageState);
            }
            throw new StartException(ex);
        }
    }

    @Override
    public void stop(StopContext context) {
        if (getBundleState().getState() != Bundle.UNINSTALLED) {
            try {
                getBundleManager().uninstallBundle(getBundleState(), Bundle.STOP_TRANSIENT);
            } catch (BundleException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    abstract R createBundleRevision(Deployment deployment, OSGiMetaData metadata, StorageState storageState) throws BundleException;

    abstract B createBundleState(R revision, ServiceName serviceName, ServiceTarget serviceTarget) throws BundleException;

    StorageState createStorageState(Deployment dep, Long bundleId) throws BundleException {
        // The storage state exists when we re-create the bundle from persistent storage
        StorageState storageState = dep.getAttachment(StorageState.class);
        if (storageState == null) {
            String location = dep.getLocation();
            VirtualFile rootFile = dep.getRoot();
            try {
                BundleStorage storagePlugin = getFrameworkState().getBundleStorage();
                Integer startlevel = dep.getStartLevel();
                if (startlevel == null) {
                    startlevel = getFrameworkState().getStartLevelSupport().getInitialBundleStartLevel();
                }
                storageState = storagePlugin.createStorageState(bundleId, location, startlevel, rootFile);
                dep.addAttachment(StorageState.class, storageState);
            } catch (IOException ex) {
                throw MESSAGES.cannotSetupStorage(ex, rootFile);
            }
        }
        return storageState;
    }

    @Override
    B getBundleState() {
        return bundleState;
    }

    private void installBundle(UserBundleState<?> userBundle) throws BundleException {

        XEnvironment env = getFrameworkState().getEnvironment();
        env.installResources(userBundle.getBundleRevision());

        userBundle.changeState(Bundle.INSTALLED, 0);
        LOGGER.infoBundleInstalled(userBundle);
    }

    private void validateBundle(UserBundleState<?> userBundle, OSGiMetaData metadata) throws BundleException {
        if (metadata.getBundleManifestVersion() > 1) {
            new BundleValidatorR4().validateBundle(userBundle, metadata);
        } else {
            new BundleValidatorR3().validateBundle(userBundle, metadata);
        }
    }

    // Process the Bundle-NativeCode header if there is one
    private void processNativeCode(UserBundleState<?> userBundle, Deployment dep) {
        OSGiMetaData metadata = userBundle.getOSGiMetaData();
        if (metadata.getBundleNativeCode() != null) {
            FrameworkState frameworkState = userBundle.getFrameworkState();
            NativeCode nativeCodePlugin = frameworkState.getNativeCode();
            nativeCodePlugin.deployNativeCode(dep);
        }
    }
}
