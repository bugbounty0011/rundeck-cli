/*
 * Copyright 2017 Rundeck, Inc. (http://rundeck.com)
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

package org.rundeck.client.tool.options;

import lombok.Data;
import picocli.CommandLine;

@Data
public class ExecutionOutputFormatOption  extends VerboseOption implements OutputFormat{

    @CommandLine.Option(names = {"-%", "--outformat"},
            description = "Output format specifier for execution data. You can use \"%%key\" where key is one of:" +
                    "id, project, description, argstring, permalink, href, status, job, job.*, user, serverUUID, " +
                    "dateStarted, dateEnded, successfulNodes, failedNodes, adhoc. E.g. \"%%id %%href\"")
    String outputFormat;
}
