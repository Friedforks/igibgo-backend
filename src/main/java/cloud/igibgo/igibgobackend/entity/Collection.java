package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "collection")
public class Collection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    String collectionId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "author")
    FUser fUser;
}
