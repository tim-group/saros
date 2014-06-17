package de.fu_berlin.inf.dpp;

import de.fu_berlin.inf.dpp.versioning.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({

de.fu_berlin.inf.dpp.account.TestSuite.class,




de.fu_berlin.inf.dpp.util.TestSuite.class,

TestSuite.class

})
public class AllTestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}
