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

import org.jboss.msc.value.InjectedValue;
import org.jboss.osgi.framework.spi.BundleStartLevelSupport;
import org.jboss.osgi.framework.spi.DeploymentProvider;
import org.jboss.osgi.framework.spi.FrameworkEvents;
import org.jboss.osgi.framework.spi.FrameworkModuleLoader;
import org.jboss.osgi.framework.spi.FrameworkModuleProvider;
import org.jboss.osgi.framework.spi.FrameworkStartLevelSupport;
import org.jboss.osgi.framework.spi.LockManager;
import org.jboss.osgi.framework.spi.ModuleManager;
import org.jboss.osgi.framework.spi.NativeCode;
import org.jboss.osgi.framework.spi.ServiceManager;
import org.jboss.osgi.framework.spi.StartLevelManager;
import org.jboss.osgi.framework.spi.StorageManager;
import org.jboss.osgi.framework.spi.SystemPaths;
import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XResolver;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.wiring.FrameworkWiring;

/**
 * Represents the state of the {@link Framework}.
 *
 * It is used by the various {@link AbstractFrameworkService}s as well as the {@link FrameworkProxy}.
 * The state is never given to the client.
 *
 * @author thomas.diesler@jboss.com
 * @since 04-Apr-2011
 */
final class FrameworkState {

    private final BundleManagerPlugin bundleManager;

    final InjectedValue<BundleStartLevelSupport> injectedBundleStartLevel = new InjectedValue<BundleStartLevelSupport>();
    final InjectedValue<StorageManager> injectedStorageManager = new InjectedValue<StorageManager>();
    final InjectedValue<DeploymentProvider> injectedDeploymentProvider = new InjectedValue<DeploymentProvider>();
    final InjectedValue<CoreServices> injectedCoreServices = new InjectedValue<CoreServices>();
    final InjectedValue<FrameworkEnvironment> injectedFrameworkEnvironment = new InjectedValue<FrameworkEnvironment>();
    final InjectedValue<FrameworkEvents> injectedFrameworkEvents = new InjectedValue<FrameworkEvents>();
    final InjectedValue<FrameworkModuleLoader> injectedModuleLoader = new InjectedValue<FrameworkModuleLoader>();
    final InjectedValue<FrameworkModuleProvider> injectedModuleProvider = new InjectedValue<FrameworkModuleProvider>();
    final InjectedValue<FrameworkStartLevelSupport> injectedFrameworkStartLevel = new InjectedValue<FrameworkStartLevelSupport>();
    final InjectedValue<FrameworkWiring> injectedFrameworkWiring = new InjectedValue<FrameworkWiring>();
    final InjectedValue<LockManager> injectedLockManager = new InjectedValue<LockManager>();
    final InjectedValue<ModuleManager> injectedModuleManager = new InjectedValue<ModuleManager>();
    final InjectedValue<NativeCode> injectedNativeCode = new InjectedValue<NativeCode>();
    final InjectedValue<ServiceManager> injectedServiceManager = new InjectedValue<ServiceManager>();
    final InjectedValue<StartLevelManager> injectedStartLevel = new InjectedValue<StartLevelManager>();
    final InjectedValue<SystemPaths> injectedSystemPaths = new InjectedValue<SystemPaths>();
    final InjectedValue<SystemBundleState> injectedSystemBundle = new InjectedValue<SystemBundleState>();
    final InjectedValue<XEnvironment> injectedEnvironment = new InjectedValue<XEnvironment>();
    final InjectedValue<XResolver> injectedResolver = new InjectedValue<XResolver>();

    FrameworkState(BundleManagerPlugin bundleManager) {
        this.bundleManager = bundleManager;
    }

    BundleManagerPlugin getBundleManager() {
        return bundleManager;
    }

    BundleStartLevelSupport getBundleStartLevel() {
        return injectedBundleStartLevel.getValue();
    }

    StorageManager getStorageManager() {
        return injectedStorageManager.getValue();
    }

    DeploymentProvider getDeploymentProvider() {
        return injectedDeploymentProvider.getValue();
    }

    CoreServices getCoreServices() {
        return injectedCoreServices.getValue();
    }

    FrameworkEnvironment getFrameworkEnvironment() {
        return injectedFrameworkEnvironment.getValue();
    }

    FrameworkEvents getFrameworkEvents() {
        return injectedFrameworkEvents.getValue();
    }

    FrameworkModuleLoader getFrameworkModuleLoader() {
        return injectedModuleLoader.getValue();
    }

    FrameworkModuleProvider getFrameworkModuleProvider() {
        return injectedModuleProvider.getValue();
    }

    FrameworkStartLevelSupport getFrameworkStartLevel() {
        return injectedFrameworkStartLevel.getValue();
    }

    XResolver getFrameworkResolver() {
        return injectedResolver.getValue();
    }

    FrameworkWiring getFrameworkWiring() {
        return injectedFrameworkWiring.getValue();
    }

    LockManager getLockManager() {
        return injectedLockManager.getValue();
    }

    ModuleManager getModuleManager() {
        return injectedModuleManager.getValue();
    }

    NativeCode getNativeCode() {
        return injectedNativeCode.getValue();
    }

    ServiceManager getServiceManagerPlugin() {
        return injectedServiceManager.getValue();
    }

    StartLevelManager getStartLevelManager() {
        return injectedStartLevel.getValue();
    }

    SystemBundleState getSystemBundle() {
        return injectedSystemBundle.getValue();
    }

    SystemPaths getSystemPaths() {
        return injectedSystemPaths.getValue();
    }

    XEnvironment getEnvironment() {
        return injectedEnvironment.getValue();
    }
}
