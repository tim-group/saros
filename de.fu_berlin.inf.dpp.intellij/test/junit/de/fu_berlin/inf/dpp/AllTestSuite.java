package de.fu_berlin.inf.dpp;

import de.fu_berlin.inf.dpp.core.versioning.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({

de.fu_berlin.inf.dpp.accountManagement.TestSuite.class,

de.fu_berlin.inf.dpp.core.editor.colorstorage.TestSuite.class,

de.fu_berlin.inf.dpp.invitation.TestSuite.class,

de.fu_berlin.inf.dpp.net.internal.extensions.TestSuite.class,

de.fu_berlin.inf.dpp.core.project.TestSuite.class,

de.fu_berlin.inf.dpp.core.project.internal.TestSuite.class,


de.fu_berlin.inf.dpp.util.TestSuite.class,

TestSuite.class

})
public class AllTestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}
