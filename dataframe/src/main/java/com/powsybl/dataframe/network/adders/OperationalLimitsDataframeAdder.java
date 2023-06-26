package com.powsybl.dataframe.network.adders;

import com.powsybl.commons.PowsyblException;
import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.update.DoubleSeries;
import com.powsybl.dataframe.update.IntSeries;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.*;
import com.powsybl.dataframe.TemporaryLimitData;
import gnu.trove.list.array.TIntArrayList;

import java.util.*;

import static com.powsybl.dataframe.network.adders.SeriesUtils.*;

public class OperationalLimitsDataframeAdder implements NetworkElementAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("element_id"),
        SeriesMetadata.strings("name"),
        SeriesMetadata.strings("element_type"),
        SeriesMetadata.strings("side"),
        SeriesMetadata.strings("type"),
        SeriesMetadata.doubles("value"),
        SeriesMetadata.ints("acceptable_duration"),
        SeriesMetadata.booleans("is_fictitious")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static final class OperationalLimitsSeries {

        private final StringSeries elementIds;
        private final StringSeries names;
        private final StringSeries elementTypes;
        private final StringSeries sides;
        private final StringSeries types;
        private final DoubleSeries values;
        private final IntSeries acceptableDurations;
        private final IntSeries fictitious;

        OperationalLimitsSeries(UpdatingDataframe dataframe) {
            this.elementIds = getRequiredStrings(dataframe, "element_id");
            this.names = dataframe.getStrings("name");
            this.elementTypes = getRequiredStrings(dataframe, "element_type");
            this.sides = getRequiredStrings(dataframe, "side");
            this.types = getRequiredStrings(dataframe, "type");
            this.values = getRequiredDoubles(dataframe, "value");
            this.acceptableDurations = getRequiredInts(dataframe, "acceptable_duration");
            this.fictitious = dataframe.getInts("is_fictitious");
        }

        public StringSeries getElementIds() {
            return elementIds;
        }

        public StringSeries getNames() {
            return names;
        }

        public StringSeries getElementTypes() {
            return elementTypes;
        }

        public StringSeries getSides() {
            return sides;
        }

        public StringSeries getTypes() {
            return types;
        }

        public DoubleSeries getValues() {
            return values;
        }

        public IntSeries getAcceptableDurations() {
            return acceptableDurations;
        }

        public IntSeries getFictitious() {
            return fictitious;
        }
    }

    @Override
    public void addElements(Network network, List<UpdatingDataframe> dataframes) {
        UpdatingDataframe primaryTable = dataframes.get(0);
        OperationalLimitsSeries series = new OperationalLimitsSeries(primaryTable);

        Map<LimitsDataframeAdderKey, TIntArrayList> indexMap = new HashMap<>();
        for (int i = 0; i < primaryTable.getRowCount(); i++) {
            String elementId = series.getElementIds().get(i);
            String side = series.getSides().get(i);
            String limitType = series.getTypes().get(i);
            LimitsDataframeAdderKey key = new LimitsDataframeAdderKey(elementId, side, limitType);
            indexMap.computeIfAbsent(key, k -> new TIntArrayList()).add(i);
        }

        addElements(network, series, indexMap);
    }

    private static void addElements(Network network, OperationalLimitsSeries series,
                                    Map<LimitsDataframeAdderKey, TIntArrayList> indexMap) {
        indexMap.forEach((key, indexList) -> createLimits(network, series, key.getElementId(),
            key.getSide(), key.getLimitType(), indexList));
    }

    private static void createLimits(Network network, OperationalLimitsSeries series, String elementId, String side,
                                     String type,
                                     TIntArrayList indexList) {
        IdentifiableType elementType = IdentifiableType.valueOf(series.getElementTypes().get(indexList.get(0)));
        LimitType limitType = LimitType.valueOf(type);
        TemporaryLimitData.Side limitSide = TemporaryLimitData.Side.valueOf(side);

        LoadingLimitsAdder adder = getAdder(network, elementType, elementId, limitType, limitSide);
        for (int index : indexList.toArray()) {
            createLimits(adder, index, series);
        }
        adder.add();
    }

    private static void createLimits(LoadingLimitsAdder adder, int row, OperationalLimitsSeries series) {
        int acceptableDuration = series.getAcceptableDurations().get(row);
        if (acceptableDuration == -1) {
            applyIfPresent(series.getValues(), row, adder::setPermanentLimit);
        } else {
            LoadingLimitsAdder.TemporaryLimitAdder temporaryLimitAdder = adder.beginTemporaryLimit()
                .setAcceptableDuration(acceptableDuration);
            applyIfPresent(series.getNames(), row, temporaryLimitAdder::setName);
            applyIfPresent(series.getValues(), row, temporaryLimitAdder::setValue);
            applyBooleanIfPresent(series.getFictitious(), row, temporaryLimitAdder::setFictitious);
            temporaryLimitAdder.endTemporaryLimit();
        }
    }

    /**
     * Wraps a branch in a flows limits holder view
     */
    private static FlowsLimitsHolder getBranchAsFlowsLimitsHolder(Branch<?> branch, Branch.Side side) {
        return new FlowsLimitsHolder() {

            @Override
            public Collection<OperationalLimits> getOperationalLimits() {
                return side == Branch.Side.ONE ? branch.getOperationalLimits1() : branch.getOperationalLimits2();
            }

            @Override
            public Optional<CurrentLimits> getCurrentLimits() {
                return branch.getCurrentLimits(side);
            }

            @Override
            public CurrentLimits getNullableCurrentLimits() {
                return side == Branch.Side.ONE ? branch.getNullableCurrentLimits1() : branch.getNullableCurrentLimits2();
            }

            @Override
            public Optional<ActivePowerLimits> getActivePowerLimits() {
                return branch.getActivePowerLimits(side);
            }

            @Override
            public ActivePowerLimits getNullableActivePowerLimits() {
                return side == Branch.Side.ONE ? branch.getNullableActivePowerLimits1() : branch.getNullableActivePowerLimits2();
            }

            @Override
            public Optional<ApparentPowerLimits> getApparentPowerLimits() {
                return branch.getApparentPowerLimits(side);
            }

            @Override
            public ApparentPowerLimits getNullableApparentPowerLimits() {
                return getApparentPowerLimits().orElse(null);
            }

            @Override
            public CurrentLimitsAdder newCurrentLimits() {
                return side == Branch.Side.ONE ? branch.newCurrentLimits1() : branch.newCurrentLimits2();
            }

            @Override
            public ApparentPowerLimitsAdder newApparentPowerLimits() {
                return side == Branch.Side.ONE ? branch.newApparentPowerLimits1() : branch.newApparentPowerLimits2();
            }

            @Override
            public ActivePowerLimitsAdder newActivePowerLimits() {
                return side == Branch.Side.ONE ? branch.newActivePowerLimits1() : branch.newActivePowerLimits2();
            }
        };
    }

    private static Branch.Side toBranchSide(TemporaryLimitData.Side side) {
        switch (side) {
            case ONE:
                return Branch.Side.ONE;
            case TWO:
                return Branch.Side.TWO;
            default:
                throw new PowsyblException("Invalid value for branch side: " + side);
        }
    }

    private static FlowsLimitsHolder getLimitsHolder(Network network, IdentifiableType identifiableType,
                                                     String elementId, TemporaryLimitData.Side side) {
        switch (identifiableType) {
            case LINE:
            case TWO_WINDINGS_TRANSFORMER:
                Branch<?> branch = network.getBranch(elementId);
                if (branch == null) {
                    throw new PowsyblException("Branch " + elementId + " does not exist.");
                }
                return getBranchAsFlowsLimitsHolder(branch, toBranchSide(side));
            case DANGLING_LINE:
                DanglingLine dl = network.getDanglingLine(elementId);
                if (dl == null) {
                    throw new PowsyblException("Dangling line " + elementId + " does not exist.");
                }
                if (side != TemporaryLimitData.Side.NONE) {
                    throw new PowsyblException("Invalid value for dangling line side: " + side + ", must be NONE");
                }
                return dl;
            case THREE_WINDINGS_TRANSFORMER:
                ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(elementId);
                if (transformer == null) {
                    throw new PowsyblException("Three windings transformer " + elementId + " does not exist.");
                }
                switch (side) {
                    case ONE:
                        return transformer.getLeg1();
                    case TWO:
                        return transformer.getLeg2();
                    case THREE:
                        return transformer.getLeg3();
                    default:
                        throw new PowsyblException("Invalid value for three windings transformer side: " + side);
                }
            default:
                throw new PowsyblException("Cannot create operational limits for element of type " + identifiableType);
        }
    }

    private static LoadingLimitsAdder getAdder(Network network, IdentifiableType identifiableType, String elementId,
                                               LimitType type, TemporaryLimitData.Side side) {
        FlowsLimitsHolder limitsHolder = getLimitsHolder(network, identifiableType, elementId, side);
        switch (type) {
            case CURRENT:
                return limitsHolder.newCurrentLimits();
            case ACTIVE_POWER:
                return limitsHolder.newActivePowerLimits();
            case APPARENT_POWER:
                return limitsHolder.newApparentPowerLimits();
            default:
                throw new PowsyblException(String.format("Limit type %s does not exist.", type));
        }
    }
}
