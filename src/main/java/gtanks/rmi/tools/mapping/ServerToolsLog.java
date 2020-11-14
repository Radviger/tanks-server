package gtanks.rmi.tools.mapping;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@org.hibernate.annotations.Entity
@Table(
    name = "tools_log"
)
public class ServerToolsLog implements Serializable {
    private static final long serialVersionUID = -6487154273879506245L;
}
