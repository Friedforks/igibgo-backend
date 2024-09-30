package cloud.igibgo.igibgobackend.entity.FUser;

import jakarta.persistence.*;

@Entity
@Table(name = "collection")
public class Collection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long collectionId;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "author")
    public FUser fUser;

    public String collectionName;
}
