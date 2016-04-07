/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.db;

import java.util.Objects;

import eu.itesla_project.modules.security.LimitViolationType;
import eu.itesla_project.modules.security.SecurityIssueType;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class Violation {

	private final String subject;
    private final LimitViolationType limitType;
    private final float limit;
    private final float value;

    Violation(String subject, LimitViolationType limitType, float limit, float value) {
        this.subject = Objects.requireNonNull(subject);
        this.limitType = Objects.requireNonNull(limitType);
        this.limit = limit;
        this.value = value;
    }

    public SecurityIssueType getIssueType() {
        return SecurityIssueType.LIMIT;
    }

    public String getSubject() {
        return subject;
    }

    public LimitViolationType getLimitType() {
        return limitType;
    }

    public float getLimit() {
        return limit;
    }

    public float getValue() {
        return value;
    }
    
    public String getId() {
    	return subject + "_" + limitType.name();
    }
}
