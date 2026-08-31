/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.util;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import static com.powsybl.commons.util.Result.failure;
import static com.powsybl.commons.util.Result.success;

/**
 * @author Nico Westerbeck {@literal <nico.westerbeck at 50hertz.com>}
 */
class ResultTest {

    private static final Result<Integer, String> SUCCESS = success(2);
    private static final Result<Integer, String> FAILURE = failure("not a number");

    @Test
    void successCarriesItsValue() {
        assertEquals(new Result.Success<Integer, String>(2), SUCCESS);
        assertEquals(2, SUCCESS.fold(value -> value, reason -> -1).intValue());
    }

    @Test
    void failureCarriesItsReason() {
        assertEquals(new Result.Failure<Integer, String>("not a number"), FAILURE);
        assertEquals("not a number", FAILURE.fold(String::valueOf, reason -> reason));
    }

    @Test
    void mapAppliesToTheValueOnly() {
        assertEquals(success(4), SUCCESS.map(value -> value * 2));
        assertEquals(failure("not a number"), FAILURE.map(value -> value * 2));
    }

    @Test
    void mapIsNotAppliedToAFailure() {
        List<Integer> applied = new ArrayList<>();
        FAILURE.map(applied::add);
        assertEquals(List.of(), applied);
    }

    @Test
    void flatMapContinuesOnTheSuccessTrackOnly() {
        assertEquals(success(1), SUCCESS.flatMap(value -> success(value - 1)));
        assertEquals(failure("odd"), SUCCESS.flatMap(value -> failure("odd")));
        assertEquals(failure("not a number"), FAILURE.flatMap(value -> success(value - 1)));
    }

    @Test
    void chainingStopsAtTheFirstFailure() {
        Result<Integer, String> chained = SUCCESS
                .flatMap(value -> failure("too small"))
                .flatMap(value -> failure("too large"));
        assertEquals(failure("too small"), chained);
    }


    @Test
    void neitherTrackCarriesNull() {
        assertThrows(NullPointerException.class, () -> success(null));
        assertThrows(NullPointerException.class, () -> failure(null));
        assertThrows(NullPointerException.class, () -> SUCCESS.map(value -> null));
        assertThrows(NullPointerException.class, () -> SUCCESS.flatMap(value -> null));
    }
}
