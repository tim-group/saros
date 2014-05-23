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

package de.fu_berlin.inf.dpp.intellij.project.fs;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-05-23
 * Time: 08:56
 */

public class ResourceWithProjectTest
{
    @Test
    public void testFileObjectEqual()
    {
        ProjectImp p = new ProjectImp("Test",new File("c:/temp/project1"));
        ProjectImp p2 = new ProjectImp("Test",new File("c:/temp/project1"));

        FileImp file1 = new FileImp(p,new File("c:/temp/project1/Test1.java"));
        FileImp file2 = new FileImp(p,new File("Test1.java"));
        FileImp file3 = new FileImp(p2,new File("Test1.java"));

        Assert.assertTrue("Should be equal",file1.equals(file2));
        Assert.assertTrue("Should be equal",file1.equals(file3));
    }

    @Test
    public void testFileObjectNotEqual()
    {
        ProjectImp p1 = new ProjectImp("Test",new File("c:/temp/project1"));
        ProjectImp p2 = new ProjectImp("Test",new File("c:/temp/project2"));

        FileImp file1 = new FileImp(p1,new File("Test1.java"));
        FileImp file2 = new FileImp(p2,new File("Test1.java"));

        Assert.assertFalse("Should be equal",file1.equals(file2));
    }
}
