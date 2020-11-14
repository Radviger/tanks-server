package gtanks.main.netty.blackip;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@org.hibernate.annotations.Entity
@Table(
    name = "black_ips"
)
public class BlackIP implements Serializable {
    private static final long serialVersionUID = 4292520390627573470L;
    @Id
    @GeneratedValue(
        strategy = GenerationType.IDENTITY
    )
    @Column(
        name = "idblack_ips",
        unique = true,
        nullable = false
    )
    private long id;
    @Column(
        name = "ip",
        unique = false,
        nullable = false
    )
    private String ip;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
