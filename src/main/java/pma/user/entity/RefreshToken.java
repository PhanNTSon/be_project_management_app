package pma.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "RefreshToken")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "TokenHash", nullable = false, length = 255)
    private String tokenHash;

    @Column(name = "ExpiryDate", nullable = false)
    private LocalDateTime expiryDate;

    @ManyToOne
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    public RefreshToken(User user, String tokenHash, LocalDateTime expiryDate) {

        validateUser(user);
        validateTokenHash(tokenHash);
        validateExpiry(expiryDate);

        this.user = user;
        this.tokenHash = tokenHash;
        this.expiryDate = expiryDate;
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
    }

    private void validateTokenHash(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("Token hash cannot be empty");
        }
    }

    private void validateExpiry(LocalDateTime expiry) {
        if (expiry == null || expiry.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Expiry date must be in the future");
        }
    }

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }
}
