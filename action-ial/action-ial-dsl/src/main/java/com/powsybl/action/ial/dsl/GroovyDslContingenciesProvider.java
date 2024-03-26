/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.dsl;

import com.google.common.collect.ImmutableList;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.dsl.AbstractDslContingenciesProvider;
import com.powsybl.iidm.network.Network;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * A {@link ContingenciesProvider} which provides same contingencies definition syntax as
 * {@link com.powsybl.contingency.dsl.GroovyDslContingenciesProvider},
 * but allows to use more complete action-dsl scripts as is for standard security analysis.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class GroovyDslContingenciesProvider extends AbstractDslContingenciesProvider {

    public GroovyDslContingenciesProvider(final Path path) {
        super(path);
    }

    public GroovyDslContingenciesProvider(final InputStream input) {
        super(input);
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        ActionDb actionDb = new ActionDslLoader(script).load(network);
        return ImmutableList.copyOf(actionDb.getContingencies());
    }
}
