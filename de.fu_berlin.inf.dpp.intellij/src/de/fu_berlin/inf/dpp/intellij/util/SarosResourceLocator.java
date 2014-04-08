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

package de.fu_berlin.inf.dpp.intellij.util;

import de.fu_berlin.inf.dpp.intellij.SarosToolWindowFactory;

import java.net.URL;

/**
 * Image locator helps to find UI images
 * <p/>
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.18
 * Time: 14.03
 */
public class SarosResourceLocator
{
    private static final Class loader = SarosToolWindowFactory.class;

    private static String buttonImageFolder = "/images/btn/";
    private static String treeImageFolder = "/images/tree/";


    private static URL getImageUrl(String folder, String buttonImageName, String ext)
    {
        String imageFile = folder + buttonImageName + "." + ext;
        URL imageURL = loader.getResource(imageFile);

        return imageURL;
    }

    /**
     * @param buttonImageName
     * @param ext
     * @return
     */
    public static URL getButtonImageUrl(String buttonImageName, String ext)
    {
        return getImageUrl(buttonImageFolder, buttonImageName, ext);
    }

    /**
     * @param treeImageName
     * @param ext
     * @return
     */
    public static URL getTreeImageUrl(String treeImageName, String ext)
    {
        return getImageUrl(treeImageFolder, treeImageName, ext);
    }


    /**
     * @param buttonName
     * @return
     */
    public static URL getButtonImageUrl(String buttonName)
    {
        return getButtonImageUrl(buttonName, "png");
    }

    /**
     * @param treeImageName
     * @return
     */
    public static URL getTreeImageUrl(String treeImageName)
    {
        return getTreeImageUrl(treeImageName, "png");
    }


}
