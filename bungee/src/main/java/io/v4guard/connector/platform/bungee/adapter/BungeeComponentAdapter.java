package io.v4guard.connector.platform.bungee.adapter;

import io.v4guard.connector.common.compatibility.ComponentAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.List;

public class BungeeComponentAdapter extends ComponentAdapter<BaseComponent[]> {

    private final BungeeComponentSerializer bungeeComponentSerializer;

    public BungeeComponentAdapter() {
        this.bungeeComponentSerializer = BungeeComponentSerializer.get();
    }

    @Override
    public BaseComponent[] adapt(String incomingText) {
        Component text;

        if (hasLegacyCode(incomingText)) {
            text = super.legacyComponentSerializer.deserialize(incomingText);
        } else {
            text = super.miniMessage.deserialize(incomingText);
        }

        return bungeeComponentSerializer.serialize(text);
    }

    @Override
    public BaseComponent[] adapt(List<String> incomingText) {
        if (incomingText == null || incomingText.isEmpty()) {
            return new BaseComponent[0];
        }

        TextComponent.Builder builder = Component.text();
        boolean first = true;

        for (String line : incomingText) {
            if (line == null) continue;
            if (!first) {
                builder.append(Component.newline());
            }


            builder.append(this.parse(line));
            first = false;
        }

        return bungeeComponentSerializer.serialize(builder.build());
    }

    @Override
    public BaseComponent[] adapt(String incomingText, boolean shouldDownsample) {
        if (!shouldDownsample) return this.adapt(incomingText);

        Component text;
        if (hasLegacyCode(incomingText)) {
            text = super.legacyComponentSerializer.deserialize(incomingText);
        } else {
            text = super.miniMessage.deserialize(incomingText);
        }

        return BungeeComponentSerializer.legacy().serialize(text);
    }

    // yes, we have duplicates. Fuck off intelij!
    @Override
    public BaseComponent[] adapt(List<String> incomingText, boolean shouldDownsample) {
        if (!shouldDownsample) return this.adapt(incomingText);

        if (incomingText == null || incomingText.isEmpty()) {
            return new BaseComponent[0];
        }

        TextComponent.Builder builder = Component.text();
        boolean first = true;

        for (String line : incomingText) {
            if (line == null) continue;
            if (!first) {
                builder.append(Component.newline());
            }


            builder.append(this.parse(line));
            first = false;
        }

        return BungeeComponentSerializer.legacy().serialize(builder.build());
    }

    private Component parse(String incomingText) {
        if (hasLegacyCode(incomingText)) {
            return super.legacyComponentSerializer.deserialize(incomingText);
        } else {
            return super.miniMessage.deserialize(incomingText);
        }
    }
}

