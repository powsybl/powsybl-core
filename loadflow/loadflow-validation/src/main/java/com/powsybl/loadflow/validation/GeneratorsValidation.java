package com.powsybl.loadflow.validation;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.loadflow.validation.data.GeneratorData;
import com.powsybl.loadflow.validation.data.Validated;
import com.powsybl.loadflow.validation.io.ValidationWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;

import static com.powsybl.loadflow.validation.ValidationUtils.*;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public final class GeneratorsValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorsValidation.class);

    private GeneratorsValidation() {
    }

    public static boolean checkGenerators(Network network, ValidationConfig config, Path file) throws IOException {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(file);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            return checkGenerators(network, config, writer);
        }
    }

    public static boolean checkGenerators(Network network, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);
        try (ValidationWriter generatorsWriter = ValidationUtils.createValidationWriter(network.getId(), config, writer, ValidationType.GENERATORS)) {
            return checkGenerators(network, config, generatorsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkGenerators(Network network, ValidationConfig config, ValidationWriter generatorsWriter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(generatorsWriter);
        LOGGER.info("Checking generators of network {}", network.getId());
        BalanceTypeGuesser guesser = new BalanceTypeGuesser(network, config.getThreshold());
        LOGGER.info("Using {} balance type", guesser.getBalanceType());
        return network.getGeneratorStream()
                .sorted(Comparator.comparing(Generator::getId))
                .map(gen -> checkGenerator(gen, config, generatorsWriter, guesser))
                .reduce(Boolean::logicalAnd)
                .orElse(true);
    }

    public static boolean checkGenerator(Generator gen, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(gen);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter generatorsWriter = ValidationUtils.createValidationWriter(gen.getId(), config, writer, ValidationType.GENERATORS)) {
            return checkGenerator(gen, config, generatorsWriter, BalanceTypeGuesser.NO_BALANCING_GUESSER);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkGenerator(Generator gen, ValidationConfig config, ValidationWriter generatorsWriter, BalanceTypeGuesser guesser) {
        Objects.requireNonNull(gen);
        Objects.requireNonNull(config);
        Objects.requireNonNull(generatorsWriter);
        Objects.requireNonNull(guesser);
        double p = gen.getTerminal().getP();
        double q = gen.getTerminal().getQ();
        double targetP = gen.getTargetP();
        double targetQ = gen.getRegulatingTargetQ();
        double targetV = gen.getRegulatingTargetV();
        String regulationMode = gen.getVoltageRegulation() != null ? gen.getVoltageRegulation().getMode().toString() : null;
        boolean regulating = gen.getVoltageRegulation() != null && gen.getVoltageRegulation().isRegulating();
        double minP = gen.getMinP();
        double maxP = gen.getMaxP();
        double minQ = gen.getReactiveLimits().getMinQ(targetP);
        double maxQ = gen.getReactiveLimits().getMaxQ(targetP);
        TerminalState terminalState = getTerminalState(gen.getTerminal());
        GeneratorData d = new GeneratorData(gen.getId(),
                p, q, terminalState.v(),
                targetP, targetQ, targetV,
                regulationMode, regulating,
                minP, maxP, minQ, maxQ,
                terminalState.connected(), terminalState.mainComponent(),
                guesser);
        return checkGenerator(d, config, generatorsWriter, guesser);
    }

    public static boolean checkGenerator(GeneratorData d, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(d);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter generatorsWriter = ValidationUtils.createValidationWriter(d.generatorId(), config, writer, ValidationType.GENERATORS)) {
            return checkGenerator(d, config, generatorsWriter, BalanceTypeGuesser.NO_BALANCING_GUESSER);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Rules for valid results :<br/>
     * Rule: A validation error should be detected if there is both a voltage and a target but no p or q <br/>
     * Rule: If reactive limits are inverted (`maxQ < minQ`) and noRequirementIfReactiveBoundInversion = true, generator validation OK. <br/>
     * Rule: Active setpoint outside bounds, if `targetP` is outside `[minP, maxP]` and noRequirementIfSetpointOutsidePowerBounds = true, generator validation OK <br/>
     * Rule: Active power p matches expected setpoint <br/>
     * Rule: If voltage regulator is disabled, reactive power Q matches targetQ <br/>
     * Rule: If voltage regulator is enabled, reactive power q follow V/targetV logic<br/>
     *   - qGen ~ minQ if V > targetV + threshold <br/>
     *   - qGen ~ maxQ if V < targetV - threshold <br/>
     *   - else qGen within [minQ, maxQ])
     */
    public static boolean checkGenerator(GeneratorData d, ValidationConfig config, ValidationWriter generatorsWriter, BalanceTypeGuesser guesser) {
        Objects.requireNonNull(d);
        Objects.requireNonNull(config);
        Objects.requireNonNull(generatorsWriter);
        Objects.requireNonNull(guesser);
        boolean validated = true;

        if (isConnectedAndMainComponent(d.connected(), d.mainComponent(), config)) {
            if (Double.isNaN(d.p()) || Double.isNaN(d.q())) {
                validated = validateMissingPQRule(d.generatorId(), d.p(), d.q(), d.targetP(), d.targetQ());
            } else if (isGenReactiveBoundInverted(d.minQ(), d.maxQ(), config.getThreshold(), config.isNoRequirementIfReactiveBoundInversion())) {
                validated = true;
            } else if (isGenSetpointOutsidePowerBounds(d.targetP(), d.minP(), d.maxP(), config.getThreshold(), config.isNoRequirementIfSetpointOutsidePowerBounds())) {
                validated = true;
            } else {
                validated = checkGeneratorValues(d, config);
            }
        }
        try {
            generatorsWriter.writeGenerator(new Validated<>(d, validated));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }

    private static boolean checkGeneratorValues(GeneratorData d, ValidationConfig config) {
        boolean validated = true;
        String id = d.generatorId();
        double threshold = config.getThreshold();
        double expectedP = d.expectedP();
        if (areNaN(config, expectedP) || isGenActivePowerInconsistent(d.p(), expectedP, threshold)) {
            LOGGER.warn("{} {}: {}: P={} expectedP={}", ValidationType.GENERATORS, ValidationUtils.VALIDATION_ERROR, id, d.p(), expectedP);
            validated = false;
        }

        String regulationMode = d.regulationMode();
        boolean regulating = d.regulating();
        if ((regulationMode == null || !regulating || RegulationMode.REACTIVE_POWER.name().equals(regulationMode))
                && (areNaN(config, d.targetQ()) || isGenReactivePowerInconsistent(d.q(), d.targetQ(), threshold))) {
            LOGGER.warn("{} {}: {}: voltage regulation mode={} - Q={} targetQ={}", ValidationType.GENERATORS, ValidationUtils.VALIDATION_ERROR, id, regulationMode, d.q(), d.targetQ());
            validated = false;
        }

        double qGen = -d.q();
        if (RegulationMode.VOLTAGE.name().equals(regulationMode)
                && regulating
                && (areNaN(config, d.minQ(), d.maxQ(), d.targetV()) || isGenVoltageRegulationInconsistent(qGen, d.v(), d.targetV(), d.minQ(), d.maxQ(), threshold))) {
            LOGGER.warn("{} {}: {}: voltage regulation mode={} - Q={} minQ={} maxQ={} - V={} targetV={}",
                    ValidationType.GENERATORS,
                    ValidationUtils.VALIDATION_ERROR,
                    id,
                    regulationMode,
                    qGen,
                    d.minQ(),
                    d.maxQ(),
                    d.v(),
                    d.targetV());
            validated = false;
        }
        return validated;
    }

    /**
     * Rule: a validation error should be detected if there is both a voltage and a target but no p or q
     */
    private static boolean validateMissingPQRule(String id, double p, double q, double targetP, double targetQ) {
        if (!Double.isNaN(targetP) && targetP != 0 || !Double.isNaN(targetQ) && targetQ != 0) {
            LOGGER.warn("{} {}: {}: P={} targetP={} - Q={} targetQ={}", ValidationType.GENERATORS, ValidationUtils.VALIDATION_ERROR, id, p, targetP, q, targetQ);
            return false;
        }
        return true;
    }

    /**
     * Rule: if reactive limits are inverted (`maxQ < minQ`) and noRequirementIfReactiveBoundInversion = true, generator validation OK.
     */
    private static boolean isGenReactiveBoundInverted(double minQ, double maxQ, double threshold, boolean isNoRequirementIfReactiveBoundInversion) {
        return maxQ < minQ - threshold && isNoRequirementIfReactiveBoundInversion;
    }

    /**
     * Rule: active setpoint outside bounds, if `targetP` is outside `[minP, maxP]` and noRequirementIfSetpointOutsidePowerBounds = true, generator validation OK
     */
    private static boolean isGenSetpointOutsidePowerBounds(double targetP, double minP, double maxP, double threshold, boolean isNoRequirementIfSetpointOutsidePowerBounds) {
        return (targetP < minP - threshold || targetP > maxP + threshold) && isNoRequirementIfSetpointOutsidePowerBounds;
    }

    /**
     * Rule: Active power p matches expected setpoint
     */
    private static boolean isGenActivePowerInconsistent(double p, double expectedP, double threshold) {
        return isOutsideTolerance(p, -expectedP, threshold);
    }

    /**
     * Rule: Reactive power Q matches targetQ
     */
    private static boolean isGenReactivePowerInconsistent(double q, double targetQ, double threshold) {
        return isOutsideOrAtTolerance(q, -targetQ, threshold);
    }

    /**
     * Rule: If voltage regulator is enabled, reactive power q follow V/targetV logic:<br/>
     *  - <code> targetV - V < threshold && |Q - minQ| <= threshold</code> <br/>
     *  - <code> V - targetV < threshold && |Q - maxQ| <= threshold</code> <br/>
     *  - <code> |V - targetV|  < threshold && minQ <= Q <= maxQ </code>
     */
    private static boolean isGenVoltageRegulationInconsistent(double qGen, double v, double targetV, double minQ, double maxQ, double threshold) {

        // When V is higher than g.getTargetV() then q must equal to g.getReactiveLimits().getMinQ(p)
        // When V is lower than g.getTargetV() q must equal to g.getReactiveLimits().getMaxQ(p)
        // When V is equal to g.getTargetV() then q (reactive bounds) must satisfy
        return v > targetV + threshold && Math.abs(qGen - Math.min(minQ, maxQ)) > threshold
                || v < targetV - threshold && Math.abs(qGen - Math.max(minQ, maxQ)) > threshold
                || Math.abs(v - targetV) <= threshold && !boundedWithin(minQ, maxQ, qGen, threshold);
    }

}
