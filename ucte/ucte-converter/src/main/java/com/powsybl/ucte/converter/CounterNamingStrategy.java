package com.powsybl.ucte.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.ucte.network.UcteCountryCode;
import com.powsybl.ucte.network.UcteElementId;
import com.powsybl.ucte.network.UcteNodeCode;
import com.powsybl.ucte.network.UcteVoltageLevelCode;

import java.util.*;

@AutoService(NamingStrategy.class)
public class CounterNamingStrategy implements NamingStrategy {
    private static final List<Character> ORDER_CODES = List.of('1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_', '-', '.', ' ');
    private static final String NO_COUNTRY_ERROR = "No country for this substation";
    private static final String NO_UCTE_CODE_ERROR = "No UCTE code found for id: %s";
    private static final String NO_UCTE_COUNTRY_ERROR = "No UCTE country code for %s";
    private static final String INVALID_NODE_CODE_ERROR = "Invalid ucte node code: %s";
    private static final char DEFAULT_VOLTAGE_CODE = '7';
    private static final int MAX_COUNTER = 99999;
    private static final int BASE_LETTER_OFFSET = 100000;
    private static final int MODULO_VALUE = 10000;

    private final Map<String, UcteNodeCode> ucteNodeIds = new HashMap<>();
    private final Map<String, UcteElementId> ucteElementIds = new HashMap<>();
    private int namingCounter;

    @Override
    public String getName() {
        return "Counter";
    }

    @Override
    public void initialiseNetwork(Network network) {
        namingCounter = 0;
        network.getSubstationStream()
                .flatMap(s -> s.getVoltageLevelStream())
                .forEach(this::processVoltageLevel);

        network.getBranchStream().forEach(this::generateUcteElementId);
        network.getDanglingLineStream().forEach(this::generateUcteElementId);
    }

    private void processVoltageLevel(VoltageLevel voltageLevel) {
        voltageLevel.getBusBreakerView().getBuses()
                .forEach(bus -> generateUcteNodeId(bus.getId(), voltageLevel));
        voltageLevel.getBusBreakerView().getSwitches()
                .forEach(this::generateUcteElementId);
    }

    private UcteNodeCode generateUcteNodeId(String nodeId, VoltageLevel voltageLevel) {
        if (UcteNodeCode.isUcteNodeId(nodeId)) {
            return processExistingUcteNodeId(nodeId);
        }
        if (ucteNodeIds.containsKey(nodeId)) {
            return ucteNodeIds.get(nodeId);
        }
        return createNewUcteNodeId(nodeId, voltageLevel);
    }

    private UcteNodeCode processExistingUcteNodeId(String nodeId) {
        return UcteNodeCode.parseUcteNodeCode(nodeId)
                .map(code -> {
                    ucteNodeIds.put(nodeId, code);
                    return code;
                })
                .orElseThrow(() -> new UcteException(String.format(INVALID_NODE_CODE_ERROR, nodeId)));
    }

    private UcteNodeCode createNewUcteNodeId(String nodeId, VoltageLevel voltageLevel) {
        StringBuilder newNodeCode = new StringBuilder(8);
        String newNodeId = generateIDFromCounter();
        char countryCode = getCountryCode(voltageLevel).getUcteCode();
        char voltageLevelCode = getUcteVoltageLevelCode(voltageLevel.getNominalV());

        return generateUniqueNodeCode(nodeId, newNodeCode, newNodeId, countryCode, voltageLevelCode);
    }

    private UcteNodeCode generateUniqueNodeCode(String nodeId, StringBuilder newNodeCode, String newNodeId,
                                                char countryCode, char voltageLevelCode) {
        for (Character orderCode : ORDER_CODES) {
            newNodeCode.setLength(0);
            newNodeCode.append(countryCode)
                    .append(newNodeId)
                    .append(voltageLevelCode)
                    .append(orderCode);

            Optional<UcteNodeCode> nodeCode = UcteNodeCode.parseUcteNodeCode(newNodeCode.toString());
            if (nodeCode.isPresent() && !ucteNodeIds.containsValue(nodeCode.get())) {
                ucteNodeIds.put(nodeId, nodeCode.get());
                return nodeCode.get();
            }
        }
        throw new UcteException("Unable to generate unique node code");
    }

    private String generateIDFromCounter() {
        namingCounter++;
        if (namingCounter > MAX_COUNTER) {
            int baseNumber = (namingCounter - BASE_LETTER_OFFSET) % MODULO_VALUE;
            char letter = (char) ('A' + ((namingCounter - BASE_LETTER_OFFSET) / MODULO_VALUE));
            return String.format("%04d%c", baseNumber, letter);
        }
        return String.format("%05d", namingCounter);
    }

