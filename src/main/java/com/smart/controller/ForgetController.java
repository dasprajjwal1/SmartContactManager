package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.services.emailService;
import com.smart.dao.userRepository;
import com.smart.entities.User;
import com.smart.helper.message;

@Controller
public class ForgetController {
	// generating 4 digits otp
	Random random = new Random(1000);

	@Autowired
	private emailService emailservice;

	@Autowired
	private userRepository userrepo;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	// email id form open handller
	@GetMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}

	// processing the email for the otp
	@PostMapping("/sendotp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) {

		System.out.println("Email " + email);
		int otp = random.nextInt(99999);
		System.out.println("generated otp " + otp);

		String Subject = "OTP from SCM(smartContactManager)";
		String message = "<h1> OTP = " + otp + "</h1>";
		String from = "pessimestic365@gmail.com";
		// code to send the otp to the email....
		boolean flag = this.emailservice.SendEmail(Subject, email, from, message);

		if (!flag) {
			session.setAttribute("messagee", new message("Something went wrong!!", "danger"));
			System.out.println("something went wrong");
			return "redirect:/forget";
		} else {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			System.out.println("send successfully!!");
			session.setAttribute("messagee", new message("we have send the OTP to your gmail...", "primary"));
			return "verify_otp";
		}

	}

	@PostMapping("/processotp")
	public String VerifyOTP(@RequestParam("otp") Integer formotp, HttpSession session) {
		Object otp1 = (int) session.getAttribute("myotp");
		Object email = (String) session.getAttribute("email");
		System.out.println(email);
		System.out.println(otp1);

		if (formotp.equals(otp1)) {

			// redircet to the new password page
			User user = this.userrepo.getUserByUsername(email);
			if (user == null) {

				// send error message form
				session.setAttribute("messagee", new message("User email does not exist!! ", "danger"));
				return "redirect:/forgot";
			} else {
				// send change password
				session.setAttribute("user", user);
				return "new_user_password";
			}

		} else {

			System.out.println("wrong otp!!");
			session.setAttribute("messagee", new message("Wrong OTP!!", "danger"));
			return "redirect:/forgot";
		}

	}

	@PostMapping("/changepassword")
	public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {

		if (!newpassword.isEmpty()) {

			Object email = (String) session.getAttribute("email");
			User user = this.userrepo.getUserByUsername(email);
			user.setPassword(this.passwordEncoder.encode(newpassword));
			this.userrepo.save(user);
			System.out.println("password is changed successfully!!");
			session.setAttribute("message",
					new message("password is changed successfully!! login with new password..", "primary"));
			return "login";
		} else {
			session.setAttribute("messagee", new message("something went wrong!! ", "danger"));
			return "redircet:/forgot";
		}

	}
}
