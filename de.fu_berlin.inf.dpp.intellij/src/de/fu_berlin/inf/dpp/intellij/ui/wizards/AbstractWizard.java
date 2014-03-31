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

package de.fu_berlin.inf.dpp.intellij.ui.wizards;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.3
 * Time: 08.29
 */

public abstract class AbstractWizard
{
    /*private WizardModel wizardModel;
    private WizardController wizardController;

    private JDialog Wizard;

    private JPanel cardPanel;
    private CardLayout cardLayout;

    private JButton backButton;
    private JButton nextButton;
    private JButton cancelButton;

    private int returnCode;

    public AbstractWizard(Frame owner) {

        wizardModel = new WizardModel();
        Wizard = new JDialog(owner);
        wizardController = new WizardController(this);

        initComponents();
    }


    private void initComponents() {

// Code omitted

        JPanel buttonPanel = new JPanel();
        Box buttonBox = new Box(BoxLayout.X_AXIS);

        cardPanel = new JPanel();
        cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));

        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);
        backButton = new JButton();
        nextButton = new JButton();
        cancelButton = new JButton();

        backButton.addActionListener(wizardController);
        nextButton.addActionListener(wizardController);
        cancelButton.addActionListener(wizardController);

        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(new JSeparator(), BorderLayout.NORTH);

        buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
        buttonBox.add(backButton);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(nextButton);
        buttonBox.add(Box.createHorizontalStrut(30));
        buttonBox.add(cancelButton);
        buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);
        Wizard.getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
        Wizard.getContentPane().add(cardPanel, java.awt.BorderLayout.CENTER);

    }*/

}
