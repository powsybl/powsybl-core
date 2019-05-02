/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.powsybl.commons.extensions.AbstractExtension;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.*;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ComputationParametersTest {

    @Test
    public void testEmpty() {
        ComputationParameters empty = ComputationParameters.empty();
        assertFalse(empty.getTimeout("cmd").isPresent());
    }

    @Test
    public void testBuilder() {
        String cmdId = "cmd";
        ComputationParameters opts = new ComputationParametersBuilder()
                .setTimeout(cmdId, 10)
                .setDeadline(cmdId, 42)
                .build();
        assertEquals(10, opts.getTimeout(cmdId).orElse(-1));
        assertEquals(42, opts.getDeadline(cmdId).orElse(-1));
        String missingCmd = "missing";
        assertFalse(opts.getTimeout(missingCmd).isPresent());
        assertFalse(opts.getDeadline(missingCmd).isPresent());
    }

    @Test
    public void testInvalid() {
        try {
            ComputationParameters opts = new ComputationParametersBuilder()
                    .setTimeout("inv", 0)
                    .build();
            fail();
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    public void testExt() {
        // prepare
        ComputationParameters base = ComputationParameters.empty();
        QuantumComputationParameters quantum = new QuantumComputationParameters(42);
        base.addExtension(QuantumComputationParameters.class, quantum);

        // in quantum computation manager
        QuantumComputationParameters quantumComputationParameters = base.getExtension(QuantumComputationParameters.class);
        assertSame(quantum, quantumComputationParameters);
        assertEquals(42, quantumComputationParameters.getAtLeastQubits());
    }

    class QuantumComputationParameters extends AbstractExtension<ComputationParameters> {

        private final Integer atLeastQubits;

        QuantumComputationParameters(Integer atLeastQubits) {
            this.atLeastQubits = Objects.requireNonNull(atLeastQubits);
        }

        @Override
        public String getName() {
            return "QuantumComputationParameters";
        }

        public int getAtLeastQubits() {
            return atLeastQubits;
        }
    }
}
