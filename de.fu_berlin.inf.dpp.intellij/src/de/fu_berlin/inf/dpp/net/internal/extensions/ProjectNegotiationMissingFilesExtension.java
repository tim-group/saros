package de.fu_berlin.inf.dpp.net.internal.extensions;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.fu_berlin.inf.dpp.communication.extensions.ProjectNegotiationExtension;
import de.fu_berlin.inf.dpp.invitation.FileList;

//TODO Move to core (package communication.extensions)

@XStreamAlias(/* ProjectNegotiationMissingFiles */"PNMF")
public class ProjectNegotiationMissingFilesExtension extends
    ProjectNegotiationExtension {

    public static final Provider PROVIDER = new Provider();

    private final List<FileList> fileLists;

    public ProjectNegotiationMissingFilesExtension(String sessionID,
        String negotiationID, List<FileList> fileLists) {
        super(sessionID, negotiationID);
        this.fileLists = fileLists;
    }

    public List<FileList> getFileLists() {
        return fileLists;
    }

    public static class Provider
        extends
        ProjectNegotiationExtension.Provider<ProjectNegotiationMissingFilesExtension> {

        private Provider() {
            super("pnmf", ProjectNegotiationMissingFilesExtension.class,
                FileList.class);
        }
    }
}
