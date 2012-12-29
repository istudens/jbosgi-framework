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

import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceListener;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jboss.osgi.framework.Services;
import org.jboss.osgi.framework.spi.AbstractIntegrationService;
import org.jboss.osgi.framework.spi.DeploymentProvider;
import org.jboss.osgi.framework.spi.FrameworkEvents;
import org.jboss.osgi.framework.spi.FrameworkModuleLoader;
import org.jboss.osgi.framework.spi.IntegrationServices;
import org.jboss.osgi.framework.spi.NativeCode;
import org.jboss.osgi.framework.spi.ServiceManager;
import org.jboss.osgi.resolver.XResolver;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;

/**
 * A service that represents the CREATED state of the {@link Framework}.
 *
 * When this services has started, the system bundle context is availbale as
 * well as the basic infrastructure to register OSGi services.
 *
 * @author thomas.diesler@jboss.com
 * @since 04-Apr-2011
 */
public final class FrameworkCreate extends AbstractFrameworkService {

    private final FrameworkState frameworkState;
    private final Mode initialMode;

    FrameworkCreate(FrameworkState frameworkState, Mode initialMode) {
        super(IntegrationServices.FRAMEWORK_CREATE_INTERNAL);
        this.frameworkState = frameworkState;
        this.initialMode = initialMode;
    }

    @Override
    public ServiceController<FrameworkState> install(ServiceTarget serviceTarget, ServiceListener<Object> listener) {
        ServiceController<FrameworkState> controller = super.install(serviceTarget, listener);
        new FrameworkCreated().install(serviceTarget, listener);
        return controller;
    }

    @Override
    protected void addServiceDependencies(ServiceBuilder<FrameworkState> builder) {
        builder.addDependency(IntegrationServices.DEPLOYMENT_PROVIDER_PLUGIN, DeploymentProvider.class, frameworkState.injectedDeploymentFactory);
        builder.addDependency(IntegrationServices.FRAMEWORK_EVENTS, FrameworkEvents.class, frameworkState.injectedFrameworkEvents);
        builder.addDependency(IntegrationServices.FRAMEWORK_MODULE_LOADER, FrameworkModuleLoader.class, frameworkState.injectedModuleLoader);
        builder.addDependency(IntegrationServices.NATIVE_CODE_PLUGIN, NativeCode.class, frameworkState.injectedNativeCode);
        builder.addDependency(Services.RESOLVER, XResolver.class, frameworkState.injectedResolverPlugin);
        builder.addDependency(IntegrationServices.SERVICE_MANAGER, ServiceManager.class, frameworkState.injectedServiceManager);
        builder.addDependency(IntegrationServices.SYSTEM_BUNDLE_INTERNAL, SystemBundleState.class, frameworkState.injectedSystemBundle);
        builder.setInitialMode(Mode.ON_DEMAND);
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        super.start(startContext);
        getBundleManagerPlugin().injectedFramework.inject(frameworkState);
    }

    @Override
    protected FrameworkState createServiceValue(StartContext startContext) throws StartException {
        return frameworkState;
    }

    @Override
    public void stop(StopContext context) {
        getBundleManagerPlugin().injectedFramework.uninject();
    }

    private class FrameworkCreated extends AbstractIntegrationService<BundleContext> {

        final InjectedValue<BundleContext> injectedBundleContext = new InjectedValue<BundleContext>();

        private FrameworkCreated() {
            super(Services.FRAMEWORK_CREATE);
        }

        @Override
        protected void addServiceDependencies(ServiceBuilder<BundleContext> builder) {
            builder.addDependency(IntegrationServices.SYSTEM_CONTEXT_INTERNAL, BundleContext.class, injectedBundleContext);
            builder.addDependency(IntegrationServices.FRAMEWORK_CREATE_INTERNAL);
            builder.setInitialMode(initialMode);
        }

        @Override
        protected BundleContext createServiceValue(StartContext startContext) throws StartException {
            return injectedBundleContext.getValue();
        }
    }
}
