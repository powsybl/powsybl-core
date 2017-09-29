/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GroupCommandTest {

    @Test
    public void test() {
        GroupCommand cmd1 = new GroupCommandBuilder()
                .id("cmd1")
                .inputFiles(new InputFile("file1"))
                .outputFiles(new OutputFile("out1"))
                .subCommand()
                    .program("prg1")
                    .args("arg1", "file1")
                    .timeout(30)
                .add()
                .build();

        assertEquals(CommandType.GROUP, cmd1.getType());
        assertEquals("cmd1", cmd1.getId());
        assertEquals(1, cmd1.getInputFiles().size());
        assertEquals(1, cmd1.getOutputFiles().size());
        assertEquals(1, cmd1.getSubCommands().size());
        GroupCommand.SubCommand prg1 = cmd1.getSubCommands().get(0);
        assertEquals("prg1", prg1.getProgram());
        assertEquals(ImmutableList.of("arg1", "file1"), prg1.getArgs(1));
        assertEquals(30, prg1.getTimeout());
        assertEquals("[[prg1, arg1, file1]]", cmd1.toString(1));
    }

    @Test
    public void test2() {
        GroupCommand cmd1 = new GroupCommandBuilder()
                .id("cmd1")
                .subCommand()
                    .program("prg1")
                    .args(i -> Arrays.asList("arg" + i, "file" + i))
                    .timeout(30)
                .add()
                .build();
        GroupCommand.SubCommand prg1 = cmd1.getSubCommands().get(0);
        assertEquals(ImmutableList.of("arg1", "file1"), prg1.getArgs(1));
    }

    @Test(expected = RuntimeException.class)
    public void testErrorTimeout() {
        new GroupCommandBuilder()
                .id("cmd1")
                .subCommand()
                    .program("prg1")
                    .args()
                    .timeout(-10)
                .add()
                .build();
    }

    @Test(expected = RuntimeException.class)
    public void testErrorId() {
        new GroupCommandBuilder()
                .subCommand()
                    .program("prg1")
                    .args()
                .add()
                .build();
    }

    @Test(expected = RuntimeException.class)
    public void testErrorProgram() {
        new GroupCommandBuilder()
                .id("cmd1")
                .subCommand()
                    .args()
                .add()
                .build();
    }
}
