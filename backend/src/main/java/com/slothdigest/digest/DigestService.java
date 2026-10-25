package com.slothdigest.digest;

import com.slothdigest.chat.ChatExport;
import com.slothdigest.chat.ChatExportRepository;
import com.slothdigest.chat.ChatParser;
import com.slothdigest.chat.ParsedChat;
import com.slothdigest.user.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigestService {

    // FAILED exports are retried on the next run (e.g. Ollama was not running yet)
    private static final Set<ChatExport.Status> TO_PROCESS =
            EnumSet.of(ChatExport.Status.PENDING, ChatExport.Status.FAILED);

    private final ChatExportRepository exports;
    private final DigestRepository digests;
    private final ChatParser parser;
    private final DigestAi ai;

    public int processPendingFor(AppUser user) {
        return processAll(exports.findByOwnerAndStatusIn(user, TO_PROCESS));
    }

    public int processAllPending() {
        return processAll(exports.findByStatusIn(TO_PROCESS));
    }

    private int processAll(List<ChatExport> pending) {
        int ok = 0;
        for (ChatExport export : pending) {
            if (process(export)) ok++;
        }
        return ok;
    }

    private boolean process(ChatExport export) {
        try {
            ParsedChat chat = parser.parse(export.getContent());
            if (chat.messageCount() == 0) {
                throw new IllegalArgumentException("No WhatsApp-style messages found in this file");
            }
            DigestResult result = ai.summarize(
                    export.getChatName(), export.getOwner().getDisplayName(), chat.transcript());

            Digest digest = new Digest();
            digest.setOwner(export.getOwner());
            digest.setChatName(export.getChatName());
            digest.setSummary(result.summary());
            digest.setActionItems(new ArrayList<>(result.actionItems() == null ? List.of() : result.actionItems()));
            digest.setMentions(new ArrayList<>(result.mentions() == null ? List.of() : result.mentions()));
            digest.setMessageCount(chat.messageCount());
            digests.save(digest);

            export.setStatus(ChatExport.Status.DONE);
            export.setError(null);
            exports.save(export);
            return true;
        } catch (Exception e) {
            log.warn("Could not digest export {}: {}", export.getId(), e.getMessage());
            export.setStatus(ChatExport.Status.FAILED);
            String msg = String.valueOf(e.getMessage());
            export.setError(msg.length() > 490 ? msg.substring(0, 490) : msg);
            exports.save(export);
            return false;
        }
    }
}
