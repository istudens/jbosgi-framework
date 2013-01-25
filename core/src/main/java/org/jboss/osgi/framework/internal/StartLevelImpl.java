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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.osgi.framework.spi.BundleManager;
import org.jboss.osgi.framework.spi.FrameworkEvents;
import org.jboss.osgi.framework.spi.StartLevelSupport;
import org.jboss.osgi.framework.spi.StorageState;
import org.jboss.osgi.resolver.XBundle;
import org.jboss.osgi.resolver.XBundleRevision;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.service.startlevel.StartLevel;

/**
 * An implementation of the {@link StartLevel} service.
 *
 * @author <a href="david@redhat.com">David Bosschaert</a>
 * @author Thomas.Diesler@jboss.com
 */
public final class StartLevelImpl implements StartLevelSupport {

    private final BundleManagerPlugin bundleManager;
    private final FrameworkEvents events;
    private final ExecutorService executorService;
    private final AtomicBoolean immediateExecution;
    private AtomicInteger initialBundleStartLevel = new AtomicInteger(1);
    private AtomicInteger startLevel = new AtomicInteger(0);
    private AtomicBoolean changingStartLevel = new AtomicBoolean();

    public StartLevelImpl(BundleManager bundleManager, FrameworkEvents frameworkEvents, ExecutorService executorService, AtomicBoolean immediateExecution) {
        this.bundleManager = BundleManagerPlugin.assertBundleManagerPlugin(bundleManager);
        this.events = frameworkEvents;
        this.executorService = executorService;
        this.immediateExecution = immediateExecution;
    }

    @Override
    public void enableImmediateExecution(boolean enable) {
        this.immediateExecution.set(enable);
    }

    @Override
    public boolean isChangingStartLevel() {
        return changingStartLevel.get();
    }

    @Override
    public int getStartLevel() {
        return startLevel.get();
    }

    @Override
    public synchronized void setStartLevel(final int level) {
        final Bundle sysbundle = bundleManager.getSystemBundle();
        if (level > getStartLevel()) {
            Runnable runner = new Runnable() {
                @Override
                public void run() {
                    LOGGER.infoIncreasingStartLevel(getStartLevel(), level);
                    increaseStartLevel(level);
                    events.fireFrameworkEvent(sysbundle, FrameworkEvent.STARTLEVEL_CHANGED, null);
                }
            };
            executeTask(runner);
        } else if (level < getStartLevel()) {
            Runnable runner = new Runnable() {
                @Override
                public void run() {
                    LOGGER.infoDecreasingStartLevel(getStartLevel(), level);
                    decreaseStartLevel(level);
                    events.fireFrameworkEvent(sysbundle, FrameworkEvent.STARTLEVEL_CHANGED, null);
                }
            };
            executeTask(runner);
        }
    }

    @Override
    public int getBundleStartLevel(Bundle bundle) {
        if (bundle == null)
            throw MESSAGES.illegalArgumentNull("bundle");

        return getBundleStartLevelState(bundle).getLevel();
    }

    @Override
    public void setBundleStartLevel(Bundle bundle, int level) {
        if (bundle == null)
            throw MESSAGES.illegalArgumentNull("bundle");

        if (bundle.getBundleId() == 0)
            throw MESSAGES.illegalArgumentStartLevelOnSystemBundles();

        final XBundle hostBundle = (XBundle) bundle;
        getBundleStartLevelState(bundle).setLevel(level);

        if (level <= getStartLevel()) {
            // If the bundle is active or starting, we don't need to start it again
            if ((bundle.getState() & (Bundle.ACTIVE | Bundle.STARTING)) > 0)
                return;

            if (isBundlePersistentlyStarted(bundle)) {
                LOGGER.infoStartingBundleDueToStartLevel(hostBundle);
                Runnable runner = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int opts = Bundle.START_TRANSIENT;
                            if (isBundleActivationPolicyUsed(hostBundle))
                                opts |= Bundle.START_ACTIVATION_POLICY;

                            hostBundle.start(opts);
                        } catch (BundleException e) {
                            events.fireFrameworkEvent(hostBundle, FrameworkEvent.ERROR, e);
                        }
                    }
                };

