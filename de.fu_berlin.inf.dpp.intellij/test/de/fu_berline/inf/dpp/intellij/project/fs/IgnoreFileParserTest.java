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

import de.fu_berlin.inf.dpp.intellij.project.fs.IgnorePattern;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class IgnoreFileParserTest {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Test public void doesNotCreatePatternsFromEmptyLines() throws IOException {
        InputStream lines = fromString("\n\n\n");
        List<IgnorePattern> patterns = IgnoreFileParser.from(lines, UTF_8);

        assertThat(patterns, hasSize(0));
    }

    @Test public void doesNotCreatePatternsFromCommentedLines()
        throws IOException {
        InputStream lines = fromString("\n#commented\n");
        List<IgnorePattern> patterns = IgnoreFileParser.from(lines, UTF_8);

        assertThat(patterns, hasSize(0));
    }

    @Test public void createsOnePatternPerValidLine() throws IOException {
        InputStream lines = fromString(
            "something.txt\n" +
            "*.class\n" +
            "[abc]*.txt\n"
        );
        List<IgnorePattern> patterns = IgnoreFileParser.from(lines, UTF_8);

        assertThat(patterns, hasSize(3));
    }

    private InputStream fromString(String content) {
        return new ByteArrayInputStream(content.getBytes(UTF_8));
    }
}
