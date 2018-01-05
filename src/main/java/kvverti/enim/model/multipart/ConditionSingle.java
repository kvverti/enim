package kvverti.enim.model.multipart;

import java.util.Map;

import net.minecraft.block.properties.IProperty;

import kvverti.enim.entity.state.RenderState;

/** A condition based on a single property-value pair, such as {@code "up": true} */
public class ConditionSingle extends Condition {

    private final String key;
    private final String value;

    /* Used in ConditionAnd and Condition */
    ConditionSingle(String k, String v) { key = k; value = v; }

    @Override
    @SuppressWarnings("unchecked")
    public boolean fulfills(RenderState state) {

        for(Map.Entry<IProperty<?>, Comparable<?>> entry : state.getProperties().entrySet()) {

            @SuppressWarnings("rawtypes")
            IProperty prop = entry.getKey();
            if(prop.getName().equals(key) && prop.getName(entry.getValue()).equals(value))
                return true;
        }
        return false;
    }
}