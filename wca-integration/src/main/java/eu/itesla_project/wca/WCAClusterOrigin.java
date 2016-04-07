/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.wca;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum WCAClusterOrigin {
    HADES_BASE_DIVERGENCE,
    HADES_BASE_LIMIT,
    HADES_BASE_OFFLINE_RULE,
    DOMAIN_LIMIT,
    DOMAIN_OFFLINE_RULE,
    HADES_POST_CONTINGENCY_LIMIT,
    HADES_POST_CONTINGENCY_DIVERGENCE,
    CLUSTER
}
