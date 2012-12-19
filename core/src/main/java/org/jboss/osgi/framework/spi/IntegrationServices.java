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
package org.jboss.osgi.framework.spi;

import org.jboss.modules.Module;
import org.jboss.msc.service.ServiceName;
import org.jboss.osgi.framework.Services;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;

/**
 * The collection of integration service names.
 *
 * @author thomas.diesler@jboss.com
 * @since 04-Apr-2011
 */
public interface IntegrationServices {

	/** The prefix for all OSGi services */
    ServiceName SERVICE_BASE_NAME = Services.JBOSGI_BASE_NAME.append("service");
    /** The prefix for all OSGi bundle services */
    ServiceName BUNDLE_BASE_NAME = Services.JBOSGI_BASE_NAME.append("bundle");
    /** The prefix for all internal services */
    ServiceName INTERNAL_BASE_NAME = Services.JBOSGI_BASE_NAME.append("internal");
    /** The {@link Module} service name */
    ServiceName MODULE_SERVICE = Services.JBOSGI_BASE_NAME.append("module");

    /** The service name for the started {@link Framework} */
    ServiceName FRAMEWORK_ACTIVE_INTERNAL = INTERNAL_BASE_NAME.append("framework", "ACTIVE");
    /** The service name for the created {@link Framework} */
    ServiceName FRAMEWORK_CREATE_INTERNAL = INTERNAL_BASE_NAME.append("framework", "CREATE");
    /** The service name for the initialized {@link Framework} */
    ServiceName FRAMEWORK_INIT_INTERNAL = INTERNAL_BASE_NAME.append("framework", "INIT");
    /** The service name for the system {@link Bundle} */
    ServiceName SYSTEM_BUNDLE_INTERNAL = INTERNAL_BASE_NAME.append("SystemBundle");
    /** The service name for the system {@link BundleContext} */
    ServiceName SYSTEM_CONTEXT_INTERNAL = INTERNAL_BASE_NAME.append("SystemContext");

