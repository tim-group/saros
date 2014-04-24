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

package de.fu_berlin.inf.dpp.intellij.editor.mock.ui;


import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import de.fu_berlin.inf.dpp.core.exceptions.CoreException;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.Document;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.IAnnotationModel;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.IDocument;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-16
 * Time: 11:08
 */

public class DocumentProvider implements IDocumentProvider
{


    @Override
    public IDocument getDocument(IEditorInput input)
    {
        //todo
        System.out.println("DocumentProvider.getDocument //todo Name="+input.getFile());

        VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(input.getFile().toFile());
        System.out.println("DocumentProvider.getDocument>>>"+vf);

        PsiFile pf = PsiManager.getInstance(Saros.instance().getProject()).findFile(vf);

        return new Document( pf.getViewProvider().getDocument());
    }

    @Override
    public void addElementStateListener(IElementStateListener listener)
    {
        System.out.println("DocumentProvider.addElementStateListener //todo");
    }

    @Override
    public void removeElementStateListener(IElementStateListener listener)
    {
        System.out.println("DocumentProvider.removeElementStateListener //todo");
    }

    @Override
    public IAnnotationModel getAnnotationModel(IEditorInput input)
    {
        System.out.println("DocumentProvider.getAnnotationModel //todo");
        return null;
    }

    @Override
    public void disconnect(IEditorInput input)
    {
        System.out.println("DocumentProvider.disconnect //todo");
        VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(input.getFile().toFile());
        System.out.println("DocumentProvider.disconnect>>>"+vf+" exist="+vf.exists());
        FileEditorManager.getInstance(Saros.instance().getProject()).closeFile(vf);
    }

    @Override
    public void connect(IEditorInput input) throws CoreException
    {
        System.out.println("DocumentProvider.connect //todo");

        VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(input.getFile().toFile());
        System.out.println("DocumentProvider.connect>>>"+vf+" exist="+vf.exists());

        FileEditorManager.getInstance(Saros.instance().getProject()).openFile(vf, true);

    }

    @Override
    public boolean canSaveDocument(IEditorInput input)
    {
        return false;
    }

    @Override
    public void saveDocument(IProgressMonitor monitor, IEditorInput input, IDocument doc, boolean b) throws CoreException
    {
        System.out.println("DocumentProvider.saveDocument //todo");
    }
}
