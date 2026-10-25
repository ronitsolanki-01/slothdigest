package com.slothdigest.chat;

/** A chat boiled down to "Sender: text" lines, trimmed to fit the model's context. */
public record ParsedChat(int messageCount, String transcript) {}
