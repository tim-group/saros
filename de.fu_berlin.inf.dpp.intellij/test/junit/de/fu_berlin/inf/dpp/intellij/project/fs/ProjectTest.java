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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-05-23
 * Time: 08:33
 */

public class ProjectTest
{
    @Test
    public void testProjectEqual()
    {
        ProjectImp p1 = new ProjectImp("First",new File("c:/temp/first/"));
        ProjectImp p2 = new ProjectImp("First_with_another_name",new File("c:/temp/first/"));

        Assert.assertTrue("Should be equal",p1.equals(p2));
    }

    @Test
    public void testProjectNotEqual()
    {
        ProjectImp p1 = new ProjectImp("First",new File("c:/temp/first/"));
        ProjectImp p2 = new ProjectImp("First",new File("c:/temp/second/"));

        Assert.assertFalse("Should be equal", p1.equals(p2));
    }

    @Test
    public void testProjectCollection()
    {
        ProjectImp p1 = new ProjectImp("First",new File("c:/temp/first/"));
        ProjectImp p2 = new ProjectImp("First",new File("c:/temp/second/"));
        ProjectImp p3 = new ProjectImp("First",new File("c:/temp/second/"));
        ProjectImp p4 = new ProjectImp("First",new File("c:/temp/third/"));

        List list = new ArrayList();
        list.add(p1);
        list.add(p2);

        Assert.assertTrue("Should contain",list.contains(p2));
        Assert.assertFalse("Should NOT contain",list.contains(p4));
        Assert.assertTrue("Should NOT contain",list.contains(p3));

    }
}
