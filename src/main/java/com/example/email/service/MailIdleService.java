package com.example.email.service;

import com.example.email.model.InboxMail;
import com.example.email.session.MailSession;
import com.example.email.controller.MailWebSocketController;
import jakarta.mail.*;
import jakarta.mail.event.MessageCountAdapter;
import jakarta.mail.event.MessageCountEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sun.mail.imap.IMAPFolder;


@Service
public class MailIdleService {

    @Autowired
    private MailWebSocketController mailWebSocketController;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void startIdleListener(MailSession mailSession) {
        executor.submit(() -> {
            try {
                Store store = mailSession.getStore();
                Folder inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);

                inbox.addMessageCountListener(new MessageCountAdapter() {
                    @Override
                    public void messagesAdded(MessageCountEvent event) {
                        try {
                            for (Message message : event.getMessages()) {
                                InboxMail mail = new InboxMail();
                                mail.setSubject(message.getSubject());
                                mail.setFrom(message.getFrom()[0].toString());
                                mail.setContent(message.getContent().toString());

                                mailWebSocketController.sendMailToUser(mailSession.getUsername(), mail);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                // Vòng lặp IDLE
                while (true) {
                    if (!inbox.isOpen()) inbox.open(Folder.READ_ONLY);
                    ((IMAPFolder) inbox).idle(); // Cần javax.mail.internet và jakarta.mail.imap
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
