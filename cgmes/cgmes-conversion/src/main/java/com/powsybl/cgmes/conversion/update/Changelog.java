package com.powsybl.cgmes.conversion.update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkListener;

public class Changelog implements NetworkListener {
    /**
     * Register Network changes
     *
     * @param network the IIDM Network
     */

    public Changelog(Network network) {
        network.addListener(this);
        this.baseChanges = new ArrayList<>();
        this.changesByVariant = new HashMap<>();
    }

    @Override
    public void onCreation(Identifiable identifiable) {
        baseChanges.add(new IidmChangeCreation(identifiable));
    }

    @Override
    public void onRemoval(Identifiable identifiable) {
        baseChanges.add(new IidmChangeRemoval(identifiable));
    }

    @Override
    public void onUpdate(Identifiable identifiable, String attribute, Object oldValue, Object newValue) {
        if (!IGNORED_ATTRIBUTES.contains(attribute)) {
            baseChanges.add(new IidmChangeUpdate(identifiable, attribute, oldValue, newValue));
        }
    }

    @Override
    public void onUpdate(Identifiable identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
        if (!IGNORED_ATTRIBUTES.contains(attribute)) {
            // Create a new list of changes if no changelog is found for the variant
            // or if the previous changelog was null
            changesByVariant.computeIfAbsent(variantId, k -> new ArrayList<>()).add(new IidmChangeUpdate(identifiable, attribute, oldValue, newValue));
        }
    }

    @Override
    public void onVariantCreated(String sourceVariantId, String targetVariantId) {
        // Will overwrite any previous changelog saved for target variant
        List<IidmChange> sourceChanges = changesByVariant.get(sourceVariantId);
        if (sourceChanges != null) {
            changesByVariant.put(targetVariantId, new ArrayList<>(sourceChanges));
        } else {
            // Source changelog is empty
            // Remove any previous changelog existing for target variant
            changesByVariant.remove(targetVariantId);
            // Specific changelog for target will be created with first change received
        }
    }

    public List<IidmChange> getChangesForVariant(String variantId) {
        if (!changesByVariant.containsKey(variantId)) {
            return Collections.unmodifiableList(baseChanges);
        } else {
            List<IidmChange> cs = new ArrayList<>(baseChanges.size() + changesByVariant.size());
            cs.addAll(baseChanges);
            cs.addAll(changesByVariant.get(variantId));
            return Collections.unmodifiableList(cs);
        }
    }

    private List<IidmChange> baseChanges;
    private Map<String, List<IidmChange>> changesByVariant;

    private static final String CONNECTED_COMPONENT_NUMBER = "connectedComponentNumber";
    private static final Set<String> IGNORED_ATTRIBUTES = new HashSet<>(Arrays.asList(
        CONNECTED_COMPONENT_NUMBER));

}
