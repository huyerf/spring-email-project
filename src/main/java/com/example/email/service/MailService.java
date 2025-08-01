package com.example.email.service;

import com.example.email.model.InboxMail;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class MailService {

    public void send(String username, String password,
                     String to, String subject, String body,
                     MultipartFile file) throws Exception {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.smtp.host", "192.168.71.100");
        props.put("mail.smtp.port", "25");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        // Nếu có file đính kèm
        if (file != null && !file.isEmpty()) {
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body);

            MimeBodyPart filePart = new MimeBodyPart();
            filePart.setFileName(file.getOriginalFilename());
            DataSource source = new ByteArrayDataSource(file.getBytes(), file.getContentType());
            filePart.setDataHandler(new DataHandler(source));

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(filePart);

            message.setContent(multipart);
        } else {
            // Nếu không có file thì chỉ gửi text
            message.setText(body);
        }

        Transport.send(message);
    }

   public List<InboxMail> getInboxMails(Store store, String userEmail) throws Exception {
    Folder inbox = store.getFolder("INBOX");
    inbox.open(Folder.READ_ONLY);
    Message[] messages = inbox.getMessages();

    List<InboxMail> result = new ArrayList<>();

    for (int i = 0; i < messages.length; i++) {
        Message message = messages[i];
        Address[] recipients = message.getRecipients(Message.RecipientType.TO);
        boolean isToUser = false;

        if (recipients != null) {
            for (Address address : recipients) {
                if (address.toString().equalsIgnoreCase(userEmail)) {
                    isToUser = true;
                    break;
                }
            }
        }

        if (!isToUser) continue;

        String from = ((InternetAddress) message.getFrom()[0]).getAddress();
        String subject = message.getSubject();
        String dateStr = new SimpleDateFormat("HH:mm dd-MM-yyyy")
                .format(Optional.ofNullable(message.getSentDate()).orElse(new Date()));

        String content = "";
        List<String> attachments = new ArrayList<>();

        try {
            Object messageContent = message.getContent();

            if (messageContent instanceof String) {
                content = (String) messageContent;
            } else if (messageContent instanceof Multipart multipart) {
                StringBuilder contentBuilder = new StringBuilder();

                for (int j = 0; j < multipart.getCount(); j++) {
                    BodyPart part = multipart.getBodyPart(j);

                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        attachments.add(part.getFileName());
                    } else {
                        Object partContent = part.getContent();
                        if (partContent instanceof String) {
                            contentBuilder.append(partContent);
                        }
                    }
                }

                content = contentBuilder.toString();
            } else {
                content = "[Không đọc được nội dung]";
            }

        } catch (Exception e) {
            content = "[Không đọc được nội dung]";
        }

        // Tạo email và gán thông tin mới
        InboxMail mail = new InboxMail(from, subject, content, dateStr);
        mail.setMessageIndex(i + 1); // Email trong JavaMail bắt đầu từ 1
        mail.setAttachments(attachments);

        result.add(mail);
    }

    inbox.close(false);
    return result;
    }
    public byte[] downloadAttachment(Store store, int messageIndex, String filename) throws Exception {
    Folder inbox = store.getFolder("INBOX");
    inbox.open(Folder.READ_ONLY);

    Message message = inbox.getMessage(messageIndex);
    Object content = message.getContent();

    if (content instanceof Multipart multipart) {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) &&
                filename.equalsIgnoreCase(part.getFileName())) {

                return part.getInputStream().readAllBytes();
            }
        }
    }

    throw new Exception("Không tìm thấy file đính kèm");
    } 
    public List<InboxMail> searchInboxMails(Store store, String userEmail, String keyword, String fromFilter) throws Exception {
    Folder inbox = store.getFolder("INBOX");
    inbox.open(Folder.READ_ONLY);
    Message[] messages = inbox.getMessages();

    List<InboxMail> result = new ArrayList<>();

    for (int i = 0; i < messages.length; i++) {
        Message message = messages[i];
        Address[] recipients = message.getRecipients(Message.RecipientType.TO);
        boolean isToUser = false;

        if (recipients != null) {
            for (Address address : recipients) {
                if (address.toString().equalsIgnoreCase(userEmail)) {
                    isToUser = true;
                    break;
                }
            }
        }

        if (!isToUser) continue;

        String from = ((InternetAddress) message.getFrom()[0]).getAddress();
        String subject = message.getSubject();
        String dateStr = new SimpleDateFormat("HH:mm dd-MM-yyyy")
                .format(Optional.ofNullable(message.getSentDate()).orElse(new Date()));

        String content = "";
        List<String> attachments = new ArrayList<>();

        try {
            Object messageContent = message.getContent();

            if (messageContent instanceof String str) {
                content = str;
            } else if (messageContent instanceof Multipart multipart) {
                StringBuilder contentBuilder = new StringBuilder();
                for (int j = 0; j < multipart.getCount(); j++) {
                    BodyPart part = multipart.getBodyPart(j);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        attachments.add(part.getFileName());
                    } else {
                        Object partContent = part.getContent();
                        if (partContent instanceof String text) {
                            contentBuilder.append(text);
                        }
                    }
                }
                content = contentBuilder.toString();
            }
        } catch (Exception e) {
            content = "[Không đọc được nội dung]";
        }

        boolean matchKeyword =
                (subject != null && subject.toLowerCase().contains(keyword.toLowerCase())) ||
                (content != null && content.toLowerCase().contains(keyword.toLowerCase()));

        boolean matchFrom = (fromFilter == null || fromFilter.isEmpty() || from.equalsIgnoreCase(fromFilter));

        if (matchKeyword && matchFrom) {
            InboxMail mail = new InboxMail(from, subject, content, dateStr);
            mail.setMessageIndex(i + 1);
            mail.setAttachments(attachments);
            result.add(mail);
        }
    }

    return result;
}



}
