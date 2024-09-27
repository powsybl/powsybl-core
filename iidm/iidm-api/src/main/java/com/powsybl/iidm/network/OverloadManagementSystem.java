/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.extensions.Extendable;

import java.util.List;
import java.util.Optional;

/**
 * An overload management system.
 *
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public interface OverloadManagementSystem extends AutomationSystem<OverloadManagementSystem> {

    default Optional<Tripping> getTripping(String trippingKey) {
        return this.getTrippings()
                .stream()
                .filter(tripping -> tripping.getKey().equals(trippingKey))
                .findFirst();
    }

    interface Tripping extends Extendable<Tripping> {
        OverloadManagementSystem getOverloadManagementSystem();

        enum Type {
            BRANCH_TRIPPING,
            THREE_WINDINGS_TRANSFORMER_TRIPPING,
            SWITCH_TRIPPING
        }

        Type getType();

        /**
         * Get the unique key of the tripping.
         * <p>This key is unique for a single overload management system, but it can be reused
         * for another overload management system).</p>
         * @return the tripping key
         */
        String getKey();

        /**
         * Get the name (if available) or the key of the tripping.
         * <p>This method result is used for reporting purposes.</p>
         * @return the name (if available) or the key of the tripping
         */
        String getNameOrKey();

        /**
         * Set the name of the tripping.
         * <p>This name is facultative. It is used only for reporting purposes.</p>
         * @param name the name of the tripping
         * @see #getNameOrKey()
         */
        Tripping setName(String name);

        /**
         * Return the maximum acceptable current value (in A).
         * @return the maximum current value
         */
        double getCurrentLimit();

        Tripping setCurrentLimit(double currentLimit);

        /**
         * Tell if the tripping operation consists in opening (<code>true</code>) or closing (<code>false</code>)
         * the element (branch, three windings transformer or switch) to operate.
         * @return <code>true</code> it the operation consists in opening the element, else <code>false</code>.
         */
        boolean isOpenAction();

        Tripping setOpenAction(boolean open);
    }

    interface SwitchTripping extends Tripping {
        @Override
        default Type getType() {
            return Type.SWITCH_TRIPPING;
        }

        String getSwitchToOperateId();

        SwitchTripping setSwitchToOperateId(String switchToOperateId);
    }

    interface BranchTripping extends Tripping {
        @Override
        default Type getType() {
            return Type.BRANCH_TRIPPING;
        }

        String getBranchToOperateId();

        BranchTripping setBranchToOperateId(String branch);

        TwoSides getSideToOperate();

        BranchTripping setSideToOperate(TwoSides side);
    }

    interface ThreeWindingsTransformerTripping extends Tripping {
        @Override
        default Type getType() {
            return Type.THREE_WINDINGS_TRANSFORMER_TRIPPING;
        }

        String getThreeWindingsTransformerToOperateId();

        ThreeWindingsTransformerTripping setThreeWindingsTransformerToOperateId(String threeWindingsTransformerId);

        ThreeSides getSideToOperate();

        ThreeWindingsTransformerTripping setSideToOperate(ThreeSides side);
    }

    /**
     * Get the parent substation.
     * @return the parent substation
     */
    Substation getSubstation();

    /**
     * Get the id of the element (branch or three windings transformer) which is monitored
     * @return the id of the monitored element
     */
    String getMonitoredElementId();

    /**
     * Get the monitored side of the element.
     * @return the side
     * @see #getMonitoredElementId()
     */
    ThreeSides getMonitoredSide();

    /**
     * Add a tripping (operation to perform when the current is out of an acceptable interval)
     * @param tripping the tripping to add
     */
    void addTripping(Tripping tripping);

    /**
     * Return the list of the defined trippings.
     * @return the trippings
     */
    List<Tripping> getTrippings();

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.OVERLOAD_MANAGEMENT_SYSTEM;
    }
}
