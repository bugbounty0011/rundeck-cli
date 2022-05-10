/*
 * Copyright 2018 Rundeck, Inc. (http://rundeck.com)
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

package org.rundeck.client.tool.commands;

import org.rundeck.client.api.model.ExecRetry;
import org.rundeck.client.api.model.Execution;
import org.rundeck.client.api.model.JobFileUploadResult;
import org.rundeck.client.tool.commands.jobs.Files;
import org.rundeck.client.tool.extension.BaseCommand;
import org.rundeck.client.tool.options.FollowOptions;
import org.rundeck.client.tool.options.RetryBaseOptions;


import org.rundeck.client.tool.InputError;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;


/**
 * retry subcommand
 */
@CommandLine.Command(description = "Run a Job based on a specific execution. Specify option arguments after -- as \"-opt value\". Upload files as \"-opt " +
        "@path\" or \"-opt@ path\". If they aren't specified, the options are going to be overridden by the execution options"
        , name = "retry")
public class Retry extends BaseCommand implements Callable<Boolean> {

    public static final int SEC_MS = 1000;
    public static final int MIN_MS = 60 * 1000;
    public static final int HOUR_MS = 60 * 60 * 1000;
    public static final int DAY_MS = 24 * 60 * 60 * 1000;
    public static final int WEEK_MS = 7 * 24 * 60 * 60 * 1000;


    @CommandLine.Mixin
    RetryBaseOptions options;
    @CommandLine.Mixin
    FollowOptions followOptions;

    public Boolean call() throws IOException, InputError {
        getRdTool().requireApiVersion("retry", 24);
        String jobId = Run.getJobIdFromOpts(options, getRdOutput(), getRdTool(), () -> getRdTool().projectOrEnv(options));
        String execId = options.getEid();
        if (null == jobId) {
            return false;
        }
        Execution execution;

        final String loglevel = null != options.getLoglevel() ? options.getLoglevel().toString().toUpperCase() : null;


        ExecRetry request = new ExecRetry();
        request.setLoglevel(loglevel);
        request.setAsUser(options.getUser());
        request.setFailedNodes(Boolean.toString(options.isFailedNodes()));
        List<String> commandString = options.getCommandString();
        boolean rawOptions = options.isRawOptions();
        Map<String, String> jobopts = new HashMap<>();
        Map<String, File> fileinputs = new HashMap<>();
        String key = null;
        if (null != commandString) {
            boolean isfile = false;
            for (String part : commandString) {
                if (key == null && part.startsWith("-")) {
                    key = part.substring(1);
                    if (key.endsWith("@")) {
                        key = key.substring(0, key.length() - 1);
                        isfile = true;
                    }
                } else if (key != null) {
                    String filepath = null;
                    if (!rawOptions && part.charAt(0) == '@' && !isfile) {
                        //file input
                        filepath = part.substring(1);
                        isfile = true;
                    }
                    if (isfile) {
                        File file = new File(filepath != null ? filepath : part);
                        fileinputs.put(key, file);
                    }
                    jobopts.put(key, part);
                    key = null;
                    isfile = false;
                }
            }
        }
        if (key != null) {
            throw new InputError(
                    String.format(
                            "Incorrect job options, expected: \"-%s value\", but saw only \"-%s\"",
                            key,
                            key
                    ));
        }
        if (fileinputs.size() > 0) {
            for (String optionName : fileinputs.keySet()) {
                File file = fileinputs.get(optionName);
                if (Files.invalidInputFile(file)) {
                    throw new InputError("File Option -" + optionName + ": File cannot be read: " + file);
                }
            }
            for (String optionName : fileinputs.keySet()) {
                File file = fileinputs.get(optionName);
                JobFileUploadResult jobFileUploadResult = Files.uploadFileForJob(
                        getRdTool(),
                        file,
                        jobId,
                        optionName
                );
                String fileid = jobFileUploadResult.getFileIdForOption(optionName);
                jobopts.put(optionName, fileid);
                getRdOutput().info(String.format("File Upload OK (%s -> %s)", file, fileid));
            }
        }

        request.setOptions(jobopts);
        execution = apiCall(api -> api.retryJob(jobId, execId, request));

        String started = "started";
        getRdOutput().info(String.format("Execution %s: %s%n", started, execution.toBasicString()));

        return Executions.maybeFollow(getRdTool(), followOptions, options, execution.getId(), getRdOutput());
    }
}
