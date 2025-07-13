package io.v4guard.connector.common.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.socket.settings.DefaultAddonSetting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class AddonDeserializer extends JsonDeserializer<DefaultAddonSetting> {

    private final ObjectMapper objectMapper = CoreInstance.get().getObjectMapper();

    @Override
    public DefaultAddonSetting deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JacksonException {
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode rootNode = codec.readTree(jsonParser);

        DefaultAddonSetting defaultAddonSetting = new DefaultAddonSetting();

        Iterator<String> rootKeys = rootNode.fieldNames();
        while (rootKeys.hasNext()) {
            String rootKey = rootKeys.next();

            switch (rootKey) {
                case "settings":
                    JsonNode settingsNode = rootNode.get("settings");
                    for (Iterator<String> it = settingsNode.fieldNames(); it.hasNext(); ) {
                        String key = it.next();
                        defaultAddonSetting.getSettings().put(key, settingsNode.get(key).asText());
                    }
                    break;
                case "messages":
                    JsonNode messagesNode = rootNode.get("messages");
                    for (Iterator<String> it = messagesNode.fieldNames(); it.hasNext(); ) {
                        String key = it.next();
                        defaultAddonSetting.getMessages().put(key, objectMapper.convertValue(
                                messagesNode.get(key),
                                new TypeReference<ArrayList<String>>() {}));
                    }
                    break;
                case "enabled":
                    defaultAddonSetting.setEnabled(rootNode.get("enabled").asBoolean());
                    break;
            }


        }



        return defaultAddonSetting;
    }
}
