package gtanks.rmi.payments.mapping;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@org.hibernate.annotations.Entity
@Table(
    name = "payment"
)
public class Payment implements Serializable {
    private static final long serialVersionUID = -2764032649003874382L;
    @Id
    @GeneratedValue(
        strategy = GenerationType.IDENTITY
    )
    @Column(
        name = "id",
        unique = true,
        nullable = false
    )
    private long id;
    @Column(
        name = "id_payment",
        unique = true,
        nullable = false
    )
    private long idPayment;
    @Column(
        name = "summ",
        unique = true,
        nullable = false
    )
    private int sum;
    @Column(
        name = "status",
        unique = true,
        nullable = false
    )
    private byte status;
    @Column(
        name = "nickname",
        unique = true,
        nullable = false
    )
    private String nickname;
    @Column(
        name = "date",
        unique = true,
        nullable = false
    )
    private Date date;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdPayment() {
        return this.idPayment;
    }

    public void setIdPayment(long idPayment) {
        this.idPayment = idPayment;
    }

    public int getSum() {
        return this.sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public byte getStatus() {
        return this.status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
