package com.slothdigest.web;

import com.slothdigest.chat.ChatExport;
import com.slothdigest.chat.ChatExportRepository;
import com.slothdigest.digest.Digest;
import com.slothdigest.digest.DigestRepository;
import com.slothdigest.digest.DigestService;
import com.slothdigest.user.AppUser;
import com.slothdigest.user.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DigestController {

    private final AppUserRepository users;
    private final ChatExportRepository exports;
    private final DigestRepository digests;
    private final DigestService digestService;

    public record ExportView(Long id, String chatName, String status, String error, Instant uploadedAt) {
        static ExportView of(ChatExport e) {
            return new ExportView(e.getId(), e.getChatName(), e.getStatus().name(), e.getError(), e.getUploadedAt());
        }
    }

    public record DigestView(Long id, String chatName, String summary, List<String> actionItems,
                             List<String> mentions, int messageCount, Instant createdAt) {
        static DigestView of(Digest d) {
            return new DigestView(d.getId(), d.getChatName(), d.getSummary(),
                    List.copyOf(d.getActionItems()), List.copyOf(d.getMentions()),
                    d.getMessageCount(), d.getCreatedAt());
        }
    }

    @PostMapping(path = "/chats", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ExportView upload(Authentication auth,
                             @RequestParam("file") MultipartFile file,
                             @RequestParam(value = "name", required = false) String name) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        if (content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The file is empty");
        }
        ChatExport export = new ChatExport();
        export.setOwner(currentUser(auth));
        export.setChatName(chatName(name, file.getOriginalFilename()));
        export.setContent(content);
        return ExportView.of(exports.save(export));
    }

    @GetMapping("/chats")
    public List<ExportView> listChats(Authentication auth) {
        return exports.findByOwnerOrderByUploadedAtDesc(currentUser(auth)).stream().map(ExportView::of).toList();
    }

    @GetMapping("/digests")
    public List<DigestView> listDigests(Authentication auth) {
        return digests.findByOwnerOrderByCreatedAtDesc(currentUser(auth)).stream().map(DigestView::of).toList();
    }

    /** Don't want to wait until 3 AM? Summarize this user's pending chats right now. */
    @PostMapping("/digests/run")
    public Map<String, Integer> runNow(Authentication auth) {
        return Map.of("processed", digestService.processPendingFor(currentUser(auth)));
    }

    private AppUser currentUser(Authentication auth) {
        return users.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
    }

    private static String chatName(String given, String filename) {
        if (given != null && !given.isBlank()) return given.strip();
        String base = filename == null ? "Untitled chat" : filename.replaceAll("\\.txt$", "");
        return base.replaceFirst("^WhatsApp Chat with ", "").strip();
    }
}
