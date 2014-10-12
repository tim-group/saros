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
import de.fu_berlin.inf.dpp.intellij.project.fs.MatchResult;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;

import static de.fu_berlin.inf.dpp.intellij.project.fs.MatchResult.IGNORED;
import static de.fu_berlin.inf.dpp.intellij.project.fs.MatchResult.NOT_IGNORED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class IgnorePatternTest {

    @Test public void matchesExactFileName() {
        IgnorePattern pattern = new IgnorePattern("some-file.txt");
        assertThat(pattern.isIgnored("some-file.txt", false), is(IGNORED));
        assertThat(pattern.isIgnored("some-other-file.txt", false), is(NOT_IGNORED));
    }

    @Test public void matchesDirectory() {
        IgnorePattern pattern = new IgnorePattern("some-directory");
        assertThat(pattern.isIgnored("some-directory", true), is(IGNORED));
        assertThat(pattern.isIgnored("some-directory", false), is(IGNORED));
        assertThat(pattern.isIgnored("some-directory/", true), is(IGNORED));
    }

    @Test public void leadingSlashMatchesAgainstRelativeFiles() {
        IgnorePattern pattern = new IgnorePattern("/some-file.txt");
        assertThat(pattern.isIgnored("some-file.txt", false), is(IGNORED));
        assertThat(pattern.isIgnored("/some-file.txt", false), is(IGNORED));
        assertThat(pattern.isIgnored("/some-dir/some-file.txt", false), is(NOT_IGNORED));
    }

    @Test public void allowsEscaping() {
        IgnorePattern pattern = new IgnorePattern("\\#some\\*\\-\\!\\[file\\]\\?.txt");
        assertThat(pattern.isIgnored("#some*-![file]?.txt", false), is(IGNORED));
        assertThat(pattern.isIgnored("some-file.txt", false), is(NOT_IGNORED));
    }

    @Test public void allowsWildcardWithinFilename() {
        IgnorePattern pattern = new IgnorePattern("some-*.txt");
        assertThat(pattern.isIgnored("some-matching.txt", false), is(IGNORED));
        assertThat(pattern.isIgnored("some-other-matching.txt", false), is(IGNORED));
        assertThat(pattern.isIgnored("not-matching-file.txt", false), is(NOT_IGNORED));
        assertThat(pattern.isIgnored("some-thing-else.png", false), is(NOT_IGNORED));
        assertThat(pattern.isIgnored("some-other-directory/not-matching-file.txt", false), is(NOT_IGNORED));
    }

    @Test public void singleWildcardsDontSpanDirectories() {
        IgnorePattern pattern = new IgnorePattern("directory/*.txt");
        assertThat(pattern.isIgnored("directory/some-matching.txt", false), is(IGNORED));
        assertThat(pattern.isIgnored("directory/some-other-matching.txt", false), is(IGNORED));
        assertThat(pattern.isIgnored("directory/other-directory/matching-file.txt", false), is(NOT_IGNORED));
    }

    @Test public void allowsDoubleWildcardToRepresentNestedDirectories() {
        IgnorePattern pattern = new IgnorePattern("directory/**.txt");
        assertThat(pattern.isIgnored("directory/some-matching.txt", false), is(IGNORED));
        assertThat(pattern.isIgnored("directory/ignored/some-matching.txt", false), is(IGNORED));
        assertThat(pattern.isIgnored("directory/ignored/next/some-other-matching.txt", false), is(IGNORED));
        assertThat(pattern.isIgnored("not-matching-file.txt", false), is(NOT_IGNORED));
        assertThat(pattern.isIgnored("some-other-directory/not-matching-file.txt", false), is(NOT_IGNORED));
    }

    @Test public void allowsDoubleWildcardToRepresentDirectoryContents() {
        IgnorePattern pattern = new IgnorePattern("directory/**");
        assertThat(pattern.isIgnored("directory/some-matching.txt", false), is(IGNORED));
        assertThat(pattern.isIgnored("directory/ignored/some-matching.txt", false), is(IGNORED));
        assertThat(pattern.isIgnored("directory/ignored/next/some-other-matching.txt", false), is(IGNORED));
        assertThat(pattern.isIgnored("directory", true), is(NOT_IGNORED));
    }

    @Test public void allowsDoubleWildcardToRepresentParentDirectories() {
        IgnorePattern pattern = new IgnorePattern("**/some-matching.txt");
        assertThat(pattern.isIgnored("directory/some-matching.txt", false), is(IGNORED));
        assertThat(pattern.isIgnored("directory/ignored/some-matching.txt", false), is(IGNORED));
        assertThat(pattern.isIgnored("directory/ignored/next/some-matching.txt", false), is(IGNORED));
    }



    /**
     * Things to test:
     *
     * Git
     *  x escaping (hash, space, exclamation mark)
     *  - pattern negation and priority with ! (requires ordering of ignore patterns)
     *  x matching directories specified with trailing slash
     *  - matches relative to gitignore file
     *  x wildcards don't match /
     *  x leading slash with wildcard matches only sibling files
     *  x leading ** match parent directories
     *  x trailing ** match all files inside directory
     *  x ** inside slashes matches zero or more directories
     *
     * fnmatch(3) with FNM_PATHNAME
     *  - named character classes like [:lower:]
     *  - collating symbols
     *  x matching square brackets
     *  x matching dash
     *  - groups
     *  - ranges
     *  - pattern negation
     *
     *  http://git-scm.com/docs/gitignore
     */
}
