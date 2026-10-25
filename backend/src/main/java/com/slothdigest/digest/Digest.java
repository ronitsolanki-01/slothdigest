package com.slothdigest.digest;

import com.slothdigest.user.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor
public class Digest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private AppUser owner;

    private String chatName;

    @Column(length = 4000)
    private String summary;

    // @OrderColumn makes these indexed lists (not "bags"), so two EAGER lists are fine
    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    @Column(length = 1000)
    private List<String> actionItems = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    @Column(length = 1000)
    private List<String> mentions = new ArrayList<>();

    private int messageCount;

    private Instant createdAt = Instant.now();
}
