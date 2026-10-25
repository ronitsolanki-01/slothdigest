package com.slothdigest.digest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** The "lazy" part: summaries get written while you sleep. */
@Component
@RequiredArgsConstructor
@Slf4j
public class DigestScheduler {

    private final DigestService service;

    @Scheduled(cron = "${app.digest.cron}")
    public void runNightly() {
        int done = service.processAllPending();
        log.info("Nightly digest finished, {} chat(s) summarized", done);
    }
}
