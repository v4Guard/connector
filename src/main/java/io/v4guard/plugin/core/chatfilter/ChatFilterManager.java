package io.v4guard.plugin.core.chatfilter;

import java.util.Arrays;
import java.util.List;

public class ChatFilterManager {

    private List<String> allowedCommands = Arrays.asList(
        "/me",
        "/say",
        "/tell",
        "/whisper",
        "/reply",
        "/pm",
        "/message",
        "/msg",
        "/emsg",
        "/epm",
        "/etell",
        "/ewhisper",
        "/w",
        "/m",
        "/t",
        "/r"
    );

    public List<String> getAllowedCommands() {
        return allowedCommands;
    }

    public boolean canLookupMessage(String message){
        if(message.startsWith("/")){
            String[] split = message.split(" ");
            String command = split[0];
            return allowedCommands.contains(command);
        } else {
            return true;
        }
    }
}
