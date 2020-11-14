package gtanks.users.karma;

import javax.persistence.*;
import java.util.Date;

@Entity
@org.hibernate.annotations.Entity
@Table(
    name = "karma"
)
public class Karma {
    @Id
    @GeneratedValue(
        strategy = GenerationType.IDENTITY
    )
    @Column(
        name = "idkarma",
        nullable = false
    )
    private Long id;
    @Column(
        name = "userid",
        nullable = false
    )
    private String userId;
    @Column(
        name = "banner_chat_user_id",
        nullable = true
    )
    private String whoBannedChatId;
    @Column(
        name = "banner_game_user_id",
        nullable = true
    )
    private String whoBannedGameId;
    @Column(
        name = "reason_for_chat_ban",
        nullable = true
    )
    private String reasonChatBan;
    @Column(
        name = "reason_for_game_ban",
        nullable = true
    )
    private String reasonGameBan;
    @Column(
        name = "chat_banned"
    )
    private boolean chatBanned;
    @Column(
        name = "game_banned"
    )
    private boolean gameBlocked;
    @Column(
        name = "chat_banned_before"
    )
    private Date chatBannedBefore;
    @Column(
        name = "game_banned_before"
    )
    private Date gameBlockedBefore;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReasonChatBan() {
        return this.reasonChatBan;
    }

    public void setReasonChatBan(String reasonChatBan) {
        this.reasonChatBan = reasonChatBan;
    }

    public String getReasonGameBan() {
        return this.reasonGameBan;
    }

    public void setReasonGameBan(String reasonGameBan) {
        this.reasonGameBan = reasonGameBan;
    }

    public String getWhoBannedChatId() {
        return this.whoBannedChatId;
    }

    public void setWhoBannedChatId(String whoBannedChatId) {
        this.whoBannedChatId = whoBannedChatId;
    }

    public String getWhoBannedGameId() {
        return this.whoBannedGameId;
    }

    public void setWhoBannedGameId(String whoBannedGameId) {
        this.whoBannedGameId = whoBannedGameId;
    }

    public boolean isChatBanned() {
        return this.chatBanned;
    }

    public void setChatBanned(boolean chatBanned) {
        this.chatBanned = chatBanned;
    }

    public boolean isGameBlocked() {
        return this.gameBlocked;
    }

    public void setGameBlocked(boolean gameBlocked) {
        this.gameBlocked = gameBlocked;
    }

    public Date getChatBannedBefore() {
        return this.chatBannedBefore;
    }

    public void setChatBannedBefore(Date chatBannedBefore) {
        this.chatBannedBefore = chatBannedBefore;
    }

    public Date getGameBlockedBefore() {
        return this.gameBlockedBefore;
    }

    public void setGameBlockedBefore(Date gameBlockedBefore) {
        this.gameBlockedBefore = gameBlockedBefore;
    }

    public void clearChatKarma() {
        this.chatBanned = false;
        this.chatBannedBefore = null;
        this.reasonChatBan = null;
    }

    public void clearBlockKarma() {
        this.gameBlocked = false;
        this.gameBlockedBefore = null;
        this.reasonGameBan = null;
    }
}
