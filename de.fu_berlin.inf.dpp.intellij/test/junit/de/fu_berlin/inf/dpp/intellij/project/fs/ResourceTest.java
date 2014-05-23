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
 * Time: 07:48
 */

public class ResourceTest
{
    @Test
    public void testFileObjectEqual()
    {
        ProjectImp project = new ProjectImp("TestProject");
        String sFile = "c:/temp/java/Test.java";
        File f1 = new File(sFile);
        File f2 = new File(sFile);

        FileImp file1 = new FileImp(project,f1);
        FileImp file2 = new FileImp(project,f2);

        Assert.assertTrue("Not equal",file1.equals(file2));
    }

    @Test
    public void testFileObjectNotEqual()
    {
        ProjectImp project = new ProjectImp("TestProject");
        String sFile1 = "c:/temp/java/Test1.java";
        String sFile2 = "c:/temp/java/Test2.java";
        File f1 = new File(sFile1);
        File f2 = new File(sFile2);

        FileImp file1 = new FileImp(project,f1);
        FileImp file2 = new FileImp(project,f2);

        Assert.assertNotEquals("Equal", file1, file2);
    }

    @Test
    public void testFileObjectCollection()
    {
        ProjectImp project = new ProjectImp("TestProject");
        String sFile1 = "c:/temp/java/Test1.java";
        String sFile2 = "c:/temp/java/Test2.java";
        String sFile3 = "c:/temp/java/Test3.java";
        File f1 = new File(sFile1);
        File f2 = new File(sFile2);
        File f3 = new File(sFile3);

        FileImp file1 = new FileImp(project,f1);
        FileImp file2 = new FileImp(project,f2);
        FileImp file3 = new FileImp(project,f3);
        FileImp file4 = new FileImp(project,f1);

        List list = new ArrayList();
        list.add(file1);
        list.add(file2);


        Assert.assertTrue("Should contain", list.contains(file1));
        Assert.assertTrue("Should contain",list.contains(file2));
        Assert.assertFalse("Should NOT contain", list.contains(file3));
        Assert.assertTrue("Should contain",list.contains(file4));
    }

    @Test
    public void testFolderObjectEqual()
    {
        ProjectImp project = new ProjectImp("TestProject");
        String sFile = "c:/temp/java";
        File f1 = new File(sFile);
        File f2 = new File(sFile);

        FolderImp folder1 = new FolderImp(project,f1);
        FolderImp folder2 = new FolderImp(project,f2);

        Assert.assertEquals("Not equal",folder1,folder2);
    }

    @Test
    public void testFolderObjectNotEqual()
    {
        ProjectImp project = new ProjectImp("TestProject");
        String sFile1 = "c:/temp/java1";
        String sFile2 = "c:/temp/java2";
        File f1 = new File(sFile1);
        File f2 = new File(sFile2);

        FolderImp folder1 = new FolderImp(project,f1);
        FolderImp folder2 = new FolderImp(project,f2);

        Assert.assertNotEquals("Equal", folder1, folder2);
    }

    @Test
    public void testFolderObjectCollection()
    {
        ProjectImp project = new ProjectImp("TestProject");

        FolderImp file1 = new FolderImp(project,new File("c:/temp/java1"));
        FolderImp file2 =  new FolderImp(project,new File("c:/temp/java2"));
        FolderImp file3 =  new FolderImp(project,new File("c:/temp/java3"));
        FolderImp file4 =  new FolderImp(project,new File("c:/temp/java1"));

        List list = new ArrayList();
        list.add(file1);
        list.add(file2);


        Assert.assertTrue("Should contain",list.contains(file1));
        Assert.assertTrue("Should contain",list.contains(file2));
        Assert.assertFalse("Should NOT contain",list.contains(file3));
        Assert.assertTrue("Should contain",list.contains(file4));
    }


}
