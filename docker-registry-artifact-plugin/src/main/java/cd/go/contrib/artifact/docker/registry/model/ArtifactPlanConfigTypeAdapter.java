/*
 * Copyright 2019 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.contrib.artifact.docker.registry.model;

import com.google.gson.*;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Type;
import java.util.Optional;

public class ArtifactPlanConfigTypeAdapter implements JsonDeserializer<ArtifactPlanConfig>, JsonSerializer<ArtifactPlanConfig> {

    @Override
    public ArtifactPlanConfig deserialize(JsonElement json,
                                          Type typeOfT,
                                          JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        if (isImageTagConfig(jsonObject)) {
            return new ImageTagArtifactPlanConfig(jsonObject.get("Image").getAsString(), parseTag(jsonObject));
        } else if (isBuildFileConfig(jsonObject)) {
            return new BuildFileArtifactPlanConfig(jsonObject.get("BuildFile").getAsString());
        } else {
            throw new JsonParseException("Ambiguous or unknown json. Either `Image` or`BuildFile` property must be specified.");
        }
    }

    private Optional<String> parseTag(JsonObject jsonObject) {
        JsonElement tag = jsonObject.get("Tag");
        if (tag != null && StringUtils.isNotBlank(tag.getAsString())) {
            return Optional.of(tag.getAsString());
        }
        return Optional.empty();
    }

    @Override
    public JsonElement serialize(ArtifactPlanConfig src, Type typeOfSrc, JsonSerializationContext context) {
        if (src instanceof BuildFileArtifactPlanConfig) {
            return context.serialize(src, BuildFileArtifactPlanConfig.class);
        } else if (src instanceof ImageTagArtifactPlanConfig) {
            return context.serialize(src, ImageTagArtifactPlanConfig.class);
        }
        throw new JsonIOException("Unknown type of ArtifactPlanConfig");
    }

    private boolean isBuildFileConfig(JsonObject jsonObject) {
        return containsBuildFileProperty(jsonObject) && !containsImageProperty(jsonObject);
    }

    private boolean isImageTagConfig(JsonObject jsonObject) {
        return containsImageProperty(jsonObject) && !containsBuildFileProperty(jsonObject);
    }

    private boolean containsBuildFileProperty(JsonObject jsonObject) {
        return jsonObject.has("BuildFile") && isPropertyNotBlank(jsonObject, "BuildFile");
    }

    private boolean containsImageProperty(JsonObject jsonObject) {
        return jsonObject.has("Image") && isPropertyNotBlank(jsonObject, "Image");
    }

    private boolean isPropertyNotBlank(JsonObject jsonObject, String property) {
        try {
            JsonElement jsonElement = jsonObject.get(property);
            return StringUtils.isNotBlank(jsonElement.getAsString());
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }
}
