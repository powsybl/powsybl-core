/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.OperationalLimitsGroup;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.util.LoadingLimitsUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * This class is used to copy {@link OperationalLimitsGroup} between branches that might not exist at the same time.
 * If your branches do exist at the same time, it would be better to use {@link LoadingLimitsUtil#copyOperationalLimits(Branch, Branch)},
 * as it will be more performant.
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public class BranchOperationalLimitsGroupsCopy {
    private final Collection<OperationalLimitsGroup> groups1;
    private final Collection<String> selectedGroups1;

    private final Collection<OperationalLimitsGroup> groups2;
    private final Collection<String> selectedGroups2;

    public BranchOperationalLimitsGroupsCopy(Branch<?> copiedBranch) {
        this.groups1 = copiedBranch.getOperationalLimitsGroups1();
        this.selectedGroups1 = copiedBranch.getAllSelectedOperationalLimitsGroupIds(TwoSides.ONE);

        this.groups2 = copiedBranch.getOperationalLimitsGroups2();
        this.selectedGroups2 = copiedBranch.getAllSelectedOperationalLimitsGroupIds(TwoSides.TWO);
    }

    /**
     * Copies the groups of the <code>copiedBranch</code> that was used to create the instance of this {@link BranchOperationalLimitsGroupsCopy} onto <code>branch</code>,
     * on the specified sides. This works even if the original branch was deleted.
     * If a group with the same ID already exists, it is silently replaced.
     * @param branch the branch on which to copy the groups. This also deselects all selected groups on <code>branch</code>,
     *              then selects all groups that were selected on the branch the groups were copied from
     * @param sides which sides to copy to
     */
    public void applyGroupsToBranch(Branch<?> branch, TwoSides... sides) {
        List<TwoSides> sideList = Arrays.asList(sides);
        if (sideList.contains(TwoSides.ONE)) {
            LoadingLimitsUtil.copyOperationalLimits(groups1, branch::newOperationalLimitsGroup1);
            branch.cancelSelectedOperationalLimitsGroup1();
            branch.addSelectedOperationalLimitsGroups(TwoSides.ONE, selectedGroups1.toArray(String[]::new));
        }
        if (sideList.contains(TwoSides.TWO)) {
            LoadingLimitsUtil.copyOperationalLimits(groups2, branch::newOperationalLimitsGroup2);
            branch.cancelSelectedOperationalLimitsGroup2();
            branch.addSelectedOperationalLimitsGroups(TwoSides.TWO, selectedGroups2.toArray(String[]::new));
        }
    }
}
