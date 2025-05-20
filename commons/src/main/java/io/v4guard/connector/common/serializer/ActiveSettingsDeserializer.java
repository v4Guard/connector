package io.v4guard.connector.common.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.v4guard.connector.api.socket.Addon;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.socket.DefaultActiveSettings;
import io.v4guard.connector.common.socket.NameValidator;
import io.v4guard.connector.common.socket.listener.DefaultAddon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class ActiveSettingsDeserializer extends JsonDeserializer<DefaultActiveSettings> {

    private final ObjectMapper objectMapper = CoreInstance.get().getObjectMapper();

    @Override
    public DefaultActiveSettings deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JacksonException {
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode rootNode = codec.readTree(jsonParser);

        DefaultActiveSettings defaultActiveSettings = new DefaultActiveSettings();

        Iterator<String> rootKeys = rootNode.fieldNames();
        while (rootKeys.hasNext()) {
            String rootKey = rootKeys.next();
            if (!rootKey.equals("messages") && !rootKey.equals("addons") && !rootKey.equals("privacy") && !rootKey.equals("nameValidator")) {
                defaultActiveSettings.getGeneral().put(rootKey, rootNode.get(rootKey).asBoolean());
                continue;
            }

            if (rootKey.equals("messages")) {
                JsonNode messageNode = rootNode.get("messages");
                for (Iterator<String> it = messageNode.fieldNames(); it.hasNext(); ) {
                    String key = it.next();
                    defaultActiveSettings.getMessages().put(key, objectMapper.convertValue(
                            messageNode.get(key),
                            new TypeReference<ArrayList<String>>() {})
                    );
                }
                continue;
            }

            if (rootKey.equals("addons")) {
                JsonNode activeAddnos = rootNode.get("addons");
                for (Iterator<String> it = activeAddnos.fieldNames(); it.hasNext(); ) {
                    String key = it.next();
                    defaultActiveSettings.getActiveAddons().put(key, objectMapper.convertValue(activeAddnos.get(key), DefaultAddon.class));
                }
                continue;
            }

            if (rootKey.equals("privacy")) {
                JsonNode privacyNode = rootNode.get("privacy");
                for (Iterator<String> it = privacyNode.fieldNames(); it.hasNext(); ) {
                    String key = it.next();
                    defaultActiveSettings.getPrivacySettings().put(key, privacyNode.get(key).asBoolean());
                }
                continue;
            }

            JsonNode nameValidatorNode = rootNode.get("nameValidator");

            defaultActiveSettings.setNameValidator(new NameValidator(
                    nameValidatorNode.get("enabled").asBoolean(), nameValidatorNode.get("regex").asText()
            ));
        }

        return defaultActiveSettings;
    }
}
