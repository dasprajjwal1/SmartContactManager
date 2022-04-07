package com.smart.services;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class emailService
{
	//method to send the email
	public boolean SendEmail(String subject ,String to ,String from,String message)
	{
		
		boolean flag = false ;
		//variable for gmail	
		String host= "smtp.gmail.com";
		
		//String from1 = "prajjwal.das.3@gmail.com";
		//String message1 ="this is the message";

		//get the system properties 
		Properties properties = System.getProperties();
		
		System.out.println("Properties " +properties);
		//setting important information to the properties 
		//host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		//step 1: to get the session object
		Session session  = Session.getInstance(properties,new javax.mail.Authenticator()
				{
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						
						return new PasswordAuthentication("youremail", "yourpassword");
					}
			
				});
		
		session.setDebug(true);
		
		//step2: compose the message[text,media etc]
		
		MimeMessage mimeMessage = new MimeMessage(session);
		
		//set from email
		try {
			
		mimeMessage.setFrom(from);
		
		//adding recipent
		mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		
		//adding subject
		mimeMessage.setSubject(subject);
		
		//adding text to message 
		//mimeMessage.setText(message);
		mimeMessage.setContent(message, "text/html");
		
		//step 3 send the message using transport class
		
		Transport.send(mimeMessage);
		flag = true ;	
		System.out.println("send successfully........");
		
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return flag;		
	}
	
}
