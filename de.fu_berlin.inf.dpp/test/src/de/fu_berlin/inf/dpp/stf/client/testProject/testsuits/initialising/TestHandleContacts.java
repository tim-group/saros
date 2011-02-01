package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.initialising;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestHandleContacts extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>init Alice</li>
     * <li>init Bob</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbenchs();
        setUpSaros();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        alice.addBuddyGUIDone(bob);
        bob.addBuddyGUIDone(alice);
    }

    // FIXME these testAddContact assumes that testRemoveContact succeeds
    // FIXME all the other tests in the suite would fail if testAddContact fails

    /**
     * Steps:
     * <ol>
     * <li>bob delete buddy alice.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>bob and alice don't contact each other.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testBobRemoveBuddyAlice() throws RemoteException {
        assertTrue(alice.sarosBuddiesV.hasBuddy(bob.jid));
        assertTrue(bob.sarosBuddiesV.hasBuddy(alice.jid));
        bob.deleteBuddyGUIDone(alice);
        assertFalse(bob.sarosBuddiesV.hasBuddy(alice.jid));
        assertFalse(alice.sarosBuddiesV.hasBuddy(bob.jid));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice delete buddy bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>bob and alice don't contact each other.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testAliceRemoveBuddyBob() throws RemoteException {
        assertTrue(alice.sarosBuddiesV.hasBuddy(bob.jid));
        assertTrue(bob.sarosBuddiesV.hasBuddy(alice.jid));
        alice.deleteBuddyGUIDone(bob);
        assertFalse(bob.sarosBuddiesV.hasBuddy(alice.jid));
        assertFalse(alice.sarosBuddiesV.hasBuddy(bob.jid));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice delete buddy bob first and then add bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>bob and alice contact each other.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testAliceAddBuddyBob() throws RemoteException {
        alice.deleteBuddyGUIDone(bob);
        alice.addBuddyGUIDone(bob);
        assertTrue(bob.sarosBuddiesV.hasBuddy(alice.jid));
        assertTrue(alice.sarosBuddiesV.hasBuddy(bob.jid));
    }

    /**
     * Steps:
     * <ol>
     * <li>bob delete buddy alice first and then add alice again.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>bob and alice contact each other.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testBobAddBuddyAlice() throws RemoteException {
        bob.deleteBuddyGUIDone(alice);
        bob.addBuddyGUIDone(alice);
        assertTrue(bob.sarosBuddiesV.hasBuddy(alice.jid));
        assertTrue(alice.sarosBuddiesV.hasBuddy(bob.jid));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice click toolbar button "Add a new contact".</li>
     * <li>alice enter invalid contact name in the popup window "New contact"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>alice should get error message "Contact look up failed".</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testAddNoValidContact() throws RemoteException {
        alice.sarosBuddiesV.clickToolbarButtonAddANewBuddy();
        alice.sarosBuddiesV.confirmWindowNewBuddy("bob@bla");
        alice.sarosBuddiesV.waitUntilIsShellBuddyLookupFailedActive();
        assertTrue(alice.sarosBuddiesV.isShellBuddyLookupFailedActive());
        alice.sarosBuddiesV.confirmShellBuddyLookupFailed(NO);
    }

    /**
     * Steps:
     * <ol>
     * <li>alice click toolbar button "Add a new contact".</li>
     * <li>alice enter a existed contact name in the popup window "New contact"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>alice should get error message "Contact already added".</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testAddExistedContact() throws RemoteException {
        alice.sarosBuddiesV.clickToolbarButtonAddANewBuddy();
        alice.sarosBuddiesV.confirmWindowNewBuddy(bob.getBaseJid());
        alice.sarosBuddiesV.waitUntilIsShellBuddyAlreadyAddedActive();
        assertTrue(alice.sarosBuddiesV.isShellBuddyAlreadyAddedActive());
        alice.sarosBuddiesV.closeShellBuddyAlreadyAdded();
    }

}
