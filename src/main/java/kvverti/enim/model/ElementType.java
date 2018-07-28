package kvverti.enim.model;

import java.lang.reflect.Type;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonParseException;

/** The types an element can take */
public enum ElementType {

    MODEL_BOX,
    ITEM,
    BLOCK;

    static class Deserializer implements JsonDeserializer<ElementType> {

        private static final ImmutableMap<String, ElementType> name2type = ImmutableMap.<String, ElementType>builder()
            .put("box", MODEL_BOX)
            .put("item", ITEM)
            .put("block", BLOCK)
            .build();
        
        @Override
        public ElementType deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

            String n = json.getAsString();
            ElementType res = name2type.get(n);
            if(res == null)
                throw new JsonParseException("Invalid element type " + n);
            return res;
        }
    }
}
