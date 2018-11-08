/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test.network.compare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Identifiable;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class DifferencesFail implements Differences {

    @Override
    public void current(Identifiable current) {
        this.current = current;
    }

    @Override
    public void end() {
        // Nothing to do
    }

    @Override
    public void compare(String context, double expected, double actual, double tolerance) {
        assertEquals(completeContext(context), expected, actual, tolerance);
    }

    @Override
    public void compare(String context, Object expected, Object actual) {
        assertEquals(completeContext(context), expected, actual);
    }

    @Override
    public void unexpected(Identifiable i) {
        LOG.error("Unexpected {} {}", Comparison.className(i), i.getId());
        fail();
    }

    @Override
    public void missing(Identifiable i) {
        LOG.error("Missing {} {}", Comparison.className(i), i.getId());
        fail();
    }

    @Override
    public void unexpected(String property) {
        LOG.error("Unexpected {}.{} for {}", Comparison.className(current), property,
                current.getId());
    }

    @Override
    public void missing(String property) {
        LOG.error("Missing {}.{} for {}", Comparison.className(current), property, current.getId());
    }

    @Override
    public void notEquivalent(String context, Identifiable expected, Identifiable actual) {
        LOG.error("Not equivalent {}.{} for {}: expected {}, actual {}",
                Comparison.className(current),
                context,
                current.getId(),
                expected.getId(),
                actual.getId());
    }

    @Override
    public void match(Identifiable i) {
        // A match has been found, nothing to do
    }

    private String completeContext(String context) {
        return current.getId() + " " + context;
    }

    private Identifiable current;

    private static final Logger LOG = LoggerFactory.getLogger(DifferencesFail.class);
}
