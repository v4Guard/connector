package io.v4guard.connector.platform.velocity.adapter;

import io.v4guard.connector.common.compatibility.ComponentAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;

public class VelocityComponentAdapter extends ComponentAdapter<Component> {

    public VelocityComponentAdapter() {}

    @Override
    public Component adapt(String incomingText) {
        Component text;

        if (hasLegacyCode(incomingText)) {
            text = super.legacyComponentSerializer.deserialize(incomingText);
        } else {
            text = super.miniMessage.deserialize(incomingText);
        }

        return text;
    }

    @Override
    public Component adapt(List<String> incomingText) {
        if (incomingText == null || incomingText.isEmpty()) {
            return Component.empty();
        }

        TextComponent.Builder builder = Component.text();
        boolean first = true;

        for (String line : incomingText) {
            if (line == null) continue;
            if (!first) {
                builder.append(Component.newline());
            }

            builder.append(this.adapt(line));
            first = false;
        }

        return builder.build();
    }

    @Override
    public Component adapt(String incomingText, boolean shouldDownsample) {
        Component text = this.adapt(incomingText);
        return shouldDownsample ? this.downsample(text) : text;
    }

    @Override
    public Component adapt(List<String> incomingText, boolean shouldDownsample) {
        Component text = this.adapt(incomingText);
        return shouldDownsample ? this.downsample(text) : text;
    }

    // So, older version that 1.16 on a kick event that might have gradients; it might not work as expected
    // Becoming albino.
    private Component downsample(Component c) {
        TextColor color = c.color();
        if (color != null && !(color instanceof NamedTextColor)) {
            c = c.color(NamedTextColor.nearestTo(color));
        }
        return c.children(c.children().stream().map(this::downsample).toList());
    }

}
