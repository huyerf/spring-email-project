package com.example.email.controller;

import com.example.email.model.InboxMail;
import com.example.email.service.AuthService;
import com.example.email.service.MailService;
import com.example.email.session.MailSession;

import jakarta.mail.Store;

//import org.apache.tomcat.util.http.parser.MediaType;
//import com.example.email.controller.MailWebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpHeaders;


@RestController
@RequestMapping("/api/mail")
@CrossOrigin(origins = "*")
public class MailController {

    @Autowired private MailService mailService;
    @Autowired private AuthService authService;
    @Autowired private MailWebSocketController wsController;

    @PostMapping("/send")
public String send(
        @RequestParam String username,
        @RequestParam String to,
        @RequestParam String subject,
        @RequestParam String body,
        @RequestPart(required = false) MultipartFile file
) {
    MailSession session = authService.getSession(username);
    if (session == null) return "not logged in";

    try {
        mailService.send(session.getUsername(), session.getPassword(), to, subject, body, file);

        // 💤 Đợi thư tới hòm thư người nhận (tránh lag)
        Thread.sleep(1000);

        MailSession receiverSession = authService.getSession(to);

        Store store = receiverSession.getStore();
        List<InboxMail> inbox = mailService.getInboxMails(store, to);

        // 🔍 Tìm thư trùng subject + content + (nếu có file thì trùng tên file)
        for (int i = inbox.size() - 1; i >= 0; i--) {
            InboxMail mail = inbox.get(i);

            boolean match = mail.getSubject().equals(subject)
                    && mail.getContent().trim().equals(body.trim());

            if (file != null) {
                match = match && mail.getAttachments() != null
                        && mail.getAttachments().contains(file.getOriginalFilename());
            }

            if (match) {
                wsController.sendMailToUser(to, mail);
                return "sent";
            }
        }

        return "fail: không tìm thấy thư vừa gửi";

    } catch (Exception e) {
        e.printStackTrace();
        return "fail: " + e.getMessage();
    }
}


    @GetMapping("/inbox")
    public List<InboxMail> inbox(@RequestParam String username) throws Exception {
        MailSession session = authService.getSession(username);
        if (session == null) return Collections.emptyList();
        return mailService.getInboxMails(session.getStore(), session.getEmail());
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadAttachment(
        @RequestParam String username,
        @RequestParam int messageIndex,
        @RequestParam String filename
        ) {
    MailSession session = authService.getSession(username);
    if (session == null) return ResponseEntity.status(401).build();

    try {
        byte[] fileData = mailService.downloadAttachment(session.getStore(), messageIndex, filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileData);
    } catch (Exception e) {
        return ResponseEntity.status(500).body(("Download lỗi: " + e.getMessage()).getBytes());
    }
    }
    @GetMapping("/search")
    public List<InboxMail> searchInbox(
        @RequestParam String username,
        @RequestParam String keyword,
        @RequestParam(required = false) String from 
    ) throws Exception {
    MailSession session = authService.getSession(username);
    if (session == null) return Collections.emptyList();

    return mailService.searchInboxMails(session.getStore(), session.getEmail(), keyword, from);
    }
 

}
