package com.example.email.model;

import jakarta.persistence.*;

@Entity
@Table(name = "login_mapping")
public class Login {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String laoEmail;
    private String username;
    private String password;

    public Login() {}

    public Login(String laoEmail, String username, String password) {
        this.laoEmail = laoEmail;
        this.username = username;
        this.password = password;
    }

    // Getters & Setters
    public Long getId() { return id; }

    public String getLaoEmail() { return laoEmail; }
    public void setLaoEmail(String laoEmail) { this.laoEmail = laoEmail; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
