package com.slothdigest.digest;

import java.util.List;

/** The structured output we ask the model to produce (Spring AI maps JSON onto this record). */
public record DigestResult(String summary, List<String> actionItems, List<String> mentions) {}