                executeTask(runner);
            }
        } else {
            // If the bundle is not active we don't need to stop it
            if ((bundle.getState() & (Bundle.ACTIVE | Bundle.STARTING)) == 0)
                return;

            LOGGER.infoStoppingBundleDueToStartLevel(hostBundle);
            Runnable runner = new Runnable() {
                @Override
                public void run() {
                    try {
                        hostBundle.stop(Bundle.STOP_TRANSIENT);
                    } catch (BundleException e) {
                        events.fireFrameworkEvent(hostBundle, FrameworkEvent.ERROR, e);
                    }
                }
            };

            executeTask(runner);
        }
    }

    @Override
    public int getInitialBundleStartLevel() {
        return initialBundleStartLevel.get();
    }

    @Override
    public void setInitialBundleStartLevel(int startlevel) {
        initialBundleStartLevel.set(startlevel);
    }

    @Override
    public boolean isBundlePersistentlyStarted(Bundle bundle) {
        if (bundle == null)
            throw MESSAGES.illegalArgumentNull("bundle");
        return getBundleStartLevelState(bundle).isStarted();
    }

    @Override
    public void setBundlePersistentlyStarted(XBundle bundle, boolean started) {
        if (bundle == null)
            throw MESSAGES.illegalArgumentNull("bundle");
        getBundleStartLevelState(bundle).setStarted(started);
    }

    @Override
    public boolean isBundleActivationPolicyUsed(Bundle bundle) {
        boolean result = false;
        if (bundle instanceof AbstractBundleState) {
            AbstractBundleState<?> bundleState = AbstractBundleState.assertBundleState(bundle);
            StorageState storageState = bundleState.getStorageState();
            result = storageState.isBundleActivationPolicyUsed();
        }
        return result;
    }

    /**
     * Increases the Start Level of the Framework in the current thread.
     *
     * @param level the target Start Level to which the Framework should move.
     */
    @Override
    public synchronized void increaseStartLevel(int level) {

        try {
            changingStartLevel.set(true);

            // Sort the bundles after their bundle id
            List<XBundle> bundles = new ArrayList<XBundle>(bundleManager.getBundles());
            Comparator<XBundle> comparator = new Comparator<XBundle>() {
                @Override
                public int compare(XBundle b1, XBundle b2) {
                    return (int) (b1.getBundleId() - b2.getBundleId());
                }
            };
            Collections.sort(bundles, comparator);


            while (startLevel.get() < level) {
                startLevel.incrementAndGet();
                LOGGER.infoStartingBundlesForStartLevel(startLevel.get());
                for (XBundle bundle : bundles) {
                    if (bundle.getBundleId() == 0 || bundle.isFragment())
                        continue;

                    BundleStartLevelState state = getBundleStartLevelState(bundle);
                    if (state.getLevel() == startLevel.get() && state.isStarted()) {
                        try {
                            int opts = Bundle.START_TRANSIENT;
                            if (isBundleActivationPolicyUsed(bundle)) {
                                opts |= Bundle.START_ACTIVATION_POLICY;
                            }
                            bundle.start(opts);
                        } catch (Throwable e) {
                            events.fireFrameworkEvent(bundle, FrameworkEvent.ERROR, e);
                        }
                    }
                }
            }

        } finally {
            changingStartLevel.set(false);
        }
    }

    /**
     * Decreases the Start Level of the Framework in the current thread.
     *
     * @param level the target Start Level to which the Framework should move.
     */
    @Override
    public synchronized void decreaseStartLevel(int level) {
        try {
            changingStartLevel.set(true);

            while (startLevel.get() > level) {
                LOGGER.infoStoppingBundlesForStartLevel(level);

                // Sort the bundles after their bundle id
                List<XBundle> bundles = new ArrayList<XBundle>(bundleManager.getBundles());
                Comparator<XBundle> comparator = new Comparator<XBundle>() {
                    @Override
                    public int compare(XBundle b1, XBundle b2) {
                        return (int) (b1.getBundleId() - b2.getBundleId());
                    }
                };
                Collections.sort(bundles, comparator);
                Collections.reverse(bundles);

                for (XBundle bundle : bundles) {
                    if (bundle.getBundleId() == 0 || bundle.isFragment())
                        continue;

                    BundleStartLevelState state = getBundleStartLevelState(bundle);
                    if (state.getLevel() == startLevel.get()) {
                        try {
                            bundle.stop(Bundle.STOP_TRANSIENT);
                        } catch (Throwable e) {
                            events.fireFrameworkEvent(bundle, FrameworkEvent.ERROR, e);
                        }
                    }
                }
                startLevel.decrementAndGet();
            }

        } finally {
            changingStartLevel.set(false);
        }
    }

    private BundleStartLevelState getBundleStartLevelState(Bundle bundle) {
        if (bundle instanceof Framework)
            return new BundleStartLevelState(bundle);

        BundleStartLevelState state = null;
        if (bundle instanceof XBundle) {
            XBundle bundleState = (XBundle) bundle;
            state = bundleState.getAttachment(BundleStartLevelState.class);
            if (state == null) {
                state = new BundleStartLevelState(bundle);
                bundleState.addAttachment(BundleStartLevelState.class, state);
            }
        }
        return state;
    }

    private void executeTask(Runnable runner) {
        if (!executorService.isShutdown()) {
            if (immediateExecution.get()) {
                runner.run();
            } else {
                executorService.execute(runner);
            }
        }
    }

    class BundleStartLevelState {
        final Bundle bundle;
        boolean started;
        int level;

        public BundleStartLevelState(Bundle bundle) {
            this.bundle = bundle;
            if (bundle.getBundleId() > 0) {
                XBundle bundleState = (XBundle) bundle;
                level = !bundleState.isFragment() ? getInitialBundleStartLevel() : 0;
                if (bundle instanceof HostBundleState) {
                    XBundleRevision brev = bundleState.getBundleRevision();
                    StorageState storageState = ((HostBundleRevision) brev).getStorageState();
                    level = storageState.getStartLevel();
                }
            }
        }

        int getLevel() {
            return level;
        }

        void setLevel(int level) {
            this.level = level;
            if (bundle instanceof HostBundleState) {
                XBundleRevision brev = ((XBundle)bundle).getBundleRevision();
                StorageState storageState = ((HostBundleRevision) brev).getStorageState();
                storageState.setStartLevel(level);
            }
        }

        boolean isStarted() {
            return started;
        }

        void setStarted(boolean started) {
            this.started = started;
            if (bundle instanceof HostBundleState) {
                XBundleRevision brev = ((XBundle)bundle).getBundleRevision();
                StorageState storageState = ((HostBundleRevision) brev).getStorageState();
                storageState.setPersistentlyStarted(started);
            }
        }
    }
}
