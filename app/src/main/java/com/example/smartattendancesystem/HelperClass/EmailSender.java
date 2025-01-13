package com.example.smartattendancesystem.HelperClass;

import android.os.AsyncTask;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailSender extends AsyncTask<String, Void, Boolean> {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL = "anchetalancerhenier024@gmail.com"; // Replace with your email
    private static final String PASSWORD = "psmcvppzvyfjrbux"; // Replace with your app password

    @Override
    protected Boolean doInBackground(String... params) {
        String recipientEmail = params[0];
        String otp = params[1];

        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        try {
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL, PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your OTP for Smart Attendance System");
            message.setText("Your OTP is: " + otp + "\nThis OTP will expire in 5 minutes.");

            Transport.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}