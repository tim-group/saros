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
import java.awt.event.ActionListener;

/**
 * Default navigation panel creates panel with 3 default buttons
 * BACK, NEXT and CANCEL
 *
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-05-14
 * Time: 10:01
 */

public class NavigationPanel extends JPanel
{
    public enum Position
    {
        first, middle, last, zero
    }

    public static final String NEXT_ACTION = "next";
    public static final String BACK_ACTION = "back";
    public static final String CANCEL_ACTION = "cancel";

    public static final String TITLE_NEXT = "Next>>>";
    public static final String TITLE_BACK = "<<<Back";
    public static final String TITLE_CANCEL = "Cancel";
    public static final String TITLE_FINISH = "Finish";

    private JButton backButton;
    private JButton nextButton;
    private JButton cancelButton;

    /**
     * Constructor creates default buttons
     */
    public NavigationPanel()
    {
        backButton = new JButton(TITLE_BACK);
        nextButton = new JButton(TITLE_NEXT);
        cancelButton = new JButton(TITLE_CANCEL);
    }

    public void setBackButton(JButton backButton)
    {
        this.backButton = backButton;
    }

    public void setNextButton(JButton nextButton)
    {
        this.nextButton = nextButton;
    }

    public void setCancelButton(JButton cancelButton)
    {
        this.cancelButton = cancelButton;
    }

    public JButton getBackButton()
    {
        return backButton;
    }

    public JButton getNextButton()
    {
        return nextButton;
    }

    public JButton getCancelButton()
    {
        return cancelButton;
    }

    /**
     * Method creates panel UI. Should be triggered explicitly after all required buttons are set.
     */
    public void create()
    {
        JPanel buttonPanel = this;
        Box buttonBox = new Box(BoxLayout.X_AXIS);


        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(new JSeparator(), BorderLayout.NORTH);

        buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));

        if (backButton != null)
        {
            backButton.setActionCommand(BACK_ACTION);
            buttonBox.add(backButton);
            buttonBox.add(Box.createHorizontalStrut(10));
        }

        nextButton.setActionCommand(NEXT_ACTION);
        buttonBox.add(nextButton);

        if (cancelButton != null)
        {
            cancelButton.setActionCommand(CANCEL_ACTION);
            buttonBox.add(Box.createHorizontalStrut(30));
            buttonBox.add(cancelButton);
        }

        buttonPanel.add(buttonBox, BorderLayout.EAST);
    }

    /**
     * Adds action listener
     *
     * @param actionListener action listener
     */
    public void addActionListener(ActionListener actionListener)
    {
        if (backButton != null)
        {
            backButton.addActionListener(actionListener);
        }

        if (nextButton != null)
        {
            nextButton.addActionListener(actionListener);
        }

        if (cancelButton != null)
        {
            cancelButton.addActionListener(actionListener);
        }
    }

    /**
     * Method manages buttons' visibility
     *
     * @param enabled
     */
    protected void setButtonsEnabled(boolean enabled)
    {
        if (backButton != null)
        {
            backButton.setEnabled(enabled);
        }

        if (nextButton != null)
        {
            nextButton.setEnabled(enabled);
        }
    }


    /**
     * Method  is called by framework to define wizard position
     *
     * @param position page position in the list
     */
    protected void setPosition(Position position)
    {
        switch (position)
        {
            case first:
                if (backButton != null)
                {
                    backButton.setEnabled(false);
                    backButton.setVisible(false);
                }
                if (nextButton != null)
                {
                    nextButton.setEnabled(true);
                }
                break;
            case middle:
                if (backButton != null)
                {
                    backButton.setVisible(true);
                    backButton.setEnabled(true);
                }
                if (nextButton != null)
                {
                    nextButton.setEnabled(true);
                }
                break;
            case last:
                if (backButton != null)
                {
                    backButton.setVisible(true);
                    backButton.setEnabled(true);
                }
                if (nextButton != null)
                {
                    nextButton.setEnabled(true);
                    nextButton.setText(TITLE_FINISH);
                    nextButton.repaint();
                }
                break;
            default:
                if (backButton != null)
                {
                    backButton.setEnabled(false);
                }
                if (nextButton != null)
                {
                    nextButton.setEnabled(false);
                }
        }
    }
}
