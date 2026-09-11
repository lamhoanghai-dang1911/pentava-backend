package com.nqt.auth.infrastructure.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã xác thực tài khoản PENTAVA");
        message.setText("Chào bạn,\n\nMã OTP xác thực tài khoản của bạn là: " + otpCode +
                "\nMã này có hiệu lực trong vòng 5 phút.\n\nVui lòng không chia sẻ mã này cho bất kỳ ai.");
        mailSender.send(message);
    }
}