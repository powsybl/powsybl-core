/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public interface OverloadManagementSystemAdder extends IdentifiableAdder<OverloadManagementSystem, OverloadManagementSystemAdder> {

    interface TrippingAdder {
        /**
         * Set the unique key of the tripping.
         * <p>This key is unique for a single overload management system, but it can be reused
         * for another overload management system).</p>
         * @param key the tripping key
         */
        TrippingAdder setKey(String key);

        /**
         * Set the name of the tripping.
         * <p>This name is facultative. It is used only for reporting purposes.</p>
         * @param name the name of the tripping
         */
        TrippingAdder setName(String name);

        /**
         * Change the state of the tripping.
         * @param enabled <code>true</code> to enable it
         */
        TrippingAdder setEnabled(boolean enabled);

        /**
         * Set the minimum acceptable current value (in A).
         * @param lowLimit the minimum current value
         */
        TrippingAdder setLowLimit(Double lowLimit);

        /**
         * Set the maximum acceptable current value (in A).
         * @param highLimit the maximum current value
         */
        TrippingAdder setHighLimit(Double highLimit);

        TrippingAdder setOpenAction(boolean open);

        OverloadManagementSystem.Tripping.Type getType();

        OverloadManagementSystemAdder add();
    }

    interface SwitchTrippingAdder extends TrippingAdder {
        @Override
        default OverloadManagementSystem.Tripping.Type getType() {
            return OverloadManagementSystem.Tripping.Type.SWITCH_TRIPPING;
        }

        SwitchTrippingAdder setSwitchToOperateId(String switchId);
    }

    interface BranchTrippingAdder extends TrippingAdder {
        @Override
        default OverloadManagementSystem.Tripping.Type getType() {
            return OverloadManagementSystem.Tripping.Type.BRANCH_TRIPPING;
        }

        BranchTrippingAdder setBranchToOperateId(String branchId);

        BranchTrippingAdder setBranchToOperateSide(ThreeSides side);
    }

    interface ThreeWindingsTransformerTrippingAdder extends TrippingAdder {
        @Override
        default OverloadManagementSystem.Tripping.Type getType() {
            return OverloadManagementSystem.Tripping.Type.THREE_WINDINGS_TRANSFORMER_TRIPPING;
        }

        ThreeWindingsTransformerTrippingAdder setThreeWindingsTransformerToOperate(String threeWindingsTransformerId);

        ThreeWindingsTransformerTrippingAdder setSideToOperateSide(ThreeSides side);

    }

    @Override
    OverloadManagementSystem add();

    OverloadManagementSystemAdder setEnabled(boolean enabled);

    /**
     * Set the id of the element (branch or three windings transformer) which is monitored.
     * @param monitoredElementId the id of the monitored element
     * @return the adder
     */
    OverloadManagementSystemAdder setMonitoredElementId(String monitoredElementId);

    /**
     * Set the monitored side of the element.
     * @param monitoredElementSide the monitored side of the element
     * @return the adder
     */
    OverloadManagementSystemAdder setMonitoredElementSide(ThreeSides monitoredElementSide);

    SwitchTrippingAdder newSwitchTripping();

    BranchTrippingAdder newBranchTripping();

    ThreeWindingsTransformerTrippingAdder newThreeWindingsTransformerTripping();
}
