package com.powsybl.cgmes.update;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.NamingStrategy;

public class IidmToCgmes {
    // responsible for mapping back identifiers and attribute names.
//    public IidmToCgmes(List<IidmChangeOnUpdate> changes, Context context) {
//        this.changes = changes;
//    }

    public IidmToCgmes(List<IidmChangeOnUpdate> changes) {
        this.changes = changes;
        this.namingStrategy = new NamingStrategy.Identity();
    }

    public List<String> convert(List<IidmChangeOnUpdate> changes) {
        cgmesChanges = new ArrayList<String>();
        for (IidmChangeOnUpdate change : changes) {
            String iidmIdentifiableId = change.getIdentifiableId();
            String idmAttribute = change.getAttribute();
            String iidmOldValue = change.getOldValueString();
            String iidmNewValue = change.getNewValueString();

            String cgmesSubject = namingStrategy.getCgmesId(iidmIdentifiableId);
            cgmesChanges.add(cgmesSubject);
            LOG.info("RUNNING FROM IidmToCgmes.convert() cgmesChanges are " + cgmesChanges);
        }
        return cgmesChanges;
    }

    private List<IidmChangeOnUpdate> changes;
    private final NamingStrategy namingStrategy;
    private List<String> cgmesChanges;

    private static final Logger LOG = LoggerFactory.getLogger(IidmToCgmes.class);
}
