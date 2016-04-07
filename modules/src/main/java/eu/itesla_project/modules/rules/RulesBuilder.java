/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules;

import eu.itesla_project.modules.Module;
import eu.itesla_project.modules.offline.OfflineDb;
import eu.itesla_project.modules.securityindexes.SecurityIndexId;

import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class RulesBuilder implements Module {

    public void build(String workflowId, Set<RuleAttributeSet> attributeSets, RulesBuildListener listener) throws Exception {
        build(workflowId, attributeSets, getOfflineDb().getSecurityIndexIds(workflowId), listener);
    }

    public void build(String workflowId, RuleAttributeSet attributeSet, SecurityIndexId securityIndexId) throws Exception {
        Objects.requireNonNull(attributeSet);
        Objects.requireNonNull(securityIndexId);
        build(workflowId, EnumSet.of(attributeSet), Collections.singleton(securityIndexId), null);
    }

    protected abstract OfflineDb getOfflineDb();

    protected abstract void build(final String workflowId, final Set<RuleAttributeSet> attributeSets, Collection<SecurityIndexId> securityIndexIds, RulesBuildListener listener) throws Exception;

}