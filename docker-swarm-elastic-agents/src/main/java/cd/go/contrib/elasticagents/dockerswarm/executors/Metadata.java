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

import cd.go.plugin.base.validation.ValidationResult;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;

public class Metadata {

    @Expose
    @SerializedName("key")
    private String key;

    @Expose
    @SerializedName("metadata")
    private ProfileMetadata metadata;

    public Metadata(String key, boolean required, boolean secure) {
        this(key, new ProfileMetadata(required, secure));
    }

    public Metadata(String key) {
        this(key, new ProfileMetadata(false, false));
    }

    public Metadata(String key, ProfileMetadata metadata) {
        this.key = key;
        this.metadata = metadata;
    }

    public void validate(String input, ValidationResult validationResult) {
        String errorMessage = doValidate(input);
        if (StringUtils.isNotBlank(errorMessage)) {
            validationResult.add(key, errorMessage);
        }
    }

    protected String doValidate(String input) {
        if (isRequired()) {
            if (StringUtils.isBlank(input)) {
                return this.key + " must not be blank.";
            }
        }
        return null;
    }


    public String getKey() {
        return key;
    }

    public boolean isRequired() {
        return metadata.required;
    }

    public static class ProfileMetadata {
        @Expose
        @SerializedName("required")
        private Boolean required;

        @Expose
        @SerializedName("secure")
        private Boolean secure;

        public ProfileMetadata(boolean required, boolean secure) {
            this.required = required;
            this.secure = secure;
        }
    }
}
