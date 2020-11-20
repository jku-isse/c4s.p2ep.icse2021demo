package passiveprocessengine.persistance.json;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import passiveprocessengine.definition.ArtifactType;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class TaskDefinitionIntermediary {
    private @Getter
    String id;
    private @Getter boolean isNoOp;
    private @Getter String subWfdId;
    private @Getter
    Map<String, ArtifactType> expectedInput = new HashMap<>();
    private @Getter Map<String,ArtifactType> expectedOutput = new HashMap<>();
}