/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SimpleCommandTest {

    @Test
    void test() {
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
    void test2() {
        SimpleCommand cmd1 = new SimpleCommandBuilder()
                .id("cmd1")
                .program("prg1")
                .args(i -> Arrays.asList("arg" + i, "file" + i))
                .build();

        assertEquals(ImmutableList.of("arg1", "file1"), cmd1.getArgs(1));
    }

    @Test
    void testInhomogeneousArgs() {
        SimpleCommand cmd1 = new SimpleCommandBuilder()
                .id("cmd1")
                .program("prg1")
                .args("literal_arg_1", "literal_arg_2")
                .arg("literal_arg_3")
                .arg(i -> "instantiated_arg_" + i)
                .build();

        assertEquals(ImmutableList.of("literal_arg_1", "literal_arg_2", "literal_arg_3", "instantiated_arg_4"), cmd1.getArgs(4));
    }

    @Test
    void testOptions() {
        SimpleCommand cmd1 = new SimpleCommandBuilder()
                .id("cmd1")
                .program("prg1")
                .option("opt1", "val1")
                .option("opt2", i -> "val2-" + i)
                .flag("flag1", false)
                .flag("flag2", true)
                .build();

        assertEquals(ImmutableList.of("--opt1=val1", "--opt2=val2-3", "--flag2"), cmd1.getArgs(3));
    }

    @Test
    void testErrorTimeout() {
        assertThrows(RuntimeException.class, () -> new SimpleCommandBuilder()
                .id("cmd1")
                .program("prg1")
                .timeout(-34)
                .build());
    }

    @Test
    void testErrorId() {
        assertThrows(RuntimeException.class, () -> new SimpleCommandBuilder()
                .program("prg1")
                .build());
    }

    @Test
    void testErrorProgram() {
        assertThrows(RuntimeException.class, () -> new SimpleCommandBuilder()
                .id("cmd1")
                .build());
    }
}
