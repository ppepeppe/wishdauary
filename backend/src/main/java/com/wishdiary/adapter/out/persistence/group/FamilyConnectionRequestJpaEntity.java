package com.wishdiary.adapter.out.persistence.group;
import com.wishdiary.domain.group.FamilyConnectionRequest;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "family_connection_requests")
public class FamilyConnectionRequestJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long familyUserId;
    @Column(nullable = false) private Long childUserId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private FamilyConnectionRequest.Status status;
    @Column(nullable = false) private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    protected FamilyConnectionRequestJpaEntity() { }
    public FamilyConnectionRequestJpaEntity(FamilyConnectionRequest request) {
        id = request.id(); familyUserId = request.familyUserId(); childUserId = request.childUserId();
        status = request.status(); createdAt = request.createdAt(); processedAt = request.processedAt();
    }
    public FamilyConnectionRequest toDomain() {
        return new FamilyConnectionRequest(id, familyUserId, childUserId, status, createdAt, processedAt);
    }
}
