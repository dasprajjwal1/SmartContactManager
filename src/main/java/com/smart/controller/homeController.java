package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.dao.userRepository;
import com.smart.entities.User;
import com.smart.helper.message;

@Controller
public class homeController {
	@Autowired
	private userRepository userRepo;

	@Autowired
	private BCryptPasswordEncoder passwordencoder;

	@RequestMapping(path = "/")
	public String home(Model model) {
		model.addAttribute("title", "smart contact manager");
		return "home";
	}

	@RequestMapping(path = "/about")
	public String about(Model model) {
		model.addAttribute("title", "About -smart contact manager");
		return "about";
	}

	@RequestMapping(path = "/signup")
	public String signUp(Model model) {
		model.addAttribute("title", "signUp -smart contact manager");
		model.addAttribute("user", new User());

		return "signup";
	}

	// handling the new user
	@RequestMapping(path = "/do_register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			BindingResult result, HttpSession session) {
		// @valid is used for the server side validation

		try {
			if (!agreement) {
				System.out.println("terms and conditions are not applied");
				throw new Exception("terms and conditions are not applied");
			}
			if (result.hasErrors()) {
				System.out.println("ERRORS " + result.toString());
				model.addAttribute("user", user);
				return "signup";
			}

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("deafult.png");
			user.setPassword(passwordencoder.encode(user.getPassword()));

			User result1 = this.userRepo.save(user);
			System.out.println(result1);
			model.addAttribute("user", new User()); // if the user is registered succesfully then blank user is
													// transfered.
			session.setAttribute("message", new message("successfully Registered!", "alert-success"));

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new message("something went wrong " + e.getMessage(), "alert-danger"));
			return "signup";
		}
		return "signup";
	}
	//handling custom login

	@RequestMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title","login-smartcontact");
		return "login";
	}
}
