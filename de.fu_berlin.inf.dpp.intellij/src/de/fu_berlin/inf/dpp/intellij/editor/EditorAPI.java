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

package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiManager;
import com.intellij.util.ui.UIUtil;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.intellij.SarosToolWindowFactory;
import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.intellij.editor.colorstorage.ColorModel;

import java.awt.*;
import java.io.File;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-18
 * Time: 12:20
 */

public class EditorAPI extends EditorAPIBridge
{

    private ProjectManager projectManager;
    private LocalFileSystem localFileSystem;
    private Application application;
    private CommandProcessor commandProcessor;
    private FileDocumentManager fileDocumentManager;
    private EditorFactory editorFactory;
    private ModuleManager moduleManager;

    private Project project;
    private PsiManager psiManager;
    protected FileEditorManager editorFileManager;
    private PsiDocumentManager psiDocumentManager;
    private VirtualFileManager virtualFileManager;

    public EditorAPI()
    {
        Project project = Saros.instance().getProject();
        setProject(project);
    }

    public EditorAPI(Project project)
    {
        setProject(project);
    }

    public VirtualFile toVirtualFile(SPath path)
    {
        return toVirtualFile(path.getFile().toFile());
    }

    public VirtualFile toVirtualFile(File path)
    {
        return localFileSystem.findFileByIoFile(path);
    }

    public boolean isOpen(VirtualFile file)
    {
        return editorFileManager.isFileOpen(file);
    }

    public boolean isOpen(Document doc)
    {
        VirtualFile file = fileDocumentManager.getFile(doc);
        return isOpen(file);
    }

    class EditorContainer
    {
        Editor editor;
    }

