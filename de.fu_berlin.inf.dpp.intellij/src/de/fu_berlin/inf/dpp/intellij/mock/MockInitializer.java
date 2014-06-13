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

package de.fu_berlin.inf.dpp.intellij.mock;

import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.core.preferences.ISecurePreferences;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.intellij.project.fs.Workspace;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.19
 * Time: 17.51
 */

public class MockInitializer
{




    public static void initSecurePrefStore(ISecurePreferences securePrefs)
    {

    }

    public static void initPrefStore(IPreferenceStore configPrefs)
    {

        //  configPrefs.setValue(PreferenceConstants.ENCRYPT_ACCOUNT, false);

    }


    public static Map<String, IProject> createProjectList()
    {
        Map<String, IProject> projects = new HashMap<String, IProject>();

        // ProjectIntl p1 = new ProjectIntl("Testas");
        //  projects.put(p1.getName(),p1);

        // ProjectIntl p2 = new ProjectIntl("RemoteSystemsTempFiles");
        //  projects.put(testProject.getName(), testProject);

        return projects;
    }


}