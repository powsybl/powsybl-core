/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

/**
 * @author Étienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class SimpleLimitViolationIdImpl implements LimitViolationId {

    private final String subjectId;

    public SimpleLimitViolationIdImpl(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    @Override
    public String getId() {
        return subjectId;
    }

    @Override
    public String toString() {
        return "SimpleLimitViolationIdImpl{" +
            "subjectId='" + subjectId + '\'' +
            '}';
    }
}
