package pma.user.entity;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "User")
@Getter
@NoArgsConstructor
public class User {
    @Id
    @Column(name = "UserId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "Email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "Username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "Password", nullable = false, length = 500)
    private String password;

    @Column(name = "FullName", length = 255)
    private String fullName;

    @Column(name = "IsActive", nullable = false)
    private boolean isActive;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user")
    private Set<UserRole> userRoles;

    public User(String email, String username, String password, String fullName) {

        validateEmail(email);
        validatePassword(password);
        validateUsername(username);

        this.email = email;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    private void validateEmail(String email){
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private void validateUsername(String username){
        if (username == null || username.length() < 3 || username.length() > 100) {
            throw new IllegalArgumentException("Username must be between 3 and 100 characters");
        }
    }

    private void validatePassword(String password){
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
    }

    public void changeEmail(String newEmail){
        validateEmail(newEmail);
        this.email = newEmail;
    }

    public void changePassword(String newPassword){
        validatePassword(newPassword);
        this.password = newPassword;
    }

    public void changeFullName(String newFullName){
        this.fullName = newFullName;
    }

    public void deactivate(){
        this.isActive = false;
    }

    public void activate(){
        this.isActive = true;
    }

    public void assignRole(Role role){
        UserRole userRole = new UserRole(this, role);
        this.userRoles.add(userRole);
    }
}
