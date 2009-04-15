package de.fu_berlin.inf.dpp.concurrent.watchdog;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

/**
 * This class is responsible for opening the SessionView if an inconsistency has
 * been detected.
 * 
 * @component The single instance of this class per application is created by
 *            PicoContainer in the central plug-in class {@link Saros}
 */
public class SessionViewOpener {

    private static final Logger log = Logger.getLogger(SessionViewOpener.class
        .getName());

    public SessionViewOpener(IsInconsistentObservable isInconsistentObservable,
        final SarosUI sarosUI) {

        isInconsistentObservable.add(new ValueChangeListener<Boolean>() {
            public void setValue(Boolean inconsistency) {
                if (!inconsistency) {
                    return;
                }
                Util.runSafeSWTSync(log, new Runnable() {
                    public void run() {
                        sarosUI.activateSessionView();
                    }
                });

            }
        });
    }

}
