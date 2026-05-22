package com.example.ecommerce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 发送订单确认邮件
     */
    public void sendOrderConfirmation(String toEmail, String toName,
                                       String orderNo, String totalAmount) {
        String subject = "订单确认 - " + orderNo;
        String body = String.format(
                "尊敬的 %s：\n\n" +
                "您的订单 %s 已创建成功！\n" +
                "订单金额：¥%s\n" +
                "请尽快完成付款。\n\n" +
                "感谢您的购买！\n" +
                "电商平台",
                toName, orderNo, totalAmount);

        sendEmail(toEmail, subject, body);
    }

    /**
     * 发送付款确认邮件
     */
    public void sendPaymentConfirmation(String toEmail, String toName,
                                         String orderNo, String totalAmount) {
        String subject = "付款确认 - " + orderNo;
        String body = String.format(
                "尊敬的 %s：\n\n" +
                "您的订单 %s 已付款成功！\n" +
                "付款金额：¥%s\n" +
                "我们将尽快为您发货。\n\n" +
                "感谢您的购买！\n" +
                "电商平台",
                toName, orderNo, totalAmount);

        sendEmail(toEmail, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("========== 模拟邮件 ==========");
            log.info("收件人: {}", to);
            log.info("主题: {}", subject);
            log.info("内容:\n{}", body);
            log.info("==============================");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("邮件已发送至: {}", to);
    }
}
