package com.nqt.auth.infrastructure.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;

        // ➕ THÊM 2 DÒNG DEBUG TẠM Ở ĐÂY
        System.out.println(">>> MAIL_USERNAME đang dùng: [" + System.getenv("MAIL_USERNAME") + "]");
        System.out.println(">>> MAIL_PASSWORD length: " + (System.getenv("MAIL_PASSWORD") != null ? System.getenv("MAIL_PASSWORD").length() : "NULL"));
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        System.out.println(">>> BẮT ĐẦU GỬI EMAIL TỚI: " + toEmail);
        sendOtpEmail(toEmail, otpCode, "Mã xác thực tài khoản PENTAVA",
                "Xác thực tài khoản", "Đây là mã OTP để xác thực tài khoản của bạn.");
    }

    public void sendResetPasswordOtpEmail(String toEmail, String otpCode) {
        sendOtpEmail(toEmail, otpCode, "Mã đặt lại mật khẩu PENTAVA",
                "Đặt lại mật khẩu", "Đây là mã OTP để đặt lại mật khẩu tài khoản của bạn.");
    }

    private void sendOtpEmail(String toEmail, String otpCode, String subject, String heading, String description) {
        try {
            System.out.println(">>> ĐANG TẠO MIME MESSAGE"); // ➕ thêm dòng này
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(buildHtmlContent(heading, description, otpCode), true);

            System.out.println(">>> ĐANG GỬI QUA SMTP"); // ➕ thêm dòng này
            mailSender.send(mimeMessage);
            System.out.println(">>> GỬI THÀNH CÔNG"); // ➕ thêm dòng này
        } catch (Exception e) {  // ➕ đổi từ MessagingException thành Exception để bắt được cả lỗi khác
            System.out.println(">>> LỖI THẬT SỰ LÀ: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể gửi email OTP: " + e.getMessage(), e);
        }
    }

    private String buildHtmlContent(String heading, String description, String otpCode) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="margin:0; padding:0; background-color:#f4f5f7; font-family:'Segoe UI', Arial, sans-serif;">
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f5f7; padding:40px 0;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="480" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.06);">
                                    <tr>
                                        <td style="background:linear-gradient(135deg,#6C5CE7,#8E7CF1); padding:32px; text-align:center;">
                                            <h1 style="color:#ffffff; margin:0; font-size:24px; letter-spacing:1px;">PENTAVA</h1>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:32px;">
                                            <h2 style="color:#1a1a1a; font-size:20px; margin:0 0 8px;">%s</h2>
                                            <p style="color:#6b7280; font-size:14px; line-height:1.6; margin:0 0 24px;">%s</p>
                                            <div style="background-color:#f4f5f7; border-radius:10px; padding:20px; text-align:center; margin-bottom:24px;">
                                                <span style="font-size:32px; font-weight:700; letter-spacing:8px; color:#6C5CE7;">%s</span>
                                            </div>
                                            <p style="color:#6b7280; font-size:13px; line-height:1.6; margin:0;">
                                                Mã này có hiệu lực trong vòng <strong>5 phút</strong>.<br/>
                                                Vui lòng không chia sẻ mã này cho bất kỳ ai, kể cả nhân viên PENTAVA.
                                            </p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="background-color:#f9fafb; padding:20px 32px; text-align:center;">
                                            <p style="color:#9ca3af; font-size:12px; margin:0;">Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(heading, description, otpCode);
    }
}