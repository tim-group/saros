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

package de.fu_berlin.inf.dpp.intellij.ui.widgets.progress;

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.ISubMonitor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Creates progress monitor panel
 * <p/>
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-05-12
 * Time: 14:45
 */

public class SarosProgressMonitor extends SarosProgressBar implements IProgressMonitor
{

    public static final String TITLE = "Progress monitor";
    public static final String BUTTON_CANCEL = "Cancel";

    private Container parent; // = Saros.instance().getMainPanel();
    private JFrame frmMain;
    private JButton btnCancel;


    /**
     * Constructor with default title
     */
    public SarosProgressMonitor()
    {
        this(TITLE);
    }

    /**
     * Constructor with explicit title
     *
     * @param title dialog title
     */
    public SarosProgressMonitor(String title)
    {
        frmMain = new JFrame(title);
        frmMain.setSize(300, 160);
        frmMain.setLocationRelativeTo(parent);

        Container pane = frmMain.getContentPane();
        pane.setLayout(null);

        frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        JLabel infoLabel = new JLabel();
        btnCancel = new JButton(BUTTON_CANCEL);
        btnCancel.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setCanceled(true);
            }
        });

        JProgressBar progressBar = new JProgressBar(MIN_VALUE, MAX_VALUE);

        pane.add(infoLabel);
        pane.add(btnCancel);
        pane.add(progressBar);

        infoLabel.setBounds(10, 15, 200, 15);
        progressBar.setBounds(10, 50, 280, 20);
        btnCancel.setBounds(100, 85, 100, 25);

        frmMain.setResizable(false);
        frmMain.setVisible(true);

        this.frmMain.repaint();

        super.display = new DisplayContainer(progressBar, infoLabel);
    }

    @Override
    public void done()
    {
        super.done();
        frmMain.dispose();
    }

}
