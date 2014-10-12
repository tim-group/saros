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

import com.google.common.io.Files;
import com.intellij.openapi.project.Project;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IVcsIgnore;
import de.fu_berlin.inf.dpp.intellij.project.fs.GitIgnore;
import de.fu_berlin.inf.dpp.intellij.project.fs.ProjectImp;
import org.apache.commons.codec.Charsets;
import org.easymock.EasyMock;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static de.fu_berline.inf.dpp.intellij.project.fs.GitIgnoreTest.VcsIgnoredMatcher.ignoredBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class GitIgnoreTest {

    @Rule public TemporaryFolder folder = new TemporaryFolder();

    public static final String TEST_PROJECT_NAME = "Test-project";

    @Test public void honoursGitIgnoreConfig() throws Exception {
        ensureIgnoredFilesExist();
        Project project = getMockIdeaProject();
        IVcsIgnore gitIgnores = new GitIgnore();

        VcsIgnoredMatcher ignored = ignoredBy(gitIgnores,
            new ProjectImp(project, TEST_PROJECT_NAME, gitIgnores));

        assertExpectedIgnores(ignored);
    }

    @Test public void returnsFalseWhenNoGitIgnoreFileExistsAnywhere() throws Exception {
        folder.create();
        createTestProject();
        Project project = getMockIdeaProject();
        folder.newFile(TEST_PROJECT_NAME + "/some-file.txt");

        IVcsIgnore gitIgnores = new GitIgnore();

        assertThat(TEST_PROJECT_NAME + "/some-file.txt", is(not(
            ignoredBy(gitIgnores,
                new ProjectImp(project, "Test-Project", gitIgnores)))));
    }

    @Test public void ignoresTheDotGitDirectory() throws Exception {
        folder.create();
        createTestProject();
        Project project = getMockIdeaProject();

        IVcsIgnore gitIgnores = new GitIgnore();

        VcsIgnoredMatcher ignoredBy = ignoredBy(gitIgnores,
            new ProjectImp(project, TEST_PROJECT_NAME, gitIgnores));

        assertThat(".git/", is(ignoredBy));
        assertThat(".git/config", is(ignoredBy));
    }

    private Project getMockIdeaProject() {
        Project project = EasyMock.createNiceMock(Project.class);
        EasyMock.expect(project.getBasePath())
            .andReturn(folder.getRoot().getAbsolutePath());
        EasyMock.replay(project);
        return project;
    }

    private void assertExpectedIgnores(Matcher<String> ignored) {
        assertThat("ignored.txt", is(ignored));
        assertThat("not-ignored.txt", is(not(ignored)));

        assertThat("ignored-directory", is(ignored));
        assertThat("ignored-directory/ignored-file-in-ignored-dir.txt",
            is(ignored));
        assertThat("ignored-directory/not-ignored-file-in-ignored-dir.txt",
            is(ignored));
        assertThat("ignored-with-wildcard.txt", is(ignored));
        assertThat("not-ignored-directory/ignored-in-not-ignored-dir.txt",
            is(ignored));
        assertThat("not-ignored-directory/not-ignored-in-not-ignored-dir.txt",
            is(not(ignored)));
    }

    private void ensureIgnoredFilesExist() throws Exception {
        folder.create();
        File testProjectDir = createTestProject();

        initGitRepoIn(testProjectDir);

        mkdirIn(testProjectDir, "ignored-directory");
        mkdirIn(testProjectDir, "not-ignored-directory");
        mkFileIn(testProjectDir, "ignored.txt");
        mkFileIn(testProjectDir, "not-ignored.txt");
        mkFileIn(testProjectDir, "ignored-with-wildcard.txt");
        mkFileIn(testProjectDir,
            "ignored-directory/ignored-file-in-ignored-dir.txt");
        mkFileIn(testProjectDir,
            "ignored-directory/not-ignored-file-in-ignored-dir.txt");
        mkFileIn(testProjectDir,
            "not-ignored-directory/ignored-in-not-ignored-dir.txt");

        writeGitignoreIn(testProjectDir,
            "ignored-directory/\n" +
            "ignored-directory/ignored-file-in-ignored-dir.txt\n" +
            "ignored.txt\n" +
            "ignored-*.txt\n");

        writeGitignoreIn(testProjectDir, "ignored-in-not-ignored-dir.txt\n");
    }

    private File createTestProject() {
        File testProjectDir = new File(folder.getRoot(), TEST_PROJECT_NAME);
        testProjectDir.mkdir();
        return testProjectDir;
    }

    private void writeGitignoreIn(File projectDir, String gitignoreContent)
        throws IOException {
        Files.append(gitignoreContent,
            new File(projectDir, GitIgnore.GITIGNORE_FILENAME), Charsets.UTF_8);
    }

    private void initGitRepoIn(File projectDir) throws IOException {
        new File(projectDir.getAbsolutePath(), ".git").mkdirs();
    }

    private void mkFileIn(File projectDir, String file) throws IOException {
        assertTrue(new File(projectDir, file).createNewFile());
    }

    private void mkdirIn(File projectDir, String directory) {
        assertTrue(new File(projectDir, directory).mkdir());
    }

    public static class VcsIgnoredMatcher
        extends TypeSafeDiagnosingMatcher<String> {

        private final IVcsIgnore vcsIgnore;
        private final IProject project;

        public VcsIgnoredMatcher(IVcsIgnore vcsIgnore, IProject project) {
            this.vcsIgnore = vcsIgnore;
            this.project = project;
        }

        public static VcsIgnoredMatcher ignoredBy(IVcsIgnore vcsIgnore,
            IProject project) {
            return new VcsIgnoredMatcher(vcsIgnore, project);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a file that was ignored");
        }

        @Override
        protected boolean matchesSafely(String item,
            Description mismatchDescription) {
            File file = new File(project.getFullPath().toPortableString());
            if (!file.exists()) {
                mismatchDescription.appendValue(item)
                    .appendText("did not exist as a file or directory");
                return false;
            }

            IResource resource = new File(project.getFullPath().toFile(), item).isDirectory() ? project.getFolder(item) : project.getFile(item);
            boolean isIgnored = vcsIgnore.isIgnored(resource);

            if (!isIgnored) {
                mismatchDescription.appendValue(item)
                    .appendText(" was not ignored");
            }

            return isIgnored;
        }

    }
}