	/** The {@link BootstrapBundlesInstall} service for auto install bundles */
	ServiceName BOOTSTRAP_BUNDLES = Services.JBOSGI_BASE_NAME.append("BootstrapBundles");
	/** The {@link BootstrapBundlesActivate} service for auto install bundles */
	ServiceName BOOTSTRAP_BUNDLES_ACTIVATE = BootstrapPhase.serviceName(BOOTSTRAP_BUNDLES, BootstrapPhase.ACTIVATE);
	/** The {@link BootstrapBundlesResolve} service for auto install bundles */
	ServiceName BOOTSTRAP_BUNDLES_RESOLVE = BootstrapPhase.serviceName(BOOTSTRAP_BUNDLES, BootstrapPhase.RESOLVE);
	/** The {@link BootstrapBundlesInstall} service for auto install bundles */
	ServiceName BOOTSTRAP_BUNDLES_INSTALL = BootstrapPhase.serviceName(BOOTSTRAP_BUNDLES, BootstrapPhase.INSTALL);
	/** The {@link BootstrapBundlesComplete} service for auto install bundles */
	ServiceName BOOTSTRAP_BUNDLES_COMPLETE = BootstrapPhase.serviceName(BOOTSTRAP_BUNDLES, BootstrapPhase.COMPLETE);
	/** The service name for the {@link BundleLifecycle} */
	ServiceName BUNDLE_LIFECYCLE_PLUGIN = Services.JBOSGI_BASE_NAME.append("BundleLifecycle");
	/** The {@link BundleStorage} plugin service name */
	ServiceName BUNDLE_STORAGE = Services.JBOSGI_BASE_NAME.append("BundleStorage");
    /** The {@link DeploymentProvider} service name */
    ServiceName DEPLOYMENT_PROVIDER_PLUGIN = Services.JBOSGI_BASE_NAME.append("DeploymentProvider");
	/** The {@link CoreServices} service name. */
    ServiceName FRAMEWORK_CORE_SERVICES = Services.JBOSGI_BASE_NAME.append("CoreServices");
    /** The {@link FrameworkEvents} service name */
    ServiceName FRAMEWORK_EVENTS = Services.JBOSGI_BASE_NAME.append("FrameworkEvents");
	/** The service name for the {@link FrameworkModuleProvider} */
	ServiceName FRAMEWORK_MODULE_PROVIDER = Services.JBOSGI_BASE_NAME.append("FrameworkModule");
	/** The {@link LifecycleInterceptorPlugin} service name */
    ServiceName LIFECYCLE_INTERCEPTOR_PLUGIN = Services.JBOSGI_BASE_NAME.append("LifecycleInterceptor");
	/** The {@link LockManager} service name */
	ServiceName LOCK_MANAGER = Services.JBOSGI_BASE_NAME.append("LockManager");
	/** The service name for the {@link FrameworkModuleLoader} */
	ServiceName FRAMEWORK_MODULE_LOADER = Services.JBOSGI_BASE_NAME.append("ModuleLoader");
    /** The {@link ModuleManager} service name */
    ServiceName MODULE_MANGER = Services.JBOSGI_BASE_NAME.append("ModuleManager");
    /** The {@link NativeCode} service name */
    ServiceName NATIVE_CODE_PLUGIN = Services.JBOSGI_BASE_NAME.append("NativeCode");
	/** The {@link BootstrapBundlesInstall} service for persistent bundles */
	ServiceName PERSISTENT_BUNDLES = Services.JBOSGI_BASE_NAME.append("PersistentBundles");
	/** The {@link BootstrapBundlesInstall} service for persistent bundles */
	ServiceName PERSISTENT_BUNDLES_INSTALL = BootstrapPhase.serviceName(PERSISTENT_BUNDLES, BootstrapPhase.INSTALL);
	/** The {@link BootstrapBundlesResolve} service forpersistent bundles */
	ServiceName PERSISTENT_BUNDLES_RESOLVE = BootstrapPhase.serviceName(PERSISTENT_BUNDLES, BootstrapPhase.RESOLVE);
	/** The {@link BootstrapBundlesActivate} service for persistent bundles */
	ServiceName PERSISTENT_BUNDLES_ACTIVATE = BootstrapPhase.serviceName(PERSISTENT_BUNDLES, BootstrapPhase.ACTIVATE);
	/** The {@link BootstrapBundlesComplete} service for persistent bundles */
	ServiceName PERSISTENT_BUNDLES_COMPLETE = BootstrapPhase.serviceName(PERSISTENT_BUNDLES, BootstrapPhase.COMPLETE);
	/** The {@link ServiceManager} service name */
    ServiceName SERVICE_MANAGER = Services.JBOSGI_BASE_NAME.append("ServiceManager");
	/** The service name for the {@link SystemPaths} */
	ServiceName SYSTEM_PATHS = Services.JBOSGI_BASE_NAME.append("SystemPaths");
	/** The service name for the {@link SystemServices} */
	ServiceName SYSTEM_SERVICES_PLUGIN = Services.JBOSGI_BASE_NAME.append("SystemServices");
    /** The {@link URLHandlerSupport} service name */
    ServiceName URL_HANDLER_PLUGIN = Services.JBOSGI_BASE_NAME.append("URLHandler");

    public enum BootstrapPhase {

	    INSTALL, RESOLVE, ACTIVATE, COMPLETE;

	    public BootstrapPhase previous() {
	        final int ord = ordinal() - 1;
	        final BootstrapPhase[] phases = BootstrapPhase.values();
	        return ord < 0 ? null : phases[ord];
	    }

	    public BootstrapPhase next() {
	        final int ord = ordinal() + 1;
	        final BootstrapPhase[] phases = BootstrapPhase.values();
	        return ord == phases.length ? null : phases[ord];
	    }

	    public static ServiceName serviceName(ServiceName baseName, BootstrapPhase phase) {
	        return baseName.append(phase.toString());
	    }
	}

}