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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.InvalidDataException;
import de.fu_berlin.inf.dpp.core.project.ISchedulingRoot;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.2
 * Time: 15.36
 */

public class SchedulingRootIntl implements ISchedulingRoot
{
    public static final Logger log = Logger.getLogger(SchedulingRootIntl.class);
    private ProjectManager projectManager = ProjectManager.getInstance();

    private IProject defaultProject;
    private File workspacePath;

    private Map<String, IProject> projects = new HashMap<String, IProject>();

    public SchedulingRootIntl(File workspacePath)
    {
        this.workspacePath = workspacePath;
    }

    protected SchedulingRootIntl()
    {
    }


    @Override
    public IProject getProject(String projectName)
    {
      return   getDefaultProject();

        /*IProject prj = projects.get(projectName);
        if (prj == null)
        {
            try
            {
                Project project = projectManager.loadAndOpenProject(projectName);
                if(project!=null)
                {
                    prj = new ProjectIntl(project);
                    projects.put(project.getName(), prj);
                }
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }


            return prj;
        }
        else
        {
            return prj;
        }*/
    }


    public IProject getDefaultProject()
    {
        if(defaultProject==null)
        {
            Project project = projectManager.getDefaultProject();
            defaultProject = new ProjectIntl(project);
        }

        return defaultProject;
    }

    public IProject addProject(String name, File path)
    {
        log.info("Add project [" + name + "] path=" + path.getAbsolutePath());
        try
        {
            Project project = projectManager.loadAndOpenProject(name);

            if(project!=null)
            {
                IProject prj = new ProjectIntl(project);
                projects.put(project.getName(), prj);
                return prj;
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        catch (JDOMException e)
        {
            e.printStackTrace();
        }
        catch (InvalidDataException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public IProject locateProject(IPath path)
    {
        return null;
    }
}
