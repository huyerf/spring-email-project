package com.example.email.controller;


import com.example.email.model.InboxMail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class MailWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendMailToUser(String username, InboxMail mail) {
        // Gửi đến đích riêng của user
        messagingTemplate.convertAndSend("/topic/inbox/" + username, mail);
    }
}