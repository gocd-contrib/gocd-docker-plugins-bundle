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

package cd.go.contrib.elasticagents.dockerswarm.metadata;

import cd.go.contrib.elasticagents.dockerswarm.Hosts;
import cd.go.contrib.elasticagents.dockerswarm.executors.Metadata;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class HostMetadata extends Metadata {
    public HostMetadata(String key, boolean required, boolean secure) {
        super(key, required, secure);
    }

    @Override
    protected String doValidate(String input) {
        final List<String> errors = new ArrayList<>();
        final String validate = super.doValidate(input);

        if (isNotBlank(validate)) {
            errors.add(validate);
        }

        errors.addAll(new Hosts().validate(input));

        if (errors.isEmpty()) {
            return null;
        } else {
            return StringUtils.join(errors, ". ");
        }
    }
}
