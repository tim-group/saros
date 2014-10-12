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

package de.fu_berlin.inf.dpp.intellij.project.fs;

import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

public final class IgnorePattern {

    private final String pattern;

    public IgnorePattern(String pattern) {
        this.pattern = pattern;
    }

    public static IgnorePattern parse(InputStream in) {
        return null;
    }

    public MatchResult isIgnored(String path, boolean isDirectory) {
        String patternLeadingSlashRemoved = removeLeadingSlash(pattern);
        String pathLeadingSlashRemoved = removeLeadingSlash(path);


        PathMatcher pathMatcher = FileSystems.getDefault()
            .getPathMatcher("glob:" + patternLeadingSlashRemoved);

        if (pathMatcher.matches(Paths.get(pathLeadingSlashRemoved))) {
            return MatchResult.IGNORED;
        } else {
            return MatchResult.NOT_IGNORED;
        }

    }

    private String removeLeadingSlash(String string) {
        return string.startsWith("/") ? string.substring(1) : string;
    }

}
