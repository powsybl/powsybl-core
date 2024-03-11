package com.powsybl.dataframe.network.adders;

import com.powsybl.commons.PowsyblException;
import com.powsybl.dataframe.ConstantsUtils;
import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.update.DoubleSeries;
import com.powsybl.dataframe.update.IntSeries;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.*;
import gnu.trove.list.array.TIntArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.powsybl.dataframe.network.adders.SeriesUtils.applyBooleanIfPresent;
import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public class PhaseTapChangerDataframeAdder implements NetworkElementAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.strings("regulation_mode"),
        SeriesMetadata.doubles("target_value"),
        SeriesMetadata.doubles("target_deadband"),
        SeriesMetadata.booleans("regulating"),
        SeriesMetadata.strings("regulated_side"),
        SeriesMetadata.ints("low_tap"),
        SeriesMetadata.ints("tap")
    );

    private static final List<SeriesMetadata> STEPS_METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.doubles("g"),
        SeriesMetadata.doubles("b"),
        SeriesMetadata.doubles("r"),
        SeriesMetadata.doubles("x"),
        SeriesMetadata.doubles("rho"),
        SeriesMetadata.doubles("alpha")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return List.of(METADATA, STEPS_METADATA);
    }

    @Override
    public void addElements(Network network, List<UpdatingDataframe> dataframes) {
        if (dataframes.size() != 2) {
            throw new PowsyblException("Expected 2 dataframes: one for tap changes, one for steps.");
        }
        UpdatingDataframe tapChangersDf = dataframes.get(0);
        UpdatingDataframe stepsDf = dataframes.get(1);
        PhaseTapChangerSeries series = new PhaseTapChangerSeries(tapChangersDf, stepsDf);
        IntStream.range(0, tapChangersDf.getRowCount())
            .forEach(row -> series.create(network, row));
    }

    private static class PhaseTapChangerSeries {
        private final StringSeries ids;
        private final StringSeries regulationModes;
        private final IntSeries taps;
        private final IntSeries lowTaps;
        private final DoubleSeries targetDeadband;
        private final DoubleSeries targetValues;
        private final IntSeries regulating;
        private final StringSeries regulatedSide;

        private final Map<String, TIntArrayList> stepsIndexes;
        private final DoubleSeries g;
        private final DoubleSeries b;
        private final DoubleSeries r;
        private final DoubleSeries x;
        private final DoubleSeries rho;
        private final DoubleSeries alpha;

        PhaseTapChangerSeries(UpdatingDataframe tapChangersDf, UpdatingDataframe stepsDf) {
            this.ids = tapChangersDf.getStrings("id");
            this.regulationModes = tapChangersDf.getStrings("regulation_mode");
            this.taps = tapChangersDf.getInts("tap");
            this.lowTaps = tapChangersDf.getInts("low_tap");
            this.targetDeadband = tapChangersDf.getDoubles("target_deadband");
            this.targetValues = tapChangersDf.getDoubles("target_value");
            this.regulating = tapChangersDf.getInts("regulating");
            this.regulatedSide = tapChangersDf.getStrings("regulated_side");

            this.stepsIndexes = getStepsIndexes(stepsDf);
            this.g = stepsDf.getDoubles("g");
            this.b = stepsDf.getDoubles("b");
            this.r = stepsDf.getDoubles("r");
            this.x = stepsDf.getDoubles("x");
            this.rho = stepsDf.getDoubles("rho");
            this.alpha = stepsDf.getDoubles("alpha");
        }

        void create(Network network, int row) {
            String transformerId = ids.get(row);
            TwoWindingsTransformer transformer = network.getTwoWindingsTransformer(transformerId);
            if (transformer == null) {
                throw new PowsyblException("Transformer " + transformerId + ConstantsUtils.DOES_NOT_EXIST);
            }
            PhaseTapChangerAdder adder = transformer.newPhaseTapChanger();
            applyIfPresent(regulationModes, row, PhaseTapChanger.RegulationMode.class, adder::setRegulationMode);
            applyIfPresent(targetDeadband, row, adder::setTargetDeadband);
            applyIfPresent(targetValues, row, adder::setRegulationValue);
            applyIfPresent(lowTaps, row, adder::setLowTapPosition);
            applyIfPresent(taps, row, adder::setTapPosition);
            applyBooleanIfPresent(regulating, row, adder::setRegulating);
            if (regulatedSide != null) {
                setRegulatedSide(transformer, adder, regulatedSide.get(row));
            }

            TIntArrayList steps = stepsIndexes.get(transformerId);
            if (steps != null) {
                steps.forEach(i -> {
                    PhaseTapChangerAdder.StepAdder stepAdder = adder.beginStep();
                    applyIfPresent(b, i, stepAdder::setB);
                    applyIfPresent(g, i, stepAdder::setG);
                    applyIfPresent(r, i, stepAdder::setR);
                    applyIfPresent(x, i, stepAdder::setX);
                    applyIfPresent(rho, i, stepAdder::setRho);
                    applyIfPresent(alpha, i, stepAdder::setAlpha);
                    stepAdder.endStep();
                    return true;
                });
            }
            adder.add();
        }
    }

    private static void setRegulatedSide(TwoWindingsTransformer transformer, PhaseTapChangerAdder adder,
                                         String regulatedSideStr) {
        if (regulatedSideStr.isEmpty()) {
            return;
        }
        Branch.Side regulatedSide = Branch.Side.valueOf(regulatedSideStr);
        adder.setRegulationTerminal(transformer.getTerminal(regulatedSide));
    }

    /**
     * Mapping transfo ID --> index of steps in the steps dataframe
     */
    private static Map<String, TIntArrayList> getStepsIndexes(UpdatingDataframe stepsDataframe) {
        StringSeries ids = stepsDataframe.getStrings("id");
        if (ids == null) {
            throw new PowsyblException("Steps dataframe: id is not set");
        }
        Map<String, TIntArrayList> stepIndexes = new HashMap<>();
        for (int stepIndex = 0; stepIndex < stepsDataframe.getRowCount(); stepIndex++) {
            String transformerId = ids.get(stepIndex);
            stepIndexes.computeIfAbsent(transformerId, k -> new TIntArrayList())
                .add(stepIndex);
        }
        return stepIndexes;
    }
}
