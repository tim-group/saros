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

package de.fu_berlin.inf.dpp.intellij.mock.editor.core.resources;

//created from eclipse lib
public interface IContainer extends de.fu_berlin.inf.dpp.filesystem.IContainer
{

    // Field descriptor #10 I
    public static final int INCLUDE_PHANTOMS = 1;

    // Field descriptor #10 I
    public static final int INCLUDE_TEAM_PRIVATE_MEMBERS = 2;

    // Field descriptor #10 I
    public static final int EXCLUDE_DERIVED = 4;

    // Field descriptor #10 I
    public static final int INCLUDE_HIDDEN = 8;

    // Field descriptor #10 I
    public static final int DO_NOT_CHECK_EXISTENCE = 16;

}