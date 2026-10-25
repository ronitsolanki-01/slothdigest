package com.slothdigest.chat;

import com.slothdigest.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ChatExportRepository extends JpaRepository<ChatExport, Long> {
    List<ChatExport> findByOwnerOrderByUploadedAtDesc(AppUser owner);
    List<ChatExport> findByStatusIn(Collection<ChatExport.Status> statuses);
    List<ChatExport> findByOwnerAndStatusIn(AppUser owner, Collection<ChatExport.Status> statuses);
}
