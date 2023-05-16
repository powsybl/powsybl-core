package com.powsybl.iidm.modification.tap;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerHolder;

public class PhaseTapPositionModification extends AbstractTapPositionModification {

    /**
     * @param transfoId   the ID of the two windings transformer, which holds the ptc
     * @param tapPosition the new tap position
     */
    public static PhaseTapPositionModification createTwoWindingsPtcPosition(String transfoId, int tapPosition) {
        return new PhaseTapPositionModification(transfoId,
            TransformerType.TWO_WINDINGS_TRANSFORMER, tapPosition, null);
    }

    /**
     * @param transfoId   the ID of the three windings transformer, which holds the ptc
     * @param tapPosition the new tap position
     * @param legNumber   Defines the leg on which to apply the change for three winding tranformers. 0, 1 or 2
     */
    public static PhaseTapPositionModification createThreeWindingsPtcPosition(String transfoId, int tapPosition,
                                                                              int legNumber) {
        return new PhaseTapPositionModification(transfoId,
            TransformerType.THREE_WINDINGS_TRANSFORMER, tapPosition, legNumber);
    }

    /**
     * @param transfoId   the ID of the three windings transformer, which holds the ptc
     * @param tapPosition the new tap position
     * @param leg         defines on which leg of the three winding transformer the modification will be done. null on two windings.
     */
    public PhaseTapPositionModification(String transfoId, TransformerType element,
                                        int tapPosition, Integer leg) {
        super(transfoId, element, tapPosition, leg);
    }

    @Override
    public TapType getType() {
        return TapType.PHASE;
    }

    @Override
    public void apply(Network network, boolean throwException) {
        PhaseTapChangerHolder transformer = null;
        if (getElement() == TransformerType.TWO_WINDINGS_TRANSFORMER) {
            transformer = network.getTwoWindingsTransformer(getTransfoId());
        } else if (getElement() == TransformerType.THREE_WINDINGS_TRANSFORMER) {
            transformer = getLeg(network.getThreeWindingsTransformer(getTransfoId()));
        }

        if (transformer == null) {
            logOrThrow(throwException, TRANSFORMER_STR + getTransfoId() + "' not found");
            return;
        }
        if (!transformer.hasPhaseTapChanger()) {
            logOrThrow(throwException, TRANSFORMER_STR + getTransfoId() + "' does not have a PhaseTapChanger");
            return;
        }
        PhaseTapChanger phaseTapChanger = transformer.getPhaseTapChanger();
        if (getTapPosition() < phaseTapChanger.getLowTapPosition() || getTapPosition() > phaseTapChanger.getHighTapPosition()) {
            logOrThrow(throwException,
                "PhaseTapChanger of transformer '" + getTransfoId() + "' can't be set to the value given (out of Tap range).");
            return;
        }
        phaseTapChanger.setTapPosition(getTapPosition());
    }
}
