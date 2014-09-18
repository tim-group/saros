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

import de.fu_berlin.inf.dpp.intellij.ui.wizards.AbstractWizardPage;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.FlowLayout;

/**
 * Standard information page panel. Creates information page with arbitrary number of paragraphs.
 *
 */

public class InfoPage extends AbstractWizardPage
{
    private String nextButtonTitle = "Accept";
    private JPanel infoPanel;

    public InfoPage(String id)
    {
        super(id);
        create();
    }

    protected void create()
    {
        this.infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        add(infoPanel);
    }

    /**
     * Adds text paragraph
     *
     * @param text
     */
    public void addText(String text)
    {
        JTextArea textItem = new JTextArea(text);
        textItem.setLineWrap(true);
        textItem.setWrapStyleWord(true);
        textItem.setEditable(false);
        textItem.setBackground(infoPanel.getBackground());
        textItem.setSize(560,60);

        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        itemPanel.add(textItem);

        infoPanel.add(itemPanel);
    }

    /**
     *
     * @param nextButtonTitle
     */
    public void setNextButtonTitle(String nextButtonTitle)
    {
        this.nextButtonTitle = nextButtonTitle;
    }

    @Override
    public String getNextButtonTitle()
    {
        return nextButtonTitle;
    }
}
