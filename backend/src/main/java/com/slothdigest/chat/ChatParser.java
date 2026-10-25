package com.slothdigest.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses WhatsApp "Export chat" text files. Handles both common layouts:
 *   12/10/25, 9:41 pm - Riya: see you at 7
 *   [12/10/25, 9:41:07 PM] Riya: see you at 7
 * Multi-line messages are joined; media placeholders are dropped.
 */
@Component
public class ChatParser {

    private static final Pattern LINE = Pattern.compile(
            "^\\[?(\\d{1,2}[/.]\\d{1,2}[/.]\\d{2,4}),?\\s+(\\d{1,2}:\\d{2}(?::\\d{2})?(?:\\s?[APap][Mm])?)\\]?\\s*-?\\s*([^:]{1,60}):\\s(.*)$");

    private final int maxChars;

    public ChatParser(@Value("${app.digest.max-chars}") int maxChars) {
        this.maxChars = maxChars;
    }

    public ParsedChat parse(String raw) {
        List<String> messages = new ArrayList<>();
        StringBuilder current = null;
        int count = 0;

        for (String rawLine : raw.split("\\R")) {
            String line = rawLine
                    .replace('\u202f', ' ').replace('\u00a0', ' ')
                    .replace("\u200e", "").replace("\u200f", "").strip();
            Matcher m = LINE.matcher(line);
            if (m.matches()) {
                if (current != null) messages.add(current.toString());
                String text = m.group(4).strip();
                if (text.toLowerCase().endsWith("omitted") || text.isEmpty()) {
                    current = null; // skip "<Media omitted>" and empty lines
                } else {
                    current = new StringBuilder(m.group(3).strip()).append(": ").append(text);
                    count++;
                }
            } else if (current != null && !line.isEmpty()) {
                current.append(' ').append(line); // continuation of a multi-line message
            }
        }
        if (current != null) messages.add(current.toString());

        String transcript = String.join("\n", messages);
        if (transcript.length() > maxChars) {
            // keep the most recent part, starting at a clean line boundary
            String tail = transcript.substring(transcript.length() - maxChars);
            int firstNewline = tail.indexOf('\n');
            transcript = firstNewline >= 0 ? tail.substring(firstNewline + 1) : tail;
        }
        return new ParsedChat(count, transcript);
    }
}
