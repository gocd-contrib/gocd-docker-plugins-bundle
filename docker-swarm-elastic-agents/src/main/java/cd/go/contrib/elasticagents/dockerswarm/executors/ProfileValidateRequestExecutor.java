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

package cd.go.contrib.elasticagents.dockerswarm.executors;

import cd.go.contrib.elasticagents.dockerswarm.RequestExecutor;
import cd.go.contrib.elasticagents.dockerswarm.requests.ProfileValidateRequest;
import cd.go.plugin.base.GsonTransformer;
import cd.go.plugin.base.validation.ValidationResult;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfileValidateRequestExecutor implements RequestExecutor {
    private final ProfileValidateRequest request;

    public ProfileValidateRequestExecutor(ProfileValidateRequest request) {
        this.request = request;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        final List<String> knownFields = new ArrayList<>();
        final ValidationResult validationResult = new ValidationResult();

        for (Metadata field : GetProfileMetadataExecutor.FIELDS) {
            knownFields.add(field.getKey());
            field.validate(request.getProperties().get(field.getKey()), validationResult);
        }

        final Set<String> set = new HashSet<>(request.getProperties().keySet());
        set.removeAll(knownFields);

        if (!set.isEmpty()) {
            for (String key : set) {
                validationResult.add(key, "Is an unknown property.");
            }
        }

        return DefaultGoPluginApiResponse.success(GsonTransformer.toJson(validationResult));
    }
}
