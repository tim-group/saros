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

package de.fu_berlin.inf.dpp.intellij.ui.wizards.core;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Class represents default wizard container.
 * Usage:
 *
 * Wizard wiz = new Wizard("title");
 * wiz.registerPage();
 * wiz.create();
 *
 */

public class Wizard
{
    public static final String NEXT_ACTION = "next";
    public static final String BACK_ACTION = "back";
    public static final String CANCEL_ACTION = "cancel";

    private WizardModel wizardModel;
    private WizardController wizardController;

    private JDialog wizard;

    private JPanel cardPanel;
    private HeaderPanel headPanel;

    private CardLayout cardLayout;

    private NavigationPanel navigationPanel;

    private Component parent = null;//Saros.instance().getMainPanel();

    /**
     * Constructor creates wizard structure.
     *
     * @param title window title
     */
    public Wizard(String title)
    {
        JFrame frame = new JFrame();
        frame.setLocationRelativeTo(parent);

        wizardModel = new WizardModel();
        wizard = new JDialog(frame, title);

        wizard.setSize(600, 400);
        wizard.setResizable(false);

        wizardController = new WizardController(this);

        navigationPanel = new NavigationPanel();
        navigationPanel.addActionListener(wizardController);

        cardPanel = new JPanel();
        headPanel = new HeaderPanel("", "");

        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);
    }

    /**
     * Creates UI. Should be called explicitly after all settings for wizard are finished.
     */
    public void create()
    {
        wizard.setLayout(new BorderLayout());

        wizard.getContentPane().add(headPanel, BorderLayout.NORTH);

        cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
        cardPanel.setVisible(true);


        wizard.getContentPane().add(cardPanel, BorderLayout.CENTER);

        navigationPanel.create();
        wizard.getContentPane().add(navigationPanel, BorderLayout.SOUTH);


        if (wizardModel.getSize() > 0)
        {
            wizardModel.setCurrentPositionIndex(0);
            setCurrentPage(wizardModel.getCurrentPage());
        }
        else
        {
            navigationPanel.setPosition(NavigationPanel.Position.zero);
        }

        wizard.setVisible(true);

    }

    /**
     * Registers pages used in wizard.
     * Should be added before using wizard.
     *
     * @param page  AbstractWizardPage
     */
    public void registerPage(AbstractWizardPage page)
    {
        page.setWizard(this);
        cardPanel.add(page, page.getId());
        cardLayout.addLayoutComponent(page, page.getId());
        wizardModel.registerPanel(page.getId().toString(), page);
    }

    /**
     * Called by framework internally when user navigates wizard
     *
     * @param page AbstractWizardPage
     */
    protected void setCurrentPage(AbstractWizardPage page)
    {
        navigationPanel.setButtonsEnabled(false);

        AbstractWizardPage oldPanel = wizardModel.getCurrentPage();

        if (oldPanel != null)
        {
            oldPanel.aboutToHidePanel();
        }

        wizardModel.setCurrentPagePosition(page);

        if (page != null)
        {
            wizardModel.getCurrentPage().aboutToDisplayPanel();

            if (wizardModel.getNextPage() == null)
            {
                navigationPanel.setPosition(NavigationPanel.Position.last);
            }
            else if (wizardModel.getBackPage() == null)
            {
                navigationPanel.setPosition(NavigationPanel.Position.first);
            }
            else
            {
                navigationPanel.setPosition(NavigationPanel.Position.middle);
            }

            if (page.getNextButtonTitle() != null)
            {
                navigationPanel.getNextButton().setText(page.getNextButtonTitle());
            }
            else
            {
                navigationPanel.getNextButton().setVisible(false);
            }

            cardLayout.show(cardPanel, page.getId().toString());

            navigationPanel.setButtonsEnabled(true);

            wizardModel.getCurrentPage().displayingPanel();
        }

    }

    public HeaderPanel getHeadPanel()
    {
        return headPanel;
    }

    public WizardModel getWizardModel()
    {
        return wizardModel;
    }

    public void setWizardController(WizardController wizardController)
    {
        this.wizardController = wizardController;
    }

    public NavigationPanel getNavigationPanel()
    {
        return navigationPanel;
    }

    public void setNavigationPanel(NavigationPanel navigationPanel)
    {
        this.navigationPanel = navigationPanel;
    }

    public JDialog getWizard()
    {
        return wizard;
    }

    public void setWizard(JDialog wizard)
    {
        this.wizard = wizard;
    }

    public void setHeadPanel(HeaderPanel headPanel)
    {
        this.headPanel = headPanel;
    }

    public void close()
    {
        this.wizard.dispose();
    }
}
