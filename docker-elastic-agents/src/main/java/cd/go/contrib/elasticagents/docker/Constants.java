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

package cd.go.contrib.elasticagents.docker;

import com.thoughtworks.go.plugin.api.GoPluginIdentifier;

import java.util.Collections;

public interface Constants {
    String PLUGIN_ID = "cd.go.contrib.elastic-agent.docker";

    // The type of this extension
    String EXTENSION_TYPE = "elastic-agent";

    // The extension point API version that this plugin understands
    String PROCESSOR_API_VERSION = "1.0";
    String EXTENSION_API_VERSION = "5.0";

    // the identifier of this plugin
    GoPluginIdentifier PLUGIN_IDENTIFIER = new GoPluginIdentifier(EXTENSION_TYPE, Collections.singletonList(EXTENSION_API_VERSION));

    // internal use only
    String CREATED_BY_LABEL_KEY = "Elastic-Agent-Created-By";
    String JOB_IDENTIFIER_LABEL_KEY = "Elastic-Agent-Job-Identifier";
    String ENVIRONMENT_LABEL_KEY = "Elastic-Agent-Environment-Name";
    String CONFIGURATION_LABEL_KEY = "Elastic-Agent-Configuration";

}
