package com.powsybl.iidm.network.impl.util.translation;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.translation.NetworkElementInterface;

import java.util.Optional;

public class NetworkElementImpl implements NetworkElementInterface {

    private final Identifiable identifiable;

    NetworkElementImpl(Identifiable identifiable) {
        this.identifiable = identifiable;
    }

    @Override
    public String getId() {
        return identifiable.getId();
    }

    @Override
    public Country getCountry1() {
        return getCountry(TwoSides.ONE);
    }

    @Override
    public Country getCountry2() {
        return getCountry(TwoSides.TWO);
    }

    @Override
    public Country getCountry() {
        switch (identifiable.getType()) {
            case TWO_WINDINGS_TRANSFORMER -> {
                Optional<Substation> substation = ((TwoWindingsTransformer) identifiable).getSubstation();
                return substation.isPresent() ? substation.get().getNullableCountry() : null;
            }
            case THREE_WINDINGS_TRANSFORMER -> {
                Optional<Substation> substation = ((ThreeWindingsTransformer) identifiable).getSubstation();
                return substation.isPresent() ? substation.get().getNullableCountry() : null;
            }
            default -> {
                return getCountry1() != null ? getCountry1() : getCountry2();
            }

        }
    }

    private Country getCountry(TwoSides side) {
        switch (identifiable.getType()) {
            case LINE -> {
                return getCountryFromTerminal(((Line) identifiable).getTerminal(side));
            }
            case TIE_LINE -> {
                return getCountryFromTerminal(((TieLine) identifiable).getDanglingLine(side).getTerminal());
            }
            case HVDC_LINE -> {
                return getCountryFromTerminal(((HvdcLine) identifiable).getConverterStation(side).getTerminal());
            }
            default -> {
                return null;
            }
        }
    }

    private Country getCountryFromTerminal(Terminal terminal) {
        Optional<Substation> substation = terminal.getVoltageLevel().getSubstation();
        return substation.isPresent() ? substation.get().getNullableCountry() : null;
    }

    @Override
    public VoltageLevel getVoltageLevel1() {
        return getVoltageLevel(ThreeSides.ONE);
    }

    @Override
    public VoltageLevel getVoltageLevel2() {
        return getVoltageLevel(ThreeSides.TWO);
    }

    @Override
    public VoltageLevel getVoltageLevel3() {
        return getVoltageLevel(ThreeSides.THREE);
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return getVoltageLevel1() != null ? getVoltageLevel1() : getVoltageLevel2() != null ? getVoltageLevel2() : getVoltageLevel3();
    }

    private VoltageLevel getVoltageLevel(ThreeSides side) {
        switch (identifiable.getType()) {
            case LINE -> {
                return ((Line) identifiable).getTerminal(side.toTwoSides()).getVoltageLevel();
            }
            case TIE_LINE -> {
                return ((TieLine) identifiable).getTerminal(side.toTwoSides()).getVoltageLevel();
            }
            case HVDC_LINE -> {
                return ((HvdcLine) identifiable).getConverterStation(side.toTwoSides()).getTerminal().getVoltageLevel();
            }
            case TWO_WINDINGS_TRANSFORMER -> {
                return ((TwoWindingsTransformer) identifiable).getTerminal(side.toTwoSides()).getVoltageLevel();
            }
            case THREE_WINDINGS_TRANSFORMER -> {
                return ((ThreeWindingsTransformer) identifiable).getTerminal(side).getVoltageLevel();
            }
            default -> {
                return null;
            }
        }
    }

    @Override
    public Optional<? extends LoadingLimits> getLoadingLimits(LimitType limitType, ThreeSides side) {
        switch (identifiable.getType()) {
            case LINE -> {
                return ((Line) identifiable).getLimits(limitType, side.toTwoSides());
            }
            case TIE_LINE -> {
                return ((TieLine) identifiable).getLimits(limitType, side.toTwoSides());
            }
            case HVDC_LINE -> {
                return null; //((HvdcLine) identifiable).getConverterStation(side.toTwoSides()).;
            }
            case TWO_WINDINGS_TRANSFORMER -> {
                return ((TwoWindingsTransformer) identifiable).getLimits(limitType, side.toTwoSides());
            }
            case THREE_WINDINGS_TRANSFORMER -> {
                return ((ThreeWindingsTransformer) identifiable).getLeg(side).getLimits(limitType);
            }
            default -> {
                return null;
            }
        }
    }
}
