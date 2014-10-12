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

package de.fu_berline.inf.dpp.intellij.project.fs;

import de.fu_berlin.inf.dpp.intellij.project.fs.GitIgnore;
import de.fu_berlin.inf.dpp.intellij.project.fs.IgnorePattern;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class IgnoreFileParser {
    public static List<IgnorePattern> from(InputStream lines, Charset charset)
        throws IOException {

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(lines, charset));

        List<IgnorePattern> patterns = new ArrayList<IgnorePattern>();
        String patternText;
        while ((patternText = reader.readLine()) != null) {
            if (!patternText.isEmpty() && !patternText.startsWith("#")) {
                patterns.add(new IgnorePattern(patternText));
            }
        }
        return patterns;
    }
}
