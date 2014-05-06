package de.fu_berlin.inf.dpp.core.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import de.fu_berlin.inf.dpp.core.context.ISarosContext;
import de.fu_berlin.inf.dpp.core.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.core.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.core.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.core.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.core.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.core.project.internal.SarosSession;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.net.XMPPConnectionService;

import de.fu_berlin.inf.dpp.session.ISarosSession;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SarosSession.class, SarosSessionManager.class })
public class SarosSessionManagerTest {

    private class DummyError extends Error {
        private static final long serialVersionUID = 1L;
    }

    private class StateVerifyListener extends AbstractSarosSessionListener
    {
        int state = -1;

        @Override
        public void sessionStarting(ISarosSession newSarosSession) {
            checkAndSetState(-1, 0);
        }

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            checkAndSetState(0, 1);
        }

        @Override
        public void sessionEnding(ISarosSession oldSarosSession) {
            checkAndSetState(1, 2);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            checkAndSetState(2, -1);
        }

        private void checkAndSetState(int expectedState, int newState) {
            assertEquals("listener methods invoked in wrong order", state,
                expectedState);
            state = newState;
        }
    }

    private class ErrorThrowingListener extends AbstractSarosSessionListener {

        @Override
        public void sessionStarting(ISarosSession newSarosSession) {
            throw new DummyError();
        }

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            throw new DummyError();
        }

        @Override
        public void sessionEnding(ISarosSession oldSarosSession) {
            throw new DummyError();
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            throw new DummyError();
        }
    }

    private SarosSessionManager manager;

    @Before
    public void setUp() throws Exception {
        SarosSession session = PowerMock.createNiceMock(SarosSession.class);
        XMPPConnectionService network = PowerMock
            .createNiceMock(XMPPConnectionService.class);

        PreferenceUtils preferences = PowerMock
            .createNiceMock(PreferenceUtils.class);

        PowerMock.expectNew(SarosSession.class, EasyMock.anyInt(),
            EasyMock.anyObject(ISarosContext.class)).andStubReturn(session);

        PowerMock.replayAll();

        manager = new SarosSessionManager(network,
            new SarosSessionObservable(), new SessionIDObservable(),
            new InvitationProcessObservable(),
            new ProjectNegotiationObservable(), preferences);
    }

    @Test
    public void testStartStopListenerCallback() {
        manager.addSarosSessionListener(new StateVerifyListener());
        manager.startSession(new HashMap<IProject, List<IResource>>());
        manager.stopSarosSession();
    }

    @Test
    public void testMultipleStarts() {
        manager.addSarosSessionListener(new StateVerifyListener());
        manager.startSession(new HashMap<IProject, List<IResource>>());
        manager.startSession(new HashMap<IProject, List<IResource>>());
        manager.stopSarosSession();
    }

    @Test
    public void testMultipleStops() {
        manager.addSarosSessionListener(new StateVerifyListener());
        manager.startSession(new HashMap<IProject, List<IResource>>());
        manager.stopSarosSession();
        manager.stopSarosSession();
    }

    @Test(expected = DummyError.class)
    public void testListenerDispatchIsNotCatchingErrors() {
        manager.addSarosSessionListener(new ErrorThrowingListener());
        manager.startSession(new HashMap<IProject, List<IResource>>());
        manager.stopSarosSession();
    }

    @Test
    public void testRecursiveStop() {
        ISarosSessionListener listener = new AbstractSarosSessionListener() {
            int count = 0;

            @Override
            public void sessionEnding(ISarosSession oldSarosSession) {
                assertTrue("stopSession is executed recusive", count == 0);
                count++;
                manager.stopSarosSession();
            }

        };
        manager.addSarosSessionListener(listener);
        manager.startSession(new HashMap<IProject, List<IResource>>());
        manager.stopSarosSession();
    }

    @Test
    public void testRecursiveStart() {
        ISarosSessionListener listener = new AbstractSarosSessionListener() {
            int count = 0;

            @Override
            public void sessionStarting(ISarosSession oldSarosSession) {
                assertTrue("startSession is executed recusive", count == 0);
                count++;
                manager.startSession(new HashMap<IProject, List<IResource>>());
            }

        };
        manager.addSarosSessionListener(listener);
        manager.startSession(new HashMap<IProject, List<IResource>>());
    }

    @Test(expected = IllegalStateException.class)
    public void stopWhileStarting() {

        final AtomicReference<RuntimeException> exception = new AtomicReference<RuntimeException>();

        ISarosSessionListener listener = new AbstractSarosSessionListener() {
            @Override
            public void sessionStarting(ISarosSession oldSarosSession) {
                try {
                    manager.stopSarosSession();
                } catch (RuntimeException e) {
                    exception.set(e);
                }
            }

        };
        manager.addSarosSessionListener(listener);
        manager.startSession(new HashMap<IProject, List<IResource>>());

        RuntimeException rte = exception.get();

        if (rte != null)
            throw rte;
    }

    @Test(expected = IllegalStateException.class)
    public void startWhileStopping() {

        final AtomicReference<RuntimeException> exception = new AtomicReference<RuntimeException>();

        ISarosSessionListener listener = new AbstractSarosSessionListener() {
            @Override
            public void sessionEnding(ISarosSession oldSarosSession) {
                try {
                    manager
                        .startSession(new HashMap<IProject, List<IResource>>());
                } catch (RuntimeException e) {
                    exception.set(e);
                }
            }

        };
        manager.addSarosSessionListener(listener);
        manager.startSession(new HashMap<IProject, List<IResource>>());
        manager.stopSarosSession();

        RuntimeException rte = exception.get();

        if (rte != null)
            throw rte;
    }
}