    public Editor openEditor(final VirtualFile path)
    {
        // editorFileManager.openFile(path, true);
        // return editorFileManager.getSelectedTextEditor();

        final EditorContainer result = new EditorContainer();

        Runnable action = new Runnable()
        {
            @Override
            public void run()
            {

                application.runReadAction(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        editorFileManager.openFile(path, true);

                        result.editor = editorFileManager.getSelectedTextEditor();
                    }
                });

            }
        };

        UIUtil.invokeAndWaitIfNeeded(action);


        return result.editor;

    }

    public Document createDocument(VirtualFile path)
    {
        return fileDocumentManager.getDocument(path);
    }

   /* public Editor openEditor(Document doc)
    {
        return editorFactory.createEditor(doc);

    }*/

    public void closeEditor(final VirtualFile file)
    {
        // editorFileManager.addFileEditorManagerListener(); //todo

        Runnable action = new Runnable()
        {
            @Override
            public void run()
            {
                editorFileManager.closeFile(file);
            }
        };

        UIUtil.invokeAndWaitIfNeeded(action);
    }

    public Document getDocument(final VirtualFile file)
    {
        return fileDocumentManager.getDocument(file);
    }

    public void closeEditor(Document doc)
    {
        VirtualFile file = fileDocumentManager.getFile(doc);
        closeEditor(file);
    }

    public Editor getActiveEditor()
    {
        return editorFileManager.getSelectedTextEditor();
    }

    public void saveDocument(final Document doc)
    {
        application.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                application.runWriteAction(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        fileDocumentManager.saveDocument(doc);
                    }
                });
            }
        });

    }

    /**
     * @param doc
     */
    public void saveAllDocuments(Document doc)
    {
        application.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                application.runWriteAction(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        fileDocumentManager.saveAllDocuments();
                    }
                });
            }
        });

    }

    /**
     * @param editor
     * @param lineStart
     * @param lineEnd
     */
    public void setViewPort(final Editor editor, final int lineStart, final int lineEnd)
    {

        Runnable action = new Runnable()
        {
            @Override
            public void run()
            {

                VisualPosition posCenter = new VisualPosition((lineStart + lineEnd) / 2, 0);
                editor.getCaretModel().moveToVisualPosition(posCenter);
                editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);

            }
        };

        UIUtil.invokeAndWaitIfNeeded(action);
    }

    public void insertText(final Document doc, final int position, final String text)
    {
        Runnable action = new Runnable()
        {
            @Override
            public void run()
            {
                commandProcessor.executeCommand(project, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        application.runWriteAction(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                doc.insertString(position, text);
                            }
                        });
                    }
                }, "insertText()", commandProcessor.getCurrentCommandGroupId());
            }
        };

        UIUtil.invokeAndWaitIfNeeded(action);
    }

    public RangeHighlighter textMarkAdd(final Editor editor, final int start, final int end, Color color)
    {
        if (color == null || editor == null)
        {
            return null;
        }

        TextAttributes textAttr = new TextAttributes();
        textAttr.setBackgroundColor(color);

        RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(start, end, HighlighterLayer.LAST, textAttr, HighlighterTargetArea.EXACT_RANGE);
        highlighter.setGreedyToLeft(false);
        highlighter.setGreedyToRight(false);
        //highlighter.setLineSeparatorColor(Color.WHITE); //todo

        return highlighter;
    }

    public void textMarkRemove(final Editor editor,RangeHighlighter highlighter)
    {
        if(editor==null || highlighter==null)
        {
            return;
        }

        editor.getMarkupModel().removeHighlighter(highlighter);
    }

    public void deleteText(final Document doc, final int start, final int end)
    {
        Runnable action = new Runnable()
        {
            @Override
            public void run()
            {
                commandProcessor.executeCommand(project, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        application.runWriteAction(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                doc.deleteString(start, end);
                            }
                        });
                    }
                }, "deleteText(" + start + "," + end + ")", commandProcessor.getCurrentCommandGroupId());
            }
        };

        UIUtil.invokeAndWaitIfNeeded(action);
    }

    public void setSelection(final Editor editor, final int start, final int end,ColorModel colorMode)
    {

        Runnable action = new Runnable()
        {
            @Override
            public void run()
            {
                application.runReadAction(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //set selection
                        editor.getSelectionModel().setSelection(start, end);

                        //move scroll
                        int lineStart = editor.getSelectionModel().getSelectionStartPosition().getLine();
                        int lineEnd = editor.getSelectionModel().getSelectionEndPosition().getLine();

                        int colStart = editor.getSelectionModel().getSelectionStartPosition().getColumn();
                        int colEnd = editor.getSelectionModel().getSelectionEndPosition().getColumn();

                        VisualPosition posCenter = new VisualPosition((lineStart + lineEnd) / 2, (colStart + colEnd) / 2);
                        editor.getCaretModel().moveToVisualPosition(posCenter);
                        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);

                        //move cursor
                        editor.getCaretModel().moveToOffset(start, true);
                    }
                });
            }
        };

        UIUtil.invokeAndWaitIfNeeded(action);

    }
    public void _setSelection(final Editor editor, final int start, final int end)
    {
        Runnable action = new Runnable()
        {
            @Override
            public void run()
            {
                application.runReadAction(new Runnable()
                {
                    @Override
                    public void run()
                    {

                        //set selection
                        editor.getSelectionModel().setSelection(start, end);

                        //move scroll
                        int lineStart = editor.getSelectionModel().getSelectionStartPosition().getLine();
                        int lineEnd = editor.getSelectionModel().getSelectionEndPosition().getLine();

                        int colStart = editor.getSelectionModel().getSelectionStartPosition().getColumn();
                        int colEnd = editor.getSelectionModel().getSelectionEndPosition().getColumn();

                        VisualPosition posCenter = new VisualPosition((lineStart + lineEnd) / 2, (colStart + colEnd) / 2);
                        editor.getCaretModel().moveToVisualPosition(posCenter);
                        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);

                        //move cursor
                        editor.getCaretModel().moveToOffset(start, true);
                    }
                });
            }
        };

        UIUtil.invokeAndWaitIfNeeded(action);

    }

    public ModuleManager getModuleManager()
    {
        return moduleManager;
    }

    public boolean isInitialized()
    {
        return project != null;
    }

    public void setProject(Project project)
    {
        if (project != null)
        {
            this.project = project;
            this.editorFileManager = FileEditorManager.getInstance(project);
            this.psiManager = PsiManager.getInstance(project);
            this.psiDocumentManager = PsiDocumentManager.getInstance(project);
            this.moduleManager = ModuleManager.getInstance(project);

            this.virtualFileManager = VirtualFileManager.getInstance();
            this.projectManager = ProjectManager.getInstance();
            this.localFileSystem = LocalFileSystem.getInstance();
            this.application = ApplicationManager.getApplication();
            this.commandProcessor = CommandProcessor.getInstance();
            this.fileDocumentManager = FileDocumentManager.getInstance();
            this.editorFactory = EditorFactory.getInstance();
        }
    }


}
