/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.scripting;

import com.google.auto.service.AutoService;
import com.powsybl.afs.*;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.scripting.groovy.GroovyScripts;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import groovy.lang.Binding;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.codehaus.groovy.runtime.StackTraceUtils;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class RunScriptTool implements Tool {

    public static final String FILE = "file";

    private static final Command COMMAND = new Command() {
        @Override
        public String getName() {
            return "run-script";
        }

        @Override
        public String getTheme() {
            return "Script";
        }

        @Override
        public String getDescription() {
            return "run script (only groovy is supported)";
        }

        @Override
        public Options getOptions() {
            Options options = new Options();
            options.addOption(Option.builder()
                    .longOpt(FILE)
                    .desc("the script file")
                    .hasArg()
                    .required()
                    .argName("FILE")
                    .build());
            return options;
        }

        @Override
        public String getUsageFooter() {
            return null;
        }
    };

    private final List<AppFileSystemProvider> fileSystemProviders;

    private final List<FileExtension> fileExtensions;

    private final List<ProjectFileExtension> projectFileExtensions;

    private final List<ServiceExtension> serviceExtensions;

    public RunScriptTool() {
        this(new ServiceLoaderCache<>(AppFileSystemProvider.class).getServices(),
                new ServiceLoaderCache<>(FileExtension.class).getServices(),
                new ServiceLoaderCache<>(ProjectFileExtension.class).getServices(),
                new ServiceLoaderCache<>(ServiceExtension.class).getServices());
    }

    public RunScriptTool(List<AppFileSystemProvider> fileSystemProviders,
                         List<FileExtension> fileExtensions, List<ProjectFileExtension> projectFileExtensions,
                         List<ServiceExtension> serviceExtensions) {
        this.fileSystemProviders = Objects.requireNonNull(fileSystemProviders);
        this.fileExtensions = Objects.requireNonNull(fileExtensions);
        this.projectFileExtensions = Objects.requireNonNull(projectFileExtensions);
        this.serviceExtensions = Objects.requireNonNull(serviceExtensions);
    }

    @Override
    public Command getCommand() {
        return COMMAND;
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path file = context.getFileSystem().getPath(line.getOptionValue(FILE));
        Writer writer = new OutputStreamWriter(context.getOutputStream());
        try {
            try (AppData data = new AppData(context.getShortTimeExecutionComputationManager(),
                    context.getLongTimeExecutionComputationManager(), fileSystemProviders,
                    fileExtensions, projectFileExtensions, serviceExtensions)) {
                SoutTaskListener listener = new SoutTaskListener(context.getOutputStream());
                for (AppFileSystem fileSystem : data.getFileSystems()) {
                    fileSystem.getTaskMonitor().addListener(listener);
                }
                if (file.getFileName().toString().endsWith(".groovy")) {
                    try {
                        Binding binding = new Binding();
                        binding.setProperty("args", line.getArgs());
                        GroovyScripts.run(file, data, binding, writer);
                    } catch (Throwable t) {
                        Throwable rootCause = StackTraceUtils.sanitizeRootCause(t);
                        rootCause.printStackTrace(context.getErrorStream());
                    }
                } else {
                    throw new IllegalArgumentException("Script type not supported");
                }
            }
        } finally {
            writer.flush();
        }
    }
}
