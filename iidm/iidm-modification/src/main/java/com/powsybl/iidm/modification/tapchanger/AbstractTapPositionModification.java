/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tapchanger;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.powsybl.iidm.modification.util.ModificationLogs.logOrThrow;

/**
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 */
public abstract class AbstractTapPositionModification extends AbstractNetworkModification {
    public static final String TRANSFORMER_STR = "Transformer '";
    private final String transformerId;
    private final int tapPosition;
    /**
     * Defines the leg on which to apply the change for three winding tranformers. 0, 1 or 2
     *
     * @implNote Must NOT be empty if element == TransformerElement.THREE_WINDING_TRANSFORMER
     */
    private final ThreeSides legSide;

    /**
     * @param tapPosition the new tap position
     * @param legSide     defines on which leg of the three winding transformer the modification will be done.
     *                    If <code>null</code> on three windings transformer, {@link AbstractTapPositionModification#apply(Network)} will search for a unique rtc.
     *                    Ignored on two windings transformers.
     */
    protected AbstractTapPositionModification(String transformerId, int tapPosition,
                                              ThreeSides legSide) {
        this.transformerId = Objects.requireNonNull(transformerId);
        this.tapPosition = tapPosition;
        this.legSide = legSide;
    }

    abstract void applyTwoWindingsTransformer(Network network, TwoWindingsTransformer twoWindingsTransformer,
                                              boolean throwException);

    abstract void applyThreeWindingsTransformer(Network network, ThreeWindingsTransformer threeWindingsTransformer,
                                                boolean throwException);

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager,
                      ReportNode reportNode) {
        TwoWindingsTransformer twoWindingsTransformer = network.getTwoWindingsTransformer(getTransformerId());
        ThreeWindingsTransformer threeWindingsTransformer = network.getThreeWindingsTransformer(getTransformerId());
        if (threeWindingsTransformer != null) {
            applyThreeWindingsTransformer(network, threeWindingsTransformer, throwException);
        } else if (twoWindingsTransformer != null) {
            applyTwoWindingsTransformer(network, twoWindingsTransformer, throwException);
        } else {
            logOrThrow(throwException, "No matching transformer found with ID:" + getTransformerId());
        }
    }

    /**
     * @param isTapHolder predicate to test if the leg has the correct Tap
     * @return The leg either indicated in the constructor, or the unique one matching the predicate, null otherwise
     */
    protected ThreeWindingsTransformer.Leg getLeg(ThreeWindingsTransformer threeWindingsTransformer,
                                                  Predicate<ThreeWindingsTransformer.Leg> isTapHolder,
                                                  boolean throwException) {
        if (threeWindingsTransformer == null) {
            return null;
        }
        if (legSide != null) {
            // leg was given in the constructor, we return this leg
            return threeWindingsTransformer.getLeg(legSide);
        } else {
            // Otherwise we find a unique leg that is the holder
            Set<ThreeWindingsTransformer.Leg> validLegs = threeWindingsTransformer.getLegStream()
                .filter(isTapHolder)
                .collect(Collectors.toSet());
            if (validLegs.size() > 1) {
                logOrThrow(throwException, "Multiple valid legs found.");
                return null;
            } else if (validLegs.isEmpty()) {
                logOrThrow(throwException, "No valid legs found.");
                return null;
            } else { // validLegs.size() == 1
                return validLegs.iterator().next();
            }
        }
    }

    public String getTransformerId() {
        return transformerId;
    }

    public int getTapPosition() {
        return tapPosition;
    }

    public Optional<ThreeSides> getOptionalLeg() {
        return Optional.ofNullable(legSide);
    }

    public ThreeSides getLegSide() {
        return legSide;
    }

}
