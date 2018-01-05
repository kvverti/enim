package kvverti.enim.model.multipart;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.Map;

import com.google.gson.*;

import kvverti.enim.entity.state.RenderState;

/** A structure representing the logical and over conditions. Conditions may be nested. */
public class ConditionAnd extends Condition {

    /** The conditions that must be fulfilled for this to be fulfilled */
    private final Condition[] conditions;

    private ConditionAnd(Condition[] c) { conditions = c; }

    @Override
    public boolean fulfills(RenderState state) {

        for(Condition c : conditions)
            if(!c.fulfills(state))
                return false;
        return true;
    }

    /**
     * ConditionAnd is in the form of a JsonObject. i.e. { "key": value, "...": ... }
     */
    static ConditionAnd deserialize(JsonElement json, JsonDeserializationContext context) {

        assert json.isJsonObject() : json;
        Set<Map.Entry<String, JsonElement>> entrySet = json.getAsJsonObject().entrySet();
        if(entrySet.isEmpty())
            throw new JsonParseException("Conditions must be non-empty");
        assert entrySet.size() > 1 : entrySet;
        Condition[] c = new Condition[entrySet.size()];
        int i = 0;
        for(Map.Entry<String, JsonElement> entry : entrySet) {

            //it's an OR
            if(entry.getValue().isJsonArray())
                c[i] = ConditionOr.deserialize(entry.getValue(), context);
            //it's a single
            else
                c[i] = new ConditionSingle(entry.getKey(), entry.getValue().getAsString());
            i++;
        }
        return new ConditionAnd(c);
    }
}