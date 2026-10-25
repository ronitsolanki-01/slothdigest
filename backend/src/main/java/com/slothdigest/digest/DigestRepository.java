package com.slothdigest.digest;

import com.slothdigest.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DigestRepository extends JpaRepository<Digest, Long> {
    List<Digest> findByOwnerOrderByCreatedAtDesc(AppUser owner);
}
