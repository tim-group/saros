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
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.project.fs.GitIgnore;
import de.fu_berlin.inf.dpp.intellij.project.fs.ProjectImp;
import de.fu_berlin.inf.dpp.intellij.project.fs.VcsIgnore;
import org.apache.commons.codec.Charsets;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
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

    @Test public void honoursGitIgnoreConfig() throws Exception {
        ensureIgnoredFilesExist();

        VcsIgnore gitIgnores = new GitIgnore();
        VcsIgnoredMatcher ignored = ignoredBy(gitIgnores,
            new ProjectImp(null, "Test-Project", folder.getRoot(), gitIgnores));

        assertExpectedIgnores(ignored);
    }

    @Test public void returnsFalseWhenNoGitIgnoreFileExistsAnywhere() throws Exception {
        folder.create();
        File projectFolder = folder.newFolder("not-a-git-project");
        folder.newFile("not-a-git-project/some-file.txt");

        VcsIgnore gitIgnores = new GitIgnore();

        assertThat("not-a-git-project/some-file.txt", is(not(ignoredBy(gitIgnores, new ProjectImp(null, "Test-Project", projectFolder, gitIgnores)))));
    }

    private void assertExpectedIgnores(Matcher<String> ignored) {
        assertThat("ignored.txt", is(ignored));
        assertThat("not-ignored.txt", is(not(ignored)));

        assertThat("ignored-directory", is(ignored));
        assertThat("ignored-directory/ignored-file-in-ignored-dir.txt", is(ignored));
        assertThat("ignored-directory/not-ignored-file-in-ignored-dir.txt", is(ignored));
        assertThat("ignored-with-wildcard.txt", is(ignored));
        assertThat("not-ignored-directory/ignored-in-not-ignored-dir.txt", is(ignored));
        assertThat("not-ignored-directory/not-ignored-in-not-ignored-dir.txt", is(not(ignored)));
    }

    private void ensureIgnoredFilesExist() throws Exception {
        folder.create();
        File rootDir = folder.getRoot();

        initGitRepoIn(rootDir);

        mkdirIn(rootDir, "ignored-directory");
        mkdirIn(rootDir, "not-ignored-directory");
        mkFileIn(rootDir, "ignored.txt");
        mkFileIn(rootDir, "not-ignored.txt");
        mkFileIn(rootDir, "ignored-with-wildcard.txt");
        mkFileIn(rootDir, "ignored-directory/ignored-file-in-ignored-dir.txt");
        mkFileIn(rootDir, "ignored-directory/not-ignored-file-in-ignored-dir.txt");
        mkFileIn(rootDir,
            "not-ignored-directory/ignored-in-not-ignored-dir.txt");

        writeGitignoreIn(rootDir,
            "ignored-directory/\n" +
            "ignored-directory/ignored-file-in-ignored-dir.txt\n" +
            "ignored.txt\n" +
            "ignored-*.txt\n");

        writeGitignoreIn(rootDir, "ignored-in-not-ignored-dir.txt\n");
    }

    private void writeGitignoreIn(File rootDir, String gitignoreContent)
        throws IOException {
        Files.append(gitignoreContent,
            new File(rootDir, Constants.GITIGNORE_FILENAME), Charsets.UTF_8);
    }

    private void initGitRepoIn(File rootDir)
        throws GitAPIException, IOException {
        Git.init().setDirectory(rootDir).setBare(false).call();
        Repository repository = FileRepositoryBuilder
            .create(new File(rootDir.getAbsolutePath(), ".git"));
        repository.close();
    }

    private void mkFileIn(File rootDir, String file) throws IOException {
        assertTrue(new File(rootDir, file).createNewFile());
    }

    private void mkdirIn(File rootDir, String directory) {
        assertTrue(new File(rootDir, directory).mkdir());
    }

    public static class VcsIgnoredMatcher
        extends TypeSafeDiagnosingMatcher<String> {

        private final VcsIgnore vcsIgnore;
        private final IProject project;

        public VcsIgnoredMatcher(VcsIgnore vcsIgnore, IProject project) {
            this.vcsIgnore = vcsIgnore;
            this.project = project;
        }

        public static VcsIgnoredMatcher ignoredBy(VcsIgnore vcsIgnore,
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
