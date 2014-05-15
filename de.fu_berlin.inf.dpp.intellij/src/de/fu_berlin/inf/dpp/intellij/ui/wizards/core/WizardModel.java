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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default wizard model. Class keeps information about
 * wizard position, acts as container for
 *
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-05-14
 * Time: 08:27
 */

public class WizardModel
{
    private Map<Object, AbstractWizardPage> stepMap = new HashMap<Object, AbstractWizardPage>();
    private List<AbstractWizardPage> stepList = new ArrayList<AbstractWizardPage>();

    private AbstractWizardPage backPanel;
    private AbstractWizardPage currentPanel;
    private AbstractWizardPage nextPanel;

    /**
     * Method called internally by framework to add page to container
     *
     * @param id
     * @param panel
     */
    protected void registerPanel(Object id, AbstractWizardPage panel)
    {
        stepMap.put(id, panel);
        stepList.add(panel);
    }

    /**
     * Return panel
     *
     * @return  AbstractWizardPage
     */
    public AbstractWizardPage getBackPanel()
    {
        return backPanel;
    }

    /**
     * Current panel
     *
     * @return  AbstractWizardPage
     */
    public AbstractWizardPage getCurrentPanel()
    {
        return currentPanel;
    }

    /**
     * Next panel
     *
     * @return AbstractWizardPage
     */
    public AbstractWizardPage getNextPanel()
    {
        return nextPanel;
    }

    /**
     * Sets current page by ID
     *
     * @param id page identifier
     */
    public void setCurrentPanelId(Object id)
    {
        setCurrentPanel(stepMap.get(id));
    }

    /**
     * Sets current page by index.
     *
     * @param index page index in registry
     */
    public void setCurrentPanelIndex(int index)
    {
        setCurrentPanel(stepList.get(index));
    }

    /**
     * Called internally by framework to set current panel
     *
     * @param panel  AbstractWizardPage
     */
    protected void setCurrentPanel(AbstractWizardPage panel)
    {
        this.currentPanel = panel;

        if (currentPanel == null)
        {
            return;
        }

        int index = stepList.indexOf(panel);
        if (index > 0)
        {
            this.backPanel = stepList.get(index - 1);
        }
        else
        {
            this.backPanel = null;
        }

        if (index < stepList.size() - 1)
        {
            this.nextPanel = stepList.get(index + 1);
        }
        else
        {
            this.nextPanel = null;
        }
    }

    /**
     * Count of registered pages
     *
     * @return
     */
    public int getSize()
    {
        return stepList.size();
    }
}
