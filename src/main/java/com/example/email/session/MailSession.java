package com.example.email.session;

import jakarta.mail.Store;

public class MailSession {
    private String username;
    private String password;
    private Store store;
    private String email; // địa chỉ email đầy đủ, ví dụ bob@fake.local

    public MailSession(String username, String password, Store store) {
        this.username = username;
        this.password = password;
        this.store = store;
        this.email = username.contains("@") ? username : username + "@fake.local";
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Store getStore() {
        return store;
    }

    public String getEmail() {
        return email;
    }
}
