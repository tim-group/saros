/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.core.observables;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.core.invitation.SessionNegotiation;
import de.fu_berlin.inf.dpp.net.JID;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Observable which keeps track of all InvitationProcesses currently running.
 * <p/>
 * This class is used to that everybody can have an easy access to the
 * InvitationProcesses.
 */
@Component(module = "observables")
// TODO removeAll the ID part here, there should only be one invitation per JID
// TODO rename to SessionNegotiationProcessObservable
public class InvitationProcessObservable {

    private static Logger log = Logger
        .getLogger(InvitationProcessObservable.class);

    private Map<JID, List<SessionNegotiation>> processes = new HashMap<JID, List<SessionNegotiation>>();

    /**
     * Returns an invitation process from the currently running invitation
     * processes.
     * 
     * @param jid
     *            the JID of the remote contact that is part of the invitation
     *            process
     * @param id
     *            the ID of the invitation process
     * @return an {@link SessionNegotiation} object or <code>null</code> if no
     *         such process exists
     */
    public synchronized SessionNegotiation getInvitationProcess(JID jid,
        String id) {
        List<SessionNegotiation> currentProcesses = processes.get(jid);

        if (currentProcesses == null || currentProcesses.isEmpty()) {
            return null;
        }

        for (SessionNegotiation process : currentProcesses) {
            if (process.getID().equals(id)) {
                return process;
            }
        }

        return null;
    }

    /**
     * Returns if the a user with the given {@linkplain JID} is currently in a
     * session negotiation process.
     * 
     * @param jid
     *            the JID of the user
     * @return <code>true</code> if the user is currently in a session
     *         negotiation process, <code>false</code> otherwise
     */
    public synchronized boolean isInSessionNegotiation(JID jid) {
        List<SessionNegotiation> currentProcesses = processes.get(jid);
        return currentProcesses != null && !currentProcesses.isEmpty();
    }

    /**
     * Adds an invitation process to the current set.
     * 
     * @param process
     *            the process to add
     */
    public synchronized void addInvitationProcess(SessionNegotiation process) {
        List<SessionNegotiation> currentProcesses = processes.get(process
            .getPeer());

        if (currentProcesses == null) {
            currentProcesses = new ArrayList<SessionNegotiation>();
            processes.put(process.getPeer(), currentProcesses);
        }

        if (currentProcesses.size() >= 1) {
            log.warn("there is already a running invitation for contact: "
                + process.getPeer());
        }

        for (SessionNegotiation currentProcess : currentProcesses) {
            if (currentProcess.getID().equals(process.getID())) {
                log.warn("an invitation with ID " + process.getID()
                    + " is already registered");
                return;
            }
        }

        currentProcesses.add(process);
    }

    /**
     * Removes an invitation process from the current set.
     * 
     * @param process
     *            the process to removeAll
     */

    public synchronized void removeInvitationProcess(SessionNegotiation process) {
        List<SessionNegotiation> currentProcesses = processes.get(process
            .getPeer());

        if (currentProcesses == null) {
            currentProcesses = Collections.emptyList();
        }

        for (Iterator<SessionNegotiation> it = currentProcesses.iterator(); it
            .hasNext();) {

            SessionNegotiation currentProcess = it.next();
            if (currentProcess.getID().equals(process.getID())) {
                it.remove();
                return;
            }
        }

        log.warn("an invitation with ID " + process.getID()
            + " is not registered");

    }

    /**
     * Returns a snap shot of all currently running invitation processes.
     * 
     * @return a list of the currently running invitation processes which may be
     *         empty
     */
    public synchronized List<SessionNegotiation> getProcesses() {
        List<SessionNegotiation> runningProcesses = new ArrayList<SessionNegotiation>();

        for (List<SessionNegotiation> processes : this.processes.values()) {
            runningProcesses.addAll(processes);
        }

        return runningProcesses;
    }
}
