/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules;

import eu.itesla_project.simulation.securityindexes.SecurityIndexId;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RuleId implements Serializable, Comparable<RuleId> {
    
    private final RuleAttributeSet attributeSet;

    private final SecurityIndexId securityIndexId;

    public RuleId(RuleAttributeSet attributeSet, SecurityIndexId securityIndexId) {
        Objects.requireNonNull(attributeSet);
        Objects.requireNonNull(securityIndexId);
        this.attributeSet = attributeSet;
        this.securityIndexId = securityIndexId;
    }

    public RuleAttributeSet getAttributeSet() {
        return attributeSet;
    }

    public SecurityIndexId getSecurityIndexId() {
        return securityIndexId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RuleId) {
            RuleId other = (RuleId) obj;
            return attributeSet == other.attributeSet
                    && securityIndexId.equals(other.securityIndexId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeSet, securityIndexId);
    }

    @Override
    public int compareTo(RuleId o) {
        int c = securityIndexId.compareTo(o.securityIndexId);
        if (c == 0) {
            return attributeSet.compareTo(o.attributeSet);
        }
        return c;
    }

    @Override
    public String toString() {
        return attributeSet + "_" + securityIndexId;
    }

}
