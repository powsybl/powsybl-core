package com.powsybl.cgmes.conversion.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBags;

public class CgmesUpdater {

	public CgmesUpdater(Network network) {
		this.network = network;
		this.changes = new ArrayList<>();
		ChangesListener changeListener = new ChangesListener(changes);
		network.addListener(changeListener);
	}

	/**
	 * Update. Prepare triple to pass to SPARQL statement.
	 *
	 * @return the cgmes model
	 * @throws Exception the exception
	 */
	public void update(CgmesModel cgmes, String variantId) throws Exception {

		String cimNamespace = cgmes.getCimNamespace();
		String cimVersion = cimNamespace.substring(cimNamespace.lastIndexOf("cim"));

		for (IidmChange change : changes) {

			if (change.getVariant() == null || change.getVariant().equals(variantId)) {

				List<CgmesPredicateDetails> allCgmesDetails = iidmToCgmes(cimVersion, change, cgmes).convert();

				// we need to iterate over the above map, as for onCreate call there will be
				// multiples attributes-values pairs.
				Iterator entries = allCgmesDetails.iterator();
				while (entries.hasNext()) {
					CgmesPredicateDetails entry = (CgmesPredicateDetails) entries.next();
					try {
						for (String context : cgmes.tripleStore().contextNames()) {

							String currentContext = entry.getContext();
							// TODO elena : will need to add a logic to find the right context
							if (context.toUpperCase().contains(currentContext.toUpperCase())
									&& !context.toUpperCase().contains("BD")
									&& !context.toUpperCase().contains("BOUNDARY")) {

								PropertyBags result = cgmes.updateCgmes(context, getCgmesChanges(entry, change),
										instanceClassOfIidmChange(change));

								//LOG.info(result.tabulate());
							}
						}
					} catch (java.lang.NullPointerException e) {
						LOG.error("Requested attribute {} is not available for conversion\n{}", change.getAttribute(),
								e.getMessage());
					}
				}
			}
		}
	}

	private AbstractIidmToCgmes iidmToCgmes(String cimVersion, IidmChange change, CgmesModel cgmes) {
		AbstractIidmToCgmes iidmToCgmes = null;

		if (cimVersion.equals("cim14#")) {
			iidmToCgmes = new IidmToCgmes14(change, cgmes);
		} else {
			iidmToCgmes = new IidmToCgmes16(change, cgmes);
		}
		return iidmToCgmes;
	}

	private Map<String, String> getCgmesChanges(CgmesPredicateDetails entry, IidmChange change) {

		Map<String, String> cgmesChanges = new HashMap<>();
		cgmesChanges.put("cgmesSubject",
				(entry.getNewSubject() != null) ? entry.getNewSubject() : change.getIdentifiableId());
		cgmesChanges.put("cgmesPredicate", entry.getRdfPredicate());
		cgmesChanges.put("cgmesNewValue", entry.getValue());
		cgmesChanges.put("valueIsNode", String.valueOf(entry.valueIsNode()));

		return cgmesChanges;
	}

	private String instanceClassOfIidmChange(IidmChange change) {
		return change.getClass().getSimpleName();
	}

	public int getNumberOfChanges() {
		return changes.size();
	}

	private Network network;

	private List<IidmChange> changes;

	private static final Logger LOG = LoggerFactory.getLogger(CgmesUpdater.class);
}
