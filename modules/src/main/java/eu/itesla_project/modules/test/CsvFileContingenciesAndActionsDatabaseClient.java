/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.test;

import eu.itesla_project.iidm.network.Line;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.TieLine;
import eu.itesla_project.modules.contingencies.Action;
import eu.itesla_project.modules.contingencies.ActionPlan;
import eu.itesla_project.modules.contingencies.ActionsContingenciesAssociation;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.contingencies.Contingency;
import eu.itesla_project.modules.contingencies.ContingencyElement;
import eu.itesla_project.modules.contingencies.GeneratorContingency;
import eu.itesla_project.modules.contingencies.LineContingency;
import eu.itesla_project.modules.contingencies.Scenario;
import eu.itesla_project.modules.contingencies.Zone;
import eu.itesla_project.modules.contingencies.impl.ContingencyImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contingencies and actions database based on CSV file. Can only contain NmK
 * line contingencies.
 * <p>Example:
 * <pre>
 *#contingency id;line count;line1 id;line2 id;...
 * ...
 * </pre>
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CsvFileContingenciesAndActionsDatabaseClient implements ContingenciesAndActionsDatabaseClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvFileContingenciesAndActionsDatabaseClient.class);

    private final Path file;

    public CsvFileContingenciesAndActionsDatabaseClient(Path file) {
        this.file = file;
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        // pre-index tie lines
        Map<String, String> tieLines = new HashMap<>();
        for (Line l : network.getLines()) {
            if (l.isTieLine()) {
                TieLine tl = (TieLine) l;
                tieLines.put(tl.getHalf1().getId(), tl.getId());
                tieLines.put(tl.getHalf2().getId(), tl.getId());
            }
        }

        List<Contingency> contingencies = new ArrayList<>();
        try {
            try (BufferedReader r = Files.newBufferedReader(file, Charset.defaultCharset())) {
                String txt;
                while ((txt = r.readLine()) != null) {
                    if (txt.startsWith("#")) { // comment
                        continue;
                    }
                    if (txt.trim().isEmpty()) {
                        continue;
                    }
                    String[] tokens = txt.split(";");
                    if (tokens.length < 3) {
                        throw new RuntimeException("Error parsing '" + txt + "'");
                    }
                    String contingencyId = tokens[0];
                    int lineCount = Integer.parseInt(tokens[1]);
                    if (tokens.length != lineCount + 2) {
                        throw new RuntimeException("Error parsing '" + txt + "'");
                    }
                    List<ContingencyElement> elements = new ArrayList<>(lineCount);
                    for (int i = 2; i < lineCount + 2; i++) {
                        String id = tokens[i];
                        if (network.getLine(id) != null) {
                            elements.add(new LineContingency(id));
                        } else if (network.getGenerator(id) != null) {
                            elements.add(new GeneratorContingency(id));
                        } else if (tieLines.containsKey(id)) {
                            elements.add(new LineContingency(tieLines.get(id)));
                        } else {
                            LOGGER.warn("Contingency element '{}' not found", id);
                        }
                    }
                    if (elements.size() > 0) {
                        contingencies.add(new ContingencyImpl(contingencyId, elements));
                    } else {
                        LOGGER.warn("Skip empty contingency " + contingencyId);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return contingencies;
    }

    @Override
    public List<Scenario> getScenarios() {
        return Collections.emptyList();
    }

	@Override
	public Zone getZone(String id) {
		 throw new UnsupportedOperationException();
	}

	@Override
	public Collection<ActionPlan> getActionPlans() {
		 throw new UnsupportedOperationException();
	}

	@Override
	public ActionPlan getActionPlan(String id) {
		 throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Zone> getZones() {
		 throw new UnsupportedOperationException();
	}

	@Override
	public Collection<ActionsContingenciesAssociation> getActionsCtgAssociations() {
		 throw new UnsupportedOperationException();
	}

	@Override
	public Collection<ActionsContingenciesAssociation> getActionsCtgAssociationsByContingency(
			String contingencyId) {
		 throw new UnsupportedOperationException();
	}

	@Override
	public Contingency getContingency(String name, Network network) {
		 for (Contingency c : getContingencies(network)) {
             if (c.getId().equals(name)) {
                 return c;
             }
         }
        return null;
    }

	@Override
	public Collection<Action> getActions(Network network) {
		 throw new UnsupportedOperationException();
		
	}

	@Override
	public Action getAction(String id, Network network) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Zone> getZones(Network network) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ActionPlan> getActionPlans(Network network) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ActionsContingenciesAssociation> getActionsCtgAssociations(
			Network network) {
		// TODO Auto-generated method stub
		return null;
	}

}
