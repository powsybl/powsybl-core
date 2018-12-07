package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.triplestore.api.PropertyBag;

public abstract class AbstractBranchConversion extends AbstractConductingEquipmentConversion {

    public AbstractBranchConversion(
            String type,
            PropertyBag p,
            Conversion.Context context) {
        super(type, p, context, 2);
    }

    @Override
    public boolean valid() {
        if (!super.valid()) {
            return false;
        }
        String node1 = nodeId(1);
        String node2 = nodeId(2);
        if (context.boundary().containsNode(node1)
                || context.boundary().containsNode(node2)) {
            invalid("Has " + nodeIdPropertyName() + " on boundary");
            return false;
        }
        if (!p.containsKey("r") || !p.containsKey("x")) {
            invalid("No r,x attributes");
            return false;
        }
        return true;
    }
}
