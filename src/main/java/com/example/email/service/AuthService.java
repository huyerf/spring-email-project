package com.example.email.service;

import com.example.email.session.MailSession;
import jakarta.mail.Session;
import jakarta.mail.Store;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final ConcurrentHashMap<String, MailSession> sessions = new ConcurrentHashMap<>();

    public boolean login(String username, String password) {
        try {
            Properties props = new Properties();
            props.put("mail.store.protocol","imap");
            props.put("mail.imap.host", "192.168.71.100");
            props.put("mail.imap.port", "143");

            Session session = Session.getInstance(props);
            Store store = session.getStore("imap");
            store.connect(username, password);

            String normalizedEmail = username.contains("@") ? username : username + "@fake.local";
            sessions.put(normalizedEmail, new MailSession(normalizedEmail, password, store));

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public MailSession getSession(String username) {
    String normalizedEmail = username.contains("@") ? username : username + "@fake.local";
    return sessions.get(normalizedEmail);
    }

    public void logout(String username) {
    String normalizedEmail = username.contains("@") ? username : username + "@fake.local";
    sessions.remove(normalizedEmail);
    }

}