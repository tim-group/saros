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

import de.fu_berlin.inf.dpp.core.project.ISchedulingRoot;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.2
 * Time: 15.36
 */

public class SchedulingRoot implements ISchedulingRoot
{
    public static final Logger log = Logger.getLogger(SchedulingRoot.class);

    private File workspacePath;
    private Map<String, IProject> projects = new HashMap<String, IProject>();

    public SchedulingRoot(File workspacePath)
    {
        this.workspacePath = workspacePath;
    }

    protected SchedulingRoot()
    {
    }


    @Override
    public IProject getProject(String project)
    {

        IProject prj = projects.get(project);
        if (prj == null)
        {
            File fPrj = new File(this.workspacePath.getAbsolutePath() + PathImp.FILE_SEPARATOR + project);
            ProjectImp myPrj = new ProjectImp(project, fPrj);

            addProject(myPrj);

            return myPrj;
        }
        else
        {
            return prj;
        }
    }

    public void addProject(IProject proj)
    {
        this.projects.put(proj.getName(), proj);
    }

    public void addProject(String name, File path)
    {
        log.info("Add project [" + name + "] path=" + path.getAbsolutePath());
        ProjectImp prj = new ProjectImp(name, path);

        addProject(prj);
    }

    @Override
    public IProject getDefaultProject()
    {
        return null;
    }
}
