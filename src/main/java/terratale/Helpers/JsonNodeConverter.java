package terratale.Helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonNodeConverter {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Convierte un JsonNode a String para almacenar en la base de datos
     */
    public static String toString(JsonNode jsonNode) {
        try {
            return jsonNode == null ? null : mapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error serializando JSON", e);
        }
    }

    /**
     * Convierte un String de la base de datos a JsonNode
     */
    public static JsonNode fromString(String jsonString) {
        try {
            return jsonString == null || jsonString.isEmpty() ? null : mapper.readTree(jsonString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parseando JSON: " + jsonString, e);
        }
    }
}
