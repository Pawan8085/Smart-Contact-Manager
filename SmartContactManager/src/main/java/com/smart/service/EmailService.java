package com.smart.service;

import java.util.Properties;

import org.springframework.stereotype.Service;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {
	
public boolean send(String to, String from, String subject, String text) {
		
		boolean flag = false;
		
		
		// logic 
		// smtp properties 
		Properties properties = new Properties();
		properties.put("mail.smtp.auth", true);
		properties.put("mail.smtp.starttls.enable", true);
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		
		String userName = "username";
		String password = "password";
		
		// session
		
		Session session = Session.getInstance(properties, new Authenticator() {
			
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				
				return new PasswordAuthentication(userName, password);
			}
		});
		
		
		try {
			
			Message message = new MimeMessage(session);
			
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setFrom(new InternetAddress(from));
			message.setSubject(subject);
//			message.setText(text);
			
			message.setContent(text, "text/html");
			
			Transport.send(message);
			flag = true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		

		
		return flag;
	}
}