    private static char getUcteVoltageLevelCode(double voltage) {
        if (voltage < UcteVoltageLevelCode.VL_27.getVoltageLevel()) {
            return DEFAULT_VOLTAGE_CODE;
        }
        if (voltage > UcteVoltageLevelCode.VL_750.getVoltageLevel()) {
            return '0';
        }

        return Arrays.stream(UcteVoltageLevelCode.values())
                .min(Comparator.comparingDouble(code ->
                        Math.abs(voltage - code.getVoltageLevel())))
                .map(code -> (char) ('0' + code.ordinal()))
                .orElse(DEFAULT_VOLTAGE_CODE);
    }

    private UcteCountryCode getCountryCode(VoltageLevel voltageLevel) {
        Country country = voltageLevel.getSubstation()
                .flatMap(Substation::getCountry)
                .orElseThrow(() -> new UcteException(NO_COUNTRY_ERROR));

        try {
            return UcteCountryCode.valueOf(country.name());
        } catch (IllegalArgumentException e) {
            throw new UcteException(String.format(NO_UCTE_COUNTRY_ERROR, country.getName()));
        }
    }

    private UcteElementId generateUcteElementId(String id, UcteNodeCode node1, UcteNodeCode node2) {
        if (ucteElementIds.containsKey(id)) {
            return ucteElementIds.get(id);
        }

        return ORDER_CODES.stream()
                .map(orderCode -> new UcteElementId(node1, node2, orderCode))
                .filter(elementId -> !ucteElementIds.containsValue(elementId))
                .findFirst()
                .map(elementId -> {
                    ucteElementIds.put(id, elementId);
                    return elementId;
                })
                .orElseThrow(() -> new UcteException("Unable to generate unique element ID"));
    }

    private UcteElementId generateUcteElementId(Branch<?> branch) {
        if (ucteElementIds.containsKey(branch.getId())) {
            return ucteElementIds.get(branch.getId());
        }
        UcteNodeCode node1 = ucteNodeIds.get(branch.getTerminal1().getBusBreakerView().getBus().getId());
        UcteNodeCode node2 = ucteNodeIds.get(branch.getTerminal2().getBusBreakerView().getBus().getId());
        return generateUcteElementId(branch.getId(), node1, node2);
    }

    private UcteElementId generateUcteElementId(DanglingLine danglingLine) {
        if (ucteElementIds.containsKey(danglingLine.getId())) {
            return ucteElementIds.get(danglingLine.getId());
        }

        Bus bus = danglingLine.getTerminal().getBusBreakerView().getBus();
        VoltageLevel voltageLevel = danglingLine.getTerminal().getVoltageLevel();

        generateUcteNodeId(danglingLine.getPairingKey(), voltageLevel);
        generateUcteNodeId(bus.getId(), voltageLevel);

        UcteNodeCode node1 = ucteNodeIds.get(danglingLine.getPairingKey());
        UcteNodeCode node2 = ucteNodeIds.get(bus.getId());

        return generateUcteElementId(danglingLine.getId(), node1, node2);
    }

    private UcteElementId generateUcteElementId(Switch sw) {
        if (ucteElementIds.containsKey(sw.getId())) {
            return ucteElementIds.get(sw.getId());
        }

        VoltageLevel.BusBreakerView view = sw.getVoltageLevel().getBusBreakerView();
        Bus bus1 = view.getBus1(sw.getId());
        Bus bus2 = view.getBus2(sw.getId());

        UcteNodeCode u1 = generateUcteNodeId(bus1.getId(), bus1.getVoltageLevel());
        UcteNodeCode u2 = generateUcteNodeId(bus2.getId(), bus2.getVoltageLevel());

        return generateUcteElementId(sw.getId(), u1, u2);
    }

    @Override
    public UcteNodeCode getUcteNodeCode(String id) {
        if (id == null) {
            throw new PowsyblException("ID is null");
        }
        return Optional.ofNullable(ucteNodeIds.get(id))
                .orElseThrow(() -> new UcteException(String.format(NO_UCTE_CODE_ERROR, id)));
    }

    @Override
    public UcteNodeCode getUcteNodeCode(Bus bus) {
        return getUcteNodeCode(bus.getId());
    }

    @Override
    public UcteNodeCode getUcteNodeCode(DanglingLine danglingLine) {
        return getUcteNodeCode(danglingLine.getPairingKey());
    }

    @Override
    public UcteElementId getUcteElementId(String id) {
        return Optional.ofNullable(ucteElementIds.get(id))
                .orElseThrow(() -> new UcteException(String.format("No UCTE element id found for: %s", id)));
    }

    @Override
    public UcteElementId getUcteElementId(Switch sw) {
        return getUcteElementId(sw.getId());
    }

    @Override
    public UcteElementId getUcteElementId(Branch branch) {
        return getUcteElementId(branch.getId());
    }

    @Override
    public UcteElementId getUcteElementId(DanglingLine danglingLine) {
        return getUcteElementId(danglingLine.getId());
    }
}
