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

package de.fu_berlin.inf.dpp.intellij.ui.wizards.pages;

import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.core.AbstractWizardPage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Selects local project
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-05-15
 * Time: 13:38
 */

public class SelectProjectPage extends AbstractWizardPage
{

    private enum ProjectOptions
    {
        newProject, existingProject
    }

    private JFileChooser fileChooser;

    private JRadioButton rdbCreateNewProject;
    private JRadioButton rdbUseExistingProject;

    private JTextField fldNewProjectName;
    private JTextField fldExistingProjectName;

    private JButton browseButton;

    private String projectName;
    private String newProjectName;
    private String projectBase;

    private JLabel lblNewProject;
    private JLabel lblExistingProject;


    /**
     * Creates new wizard page with new identification ID
     *
     * @param id identification
     */
    public SelectProjectPage(Object id)
    {
        super(id);
    }

    public void create()
    {

        JTabbedPane tabbedPane = new JTabbedPane();
        add(tabbedPane);

        JPanel pnlProject = new JPanel();
        tabbedPane.addTab(projectName, pnlProject);

        pnlProject.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 12, 8, 12);

        rdbCreateNewProject = new JRadioButton(Messages.EnterProjectNamePage_create_new_project);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.anchor = GridBagConstraints.LINE_START;
        pnlProject.add(rdbCreateNewProject, c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.LINE_END;
        lblNewProject = new JLabel(Messages.EnterProjectNamePage_project_name);
        pnlProject.add(lblNewProject, c);

        fldNewProjectName = new JTextField();
        fldNewProjectName.setText(newProjectName);

        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.LINE_START;
        pnlProject.add(fldNewProjectName, c);

        rdbUseExistingProject = new JRadioButton(Messages.EnterProjectNamePage_use_existing_project);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 3;
        c.anchor = GridBagConstraints.LINE_START;
        pnlProject.add(rdbUseExistingProject, c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.LINE_END;
        lblExistingProject = new JLabel(Messages.EnterProjectNamePage_project_name);
        pnlProject.add(lblExistingProject, c);

        fldExistingProjectName = new JTextField();

        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.LINE_START;
        pnlProject.add(fldExistingProjectName, c);

        browseButton = new JButton("Browse");
        browseButton.setSize(20, 10);

        c.gridx = 2;
        c.gridy = 3;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.CENTER;
        pnlProject.add(browseButton, c);

        //set table size
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        pnlProject.add(Box.createHorizontalStrut(100), c);

        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth = 1;
        pnlProject.add(Box.createHorizontalStrut(300), c);

        c.gridx = 2;
        c.gridy = 4;
        c.gridwidth = 1;
        pnlProject.add(Box.createHorizontalStrut(20), c);

        fileChooser = new JFileChooser(new File(projectBase));

        //radio
        rdbCreateNewProject.setActionCommand(ProjectOptions.newProject.toString());
        rdbCreateNewProject.setSelected(true);
        rdbUseExistingProject.setActionCommand(ProjectOptions.existingProject.toString());
        rdbUseExistingProject.setSelected(false);


        ActionListener rdbListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (e.getActionCommand().equalsIgnoreCase(ProjectOptions.existingProject.toString()))
                {
                    doExistingProject();
                }
                else
                {
                    doNewProject();
                }

            }
        };
        rdbCreateNewProject.addActionListener(rdbListener);
        rdbUseExistingProject.addActionListener(rdbListener);

        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        ActionListener browseListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int returnVal = fileChooser.showOpenDialog(SelectProjectPage.this);
                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    File file = fileChooser.getSelectedFile();
                    fldExistingProjectName.setText(file.getAbsolutePath());

                }


            }
        };
        browseButton.addActionListener(browseListener);

        doNewProject();
    }

    private void doExistingProject()
    {
        rdbCreateNewProject.setSelected(false);
        rdbUseExistingProject.setSelected(true);

        fldNewProjectName.setEnabled(false);
        lblNewProject.setEnabled(false);

        fldExistingProjectName.setEnabled(true);
        lblExistingProject.setEnabled(true);
        browseButton.setEnabled(true);
    }

    private void doNewProject()
    {
        rdbCreateNewProject.setSelected(true);
        rdbUseExistingProject.setSelected(false);

        fldNewProjectName.setEnabled(true);
        lblNewProject.setEnabled(true);

        lblExistingProject.setEnabled(false);
        fldExistingProjectName.setEnabled(false);
        browseButton.setEnabled(false);
    }

    @Override
    public void displayingPanel()
    {
        wizard.getNavigationPanel().setVisibleBack(false);
        wizard.getNavigationPanel().setVisibleNext(true);
    }


    public String getNewProjectName()
    {
        return fldNewProjectName.isEnabled() ? fldNewProjectName.getText() : null;
    }

    public String getExistingProjectPath()
    {
        return fldExistingProjectName.isEnabled() ? fldExistingProjectName.getText() : null;
    }

    public String getExistingProjectName()
    {
        return fldExistingProjectName.isEnabled() ? new File(getExistingProjectPath()).getName() : null;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public void setNewProjectName(String newProjectName)
    {
        this.newProjectName = newProjectName;
    }

    public void setProjectBase(String projectBase)
    {
        this.projectBase = projectBase;
    }


}
