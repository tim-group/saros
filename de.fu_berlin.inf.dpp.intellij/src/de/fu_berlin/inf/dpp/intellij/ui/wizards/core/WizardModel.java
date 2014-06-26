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
 */
public class WizardModel
{
    private Map<Object, AbstractWizardPage> pageMap = new HashMap<Object, AbstractWizardPage>();
    private List<AbstractWizardPage> pageList = new ArrayList<AbstractWizardPage>();

    private AbstractWizardPage backPage;
    private AbstractWizardPage currentPage;
    private AbstractWizardPage nextPage;

    /**
     * Method called internally by framework to add page to container
     *
     * @param id
     * @param panel
     */
    protected void registerPanel(Object id, AbstractWizardPage panel)
    {
        pageMap.put(id, panel);
        pageList.add(panel);
    }

    /**
     * Return panel
     *
     * @return AbstractWizardPage
     */
    public AbstractWizardPage getBackPage()
    {
        return backPage;
    }

    /**
     * Current panel
     *
     * @return AbstractWizardPage
     */
    public AbstractWizardPage getCurrentPage()
    {
        return currentPage;
    }

    /**
     * Next panel
     *
     * @return AbstractWizardPage
     */
    public AbstractWizardPage getNextPage()
    {
        return nextPage;
    }

    /**
     * Finds page by ID
     *
     * @param id identifier
     * @return AbstractWizardPage
     */
    public AbstractWizardPage getPageById(Object id)
    {
        return pageMap.get(id);
    }

    /**
     * Finds page by index
     *
     * @param index
     * @return AbstractWizardPage
     */
    public AbstractWizardPage getPageByIndex(int index)
    {
        return pageList.get(index);
    }

    /**
     * Next page position
     *
     * @param page
     */
    public void setNextPage(AbstractWizardPage page)
    {
        this.nextPage = page;
    }

    /**
     * Back page
     *
     * @param backPage
     */
    public void setBackPage(AbstractWizardPage backPage)
    {
        this.backPage = backPage;
    }

    /**
     * @param index
     */
    protected void setCurrentPositionIndex(int index)
    {
        setCurrentPagePosition(getPageByIndex(index));
    }

    /**
     * Called internally by framework to set current page
     *
     * @param page AbstractWizardPage
     */
    protected void setCurrentPagePosition(AbstractWizardPage page)
    {
        this.currentPage = page;

        if (currentPage == null)
        {
            return;
        }

        int index = pageList.indexOf(page);
        if (index > 0)
        {
            this.backPage = pageList.get(index - 1);
        }
        else
        {
            this.backPage = null;
        }

        if (index < pageList.size() - 1)
        {
            this.nextPage = pageList.get(index + 1);
        }
        else
        {
            this.nextPage = null;
        }
    }

    /**
     * Count of registered pages
     *
     * @return
     */
    public int getSize()
    {
        return pageList.size();
    }
}
