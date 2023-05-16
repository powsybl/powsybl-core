package com.powsybl.iidm.modification.tap;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.OptionalInt;

public abstract class AbstractTapPositionModification extends AbstractNetworkModification {
    public static final String TRANSFORMER_STR = "Transformer '";
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTapPositionModification.class);
    private final String transfoId;
    private final TransformerType element;
    private final int tapPosition;
    /**
     * Defines the leg on which to apply the change for three winding tranformers. 0, 1 or 2
     *
     * @implNote Must NOT be empty if element == TransformerElement.THREE_WINDING_TRANSFORMER
     */
    private final Integer leg;

    /**
     * @param tapPosition the new tap position
     * @param leg         defines on which leg of the three winding transformer the modification will be done. Ignored on two windings.
     */
    public AbstractTapPositionModification(String transfoId, TransformerType element, int tapPosition,
                                           Integer leg) {
        this.transfoId = Objects.requireNonNull(transfoId);
        this.element = Objects.requireNonNull(element);
        this.tapPosition = tapPosition;
        this.leg = leg;
        if (element == TransformerType.THREE_WINDINGS_TRANSFORMER && leg == null) {
            throw new PowsyblException("TapPositionModification needs a leg for three winding transformers");
        } else if (element == TransformerType.THREE_WINDINGS_TRANSFORMER && (leg < 0 || leg > 2)) {
            throw new PowsyblException("Leg number is invalid, must be  0, 1 or 2");
        }
        if (element == TransformerType.TWO_WINDINGS_TRANSFORMER && leg != null) {
            LOGGER.warn("TapPositionModification does not need a side for two winding transformers");
        }
    }

    public abstract TapType getType();

    abstract void apply(Network network, boolean throwException);

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager,
                      Reporter reporter) {
        apply(network, throwException);
    }

    protected ThreeWindingsTransformer.Leg getLeg(ThreeWindingsTransformer threeWindingsTransformer) {
        if (threeWindingsTransformer == null) {
            return null;
        }
        // Constructor ensures that, on Three Winding Transformers, leg cannot be null.
        return threeWindingsTransformer.getLegs().get(leg);
    }

    public String getTransfoId() {
        return transfoId;
    }

    public int getTapPosition() {
        return tapPosition;
    }

    public OptionalInt getLeg() {
        return leg == null ? OptionalInt.empty() : OptionalInt.of(leg);
    }

    public TransformerType getElement() {
        return element;
    }

    public enum TransformerElement {
        TWO_WINDINGS_TRANSFORMER, THREE_WINDINGS_TRANSFORMER
    }

}
