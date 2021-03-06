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

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;


/**
 * A {@link URLStreamHandlerFactory} that provides {@link URLStreamHandler} instances which are backed by an OSGi service.
 *
 * The returned handler instances are proxies which allow the URL Stream Handler implementation to be changed at a later point
 * in time (the JRE caches the first URL Stream Handler returned for a given protocol).
 *
 * @author <a href="david@redhat.com">David Bosschaert</a>
 * @author Thomas.Diesler@jboss.com
 * @since 10-Jan-2011
 */
public class OSGiStreamHandlerFactoryService implements URLStreamHandlerFactory {

    private static URLStreamHandlerFactory delegate;

    static void setDelegateFactory(URLStreamHandlerFactory factory) {
        delegate = factory;
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        return delegate != null ? delegate.createURLStreamHandler(protocol) : null;
    }
}
