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

import cd.go.contrib.elasticagents.dockerswarm.model.ValidationError;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MemoryMetadataTest {

    @Test
    public void shouldValidateMemoryBytes() throws Exception {
        assertNull(new MemoryMetadata("Disk", false).validate("100mb"));

        ValidationError validationError = new MemoryMetadata("Disk", false).validate("xxx");

        assertNotNull(validationError);
        assertThat(validationError.key(), is("Disk"));
        assertThat(validationError.message(), is("Invalid size: xxx"));
    }

    @Test
    public void shouldValidateMemoryBytesWhenRequireField() throws Exception {
        ValidationError validationError = new MemoryMetadata("Disk", true).validate(null);

        assertNotNull(validationError);
        assertThat(validationError.key(), is("Disk"));
        assertThat(validationError.message(), is("Disk must not be blank."));
    }
}