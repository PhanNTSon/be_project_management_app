package pma.user.entity;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "User")
@Getter
public class User {
    @Id
    @Column(name = "UserId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "Email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "Password", nullable = false, length = 500)
    private String password;

    @Column(name = "FullName", length = 255)
    private String fullName;

    @Column(name = "IsActive", nullable = false)
    private boolean isActive;

    @Column(name = "CreatedAt", nullable = false)
    private Date createdAt;
}
