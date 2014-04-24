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

package de.fu_berlin.inf.dpp.intellij.project.intl;

import de.fu_berlin.inf.dpp.core.exceptions.CoreException;
import de.fu_berlin.inf.dpp.core.monitor.NullProgressMonitor;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.invitation.FileList;
import de.fu_berlin.inf.dpp.invitation.FileListDiff;
import de.fu_berlin.inf.dpp.invitation.FileListFactory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-09
 * Time: 09:19
 */

public class FileSystemTest
{
    @Test
    public void TestFileImp()
    {
        File f = new File("c:/temp/Test.java");
        FileIntl file = null;//new FileIntl(f);

        System.out.println("URI=" + file.getLocationURI());
        System.out.println("NAME=" + file.getName());
        System.out.println("String=" + file.toString());
        System.out.println("FullPath=" + file.getFullPath());
        System.out.println("FullPathPortable=" + file.getFullPath().toPortableString());
    }

    @Test
    public void TestFileWithProject()
    {
        File fp = new File("c:/temp/p1");
        ProjectIntl p = null;//new ProjectIntl("fp");
       // p.setPath(fp);

        File ff = new File("c:/temp/p1/Test.java");
        FileIntl file = new FileIntl(p, ff);

        System.out.println("URI=" + file.getLocationURI());
        System.out.println("NAME=" + file.getName());
        System.out.println("String=" + file.toString());
        System.out.println("FullPath=" + file.getFullPath());
        System.out.println("FullPathPortable=" + file.getFullPath().toPortableString());
        System.out.println("Relative path=" + file.getProjectRelativePath().toString());
    }

    @Test
    public void TestPathImp()
    {
        File f = new File("c:/temp/Test.java");
        IPath p = new PathIntl(f);

        System.out.println("Portable=" + p.toPortableString());
        System.out.println("LastSegment=" + p.lastSegment());
        System.out.println("OS String=" + p.toOSString());
        System.out.println("String=" + p.toString());
        System.out.println("Segments=" + Arrays.asList(p.segments()));
    }

    @Test
    public void testWorkspace()
    {
        try
        {
            File f = new File("c:\\Develop\\Saros\\idea\\test_projects\\");
            WorkspaceIntl.instance().createWorkSpace(f);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
