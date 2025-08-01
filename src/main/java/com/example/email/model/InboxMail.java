package com.example.email.model;

import java.util.List;

public class InboxMail {
    private String from;
    private String subject;
    private String content;
    private String date;

    // 🔸 Thêm mới
    private int messageIndex; // Vị trí trong INBOX
    private List<String> attachments; // Danh sách file đính kèm

    public InboxMail() {}

    public InboxMail(String from, String subject, String content, String date) {
        this.from = from;
        this.subject = subject;
        this.content = content;
        this.date = date;
    }
    


    // 🔸 Getter / Setter mặc định
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    // 🔸 Mới thêm
    public int getMessageIndex() { return messageIndex; }
    public void setMessageIndex(int messageIndex) { this.messageIndex = messageIndex; }

    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }
}
