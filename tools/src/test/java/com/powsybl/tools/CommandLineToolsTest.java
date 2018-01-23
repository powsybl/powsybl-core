/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools;

import com.powsybl.commons.PowsyblException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CommandLineToolsTest extends AbstractToolTest {

    private class Tool1 implements Tool {

        @Override
        public Command getCommand() {
            return new Command() {
                @Override
                public String getName() {
                    return "tool1";
                }

                @Override
                public String getTheme() {
                    return "theme1";
                }

                @Override
                public String getDescription() {
                    return "test tool1";
                }

                @Override
                public Options getOptions() {
                    Options options = new Options();
                    options.addOption(Option.builder()
                            .longOpt("option1")
                            .desc("this is option 1")
                            .hasArg()
                            .argName("FILE")
                            .required()
                            .build());
                    return options;
                }

                @Override
                public String getUsageFooter() {
                    return "footer1";
                }
            };
        }

        @Override
        public void run(CommandLine line, ToolRunningContext context) throws Exception {
            context.getOutputStream().print("result1");
        }
    }

    private class Tool2 implements Tool {

        @Override
        public Command getCommand() {
            return new Command() {
                @Override
                public String getName() {
                    return "tool2";
                }

                @Override
                public String getTheme() {
                    return "theme2";
                }

                @Override
                public String getDescription() {
                    return "test tool2";
                }

                @Override
                public Options getOptions() {
                    Options options = new Options();
                    options.addOption(Option.builder()
                            .longOpt("option2")
                            .desc("this is option 2")
                            .required(false)
                            .build());
                    return options;
                }

                @Override
                public String getUsageFooter() {
                    return null;
                }
            };
        }

        @Override
        public void run(CommandLine line, ToolRunningContext context) throws Exception {
            throw new PowsyblException("error2");
        }
    }

    @Override
    protected Iterable<Tool> getTools() {
        return Arrays.asList(new Tool1(), new Tool2());
    }

    @Override
    public void assertCommand() {
        Tool tool = new Tool1();
        Command command = tool.getCommand();

        assertCommand(command, "tool1", 1, 1);
        assertOption(command.getOptions(), "option1", true, true);

        tool = new Tool2();
        command = tool.getCommand();
        assertCommand(command, "tool2", 1, 0);
        assertOption(command.getOptions(), "option2", false, false);
    }

    @Test
    public void test() throws IOException {
        String scriptOptions = "Available options are:" + System.lineSeparator() +
            "    --config-name <CONFIG_NAME>   Override configuration file name" + System.lineSeparator() +
            "    --parallel                    Run command in parallel mode" + System.lineSeparator();

        // usage when no command is specified
        String usage = "usage: itools [OPTIONS] COMMAND [ARGS]" + System.lineSeparator() +
                System.lineSeparator() +
                scriptOptions +
                System.lineSeparator() +
                "Available commands are:" + System.lineSeparator() +
                System.lineSeparator() +
                "theme1:" + System.lineSeparator() +
                "    tool1                                    test tool1" + System.lineSeparator() +
                System.lineSeparator() +
                "theme2:" + System.lineSeparator() +
                "    tool2                                    test tool2" + System.lineSeparator() +
                System.lineSeparator();

        assertCommand(new String[] {}, CommandLineTools.COMMAND_NOT_FOUND_STATUS, "", usage);

        // usage when command does not exist
        assertCommand(new String[] {"tool3"}, CommandLineTools.COMMAND_NOT_FOUND_STATUS, "", usage);

        // command success
        assertCommand(new String[] {"tool1", "--option1", "file.txt"}, CommandLineTools.COMMAND_OK_STATUS, "result1", "");

        // command failure
        assertCommand(new String[] {"tool2"}, CommandLineTools.EXECUTION_ERROR_STATUS, "", "com.powsybl.commons.PowsyblException: error2.*");

        // invalid option
        assertCommand(new String[] {"tool1", "--optionA", "file.txt"}, CommandLineTools.INVALID_COMMAND_STATUS, "",
                "error: Unrecognized option: --optionA" + System.lineSeparator() +
                        "usage: itools [OPTIONS] tool1 [--help] --option1 <FILE>" + System.lineSeparator() +
                        System.lineSeparator() +
                        scriptOptions +
                        System.lineSeparator() +
                        "Available arguments are:" + System.lineSeparator() +
                        "    --help             display the help and quit" + System.lineSeparator() +
                        "    --option1 <FILE>   this is option 1" + System.lineSeparator() +
                        System.lineSeparator() +
                        "footer1" + System.lineSeparator());

        // required option not specified
        assertCommand(new String[] {"tool1"}, CommandLineTools.INVALID_COMMAND_STATUS, "",
                "error: Missing required option: option1" + System.lineSeparator() +
                        "usage: itools [OPTIONS] tool1 [--help] --option1 <FILE>" + System.lineSeparator() +
                        System.lineSeparator() +
                        scriptOptions +
                        System.lineSeparator() +
                        "Available arguments are:" + System.lineSeparator() +
                        "    --help             display the help and quit" + System.lineSeparator() +
                        "    --option1 <FILE>   this is option 1" + System.lineSeparator() +
                        System.lineSeparator() +
                        "footer1" + System.lineSeparator());

        // command help
        assertCommand(new String[] {"tool1", "--help"}, CommandLineTools.COMMAND_OK_STATUS, "",
                        "usage: itools [OPTIONS] tool1 [--help] --option1 <FILE>" + System.lineSeparator() +
                        System.lineSeparator() +
                        scriptOptions +
                        System.lineSeparator() +
                        "Available arguments are:" + System.lineSeparator() +
                        "    --help             display the help and quit" + System.lineSeparator() +
                        "    --option1 <FILE>   this is option 1" + System.lineSeparator() +
                        System.lineSeparator() +
                        "footer1" + System.lineSeparator());

    }
}
