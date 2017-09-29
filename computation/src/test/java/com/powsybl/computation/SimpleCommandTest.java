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
public class SimpleCommandTest {

    @Test
    public void test() {
        SimpleCommand cmd1 = new SimpleCommandBuilder()
                .id("cmd1")
                .program("prg1")
                .inputFiles(new InputFile("file1"))
                .args("arg1", "file1")
                .outputFiles(new OutputFile("out1"))
                .timeout(30)
                .build();

        assertEquals(CommandType.SIMPLE, cmd1.getType());
        assertEquals("cmd1", cmd1.getId());
        assertEquals("prg1", cmd1.getProgram());
        assertEquals(ImmutableList.of("arg1", "file1"), cmd1.getArgs(1));
        assertEquals(30, cmd1.getTimeout());
        assertEquals(1, cmd1.getInputFiles().size());
        assertEquals(1, cmd1.getOutputFiles().size());
        assertEquals("[prg1, arg1, file1]", cmd1.toString(1));
    }

    @Test
    public void test2() {
        SimpleCommand cmd1 = new SimpleCommandBuilder()
                .id("cmd1")
                .program("prg1")
                .args(i -> Arrays.asList("arg" + i, "file" + i))
                .build();

        assertEquals(ImmutableList.of("arg1", "file1"), cmd1.getArgs(1));
    }

    @Test(expected = RuntimeException.class)
    public void testErrorTimeout() {
        new SimpleCommandBuilder()
                .id("cmd1")
                .program("prg1")
                .timeout(-34)
                .build();
    }

    @Test(expected = RuntimeException.class)
    public void testErrorId() {
        new SimpleCommandBuilder()
                .program("prg1")
                .build();
    }

    @Test(expected = RuntimeException.class)
    public void testErrorProgram() {
        new SimpleCommandBuilder()
                .id("cmd1")
                .build();
    }
}
