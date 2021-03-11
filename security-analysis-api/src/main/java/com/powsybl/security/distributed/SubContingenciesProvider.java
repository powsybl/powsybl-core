/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.distributed;

import com.powsybl.computation.Partition;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A contingencies provider which provides a subset of another provider,
 * defined by a {@link Partition}.
 *
 * For exemple, if the other provider defines 10 contingencies,
 * an instance of this provider will return the 5 first contingencies for the partition 1/2,
 * or the 5 next for the partition 2/2.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SubContingenciesProvider implements ContingenciesProvider {

    private final ContingenciesProvider delegate;
    private final Partition partition;

    public SubContingenciesProvider(ContingenciesProvider delegate, Partition partition) {
        this.delegate = Objects.requireNonNull(delegate);
        this.partition = Objects.requireNonNull(partition);
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        List<Contingency> fullList = delegate.getContingencies(network);
        int fullSize = fullList.size();
        int start = partition.startIndex(fullSize);
        int end = partition.endIndex(fullSize);
        return new ArrayList<>(fullList.subList(start, end));
    }

}
