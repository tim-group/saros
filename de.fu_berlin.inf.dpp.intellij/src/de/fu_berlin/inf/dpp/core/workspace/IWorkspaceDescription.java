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

package de.fu_berlin.inf.dpp.core.workspace;

public abstract interface IWorkspaceDescription
{

    // Method descriptor #6 ()[Ljava/lang/String;
    public abstract java.lang.String[] getBuildOrder();

    // Method descriptor #8 ()J
    public abstract long getFileStateLongevity();

    // Method descriptor #10 ()I
    public abstract int getMaxBuildIterations();

    // Method descriptor #10 ()I
    public abstract int getMaxFileStates();

    // Method descriptor #8 ()J
    public abstract long getMaxFileStateSize();

    // Method descriptor #14 ()Z
    public abstract boolean isApplyFileStatePolicy();

    // Method descriptor #8 ()J
    public abstract long getSnapshotInterval();

    // Method descriptor #14 ()Z
    public abstract boolean isAutoBuilding();

    // Method descriptor #18 (Z)V
    public abstract void setAutoBuilding(boolean arg0);

    // Method descriptor #20 ([Ljava/lang/String;)V
    public abstract void setBuildOrder(java.lang.String[] arg0);

    // Method descriptor #22 (J)V
    public abstract void setFileStateLongevity(long arg0);

    // Method descriptor #24 (I)V
    public abstract void setMaxBuildIterations(int arg0);

    // Method descriptor #24 (I)V
    public abstract void setMaxFileStates(int arg0);

    // Method descriptor #22 (J)V
    public abstract void setMaxFileStateSize(long arg0);

    // Method descriptor #18 (Z)V
    public abstract void setApplyFileStatePolicy(boolean arg0);

    // Method descriptor #22 (J)V
    public abstract void setSnapshotInterval(long arg0);
}
