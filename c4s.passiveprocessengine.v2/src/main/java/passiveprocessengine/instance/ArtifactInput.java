package passiveprocessengine.instance;

import org.neo4j.ogm.annotation.RelationshipEntity;
import passiveprocessengine.definition.Artifact;
import passiveprocessengine.definition.ArtifactType;

@RelationshipEntity(type="TASK_IO")
public class ArtifactInput extends ArtifactIO {

    /**
     *
     */
    private static final long serialVersionUID = 1L;


    public ArtifactInput(Artifact artifact, String role) {
        super(artifact, role);
    }

    public ArtifactInput(Artifact artifact) {
        super(artifact);
    }

    public ArtifactInput(ArtifactOutput ao) {
        super(ao.artifact, ao.role, ao.artifactType);
        id = ao.id;
        container = ao.container;
    }

    public ArtifactInput(Artifact artifact, String role, ArtifactType artifactType) {
        super(artifact, role, artifactType);
    }

    @Deprecated
    public ArtifactInput(){
        super();
    }
}
