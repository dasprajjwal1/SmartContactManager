package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.hibernate.internal.build.AllowPrintStacktrace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.contactRepository;
import com.smart.dao.userRepository;
import com.smart.entities.User;
import com.smart.helper.message;
import com.smart.entities.Contact;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private userRepository userrepo;

	@Autowired
	private contactRepository contactrepo;
	
	@Autowired
	private BCryptPasswordEncoder passwordencoder;

	// per page =5 [n]
	// current page = 0 [page]

	@RequestMapping("/index/{page}")
	public String dashboard(@PathVariable("page") Integer page, Model model, Principal principal) {

		model.addAttribute("title", "userdashboard-smartcontact");

		String username = principal.getName();
		User user = userrepo.getUserByUsername(username);

		// setting the page with 5 contact.
		Pageable pageable = PageRequest.of(page, 3);
		Page<Contact> contacts = this.contactrepo.findAllContactByUserId(user.getId(), pageable);

		model.addAttribute("contacts", contacts);
		model.addAttribute("currentpage", page);
		model.addAttribute("totalpage", contacts.getTotalPages());

		model.addAttribute("user", user);

		return "normal/user_dashboard";
	}

	// displaying the contact upload form

	@RequestMapping(path = "/add-contact", method = RequestMethod.GET)
	public String ContactUploadform(Model model, Principal principal) {
		model.addAttribute("title", "smart-uploadform");
		model.addAttribute("contact", new Contact());
		String username = principal.getName();
		User user = userrepo.getUserByUsername(username);
		model.addAttribute("user", user);
		return "normal/contactUploadForm";
	}

	// processing the contact upload form
	@PostMapping("/processcontact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileimage") MultipartFile file,
			Principal principal, Model model, HttpSession session) {
		System.out.println(contact);
		String username = principal.getName();
		User user = userrepo.getUserByUsername(username);
		contact.setUser(user);
		try {

			if (file.isEmpty()) {

				System.out.println("file must not be empty");
				contact.setImage("contact.png");
			} else {
				contact.setImage(file.getOriginalFilename());

				File savefile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("image uploaded successfully.........");
			}
			this.contactrepo.save(contact);

			// add the message to the user for successful entry
			System.out.println("added to the database");
			session.setAttribute("message", new message("new Contact added successfully!!", "success"));

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new message("something went worng !! please try again..", "denger"));
		}

		model.addAttribute("user", user);
		model.addAttribute("contact", new Contact());

		return "redirect:/user/add-contact";
	}

	// showing specific details
	@RequestMapping("/{cId}/contact")
	public String contactDetails(@PathVariable("cId") Integer cId, Model m, Principal principal) {

		System.out.println("cId = " + cId);

		try {
			Optional<Contact> contactoptional = this.contactrepo.findById(cId);
			Contact contact = contactoptional.get();

			// authenticating the user to access the other user contact.by hit and trail
			// method
			String username = principal.getName();
			User user = userrepo.getUserByUsername(username);
			if (user.getId() == contact.getUser().getId()) {
				m.addAttribute("contact", contact);
			}
			System.out.println("come here");
			m.addAttribute("user", user);
			m.addAttribute("title", "Contact Details");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "normal/contact_details";
	}

	// delete a contact
	@RequestMapping(path = "/delete/{contactid}")
	public String deleteContact(@PathVariable Integer contactid, Principal principal, HttpSession session) {
		try {
			String username = principal.getName();
			User user = userrepo.getUserByUsername(username);

			if (contactid.equals(null)) {
				System.out.println("no contact available of id " + contactid);
				session.setAttribute("message", new message("something went wrong!!", "danger"));
			} else {

				Optional<Contact> usercontact = this.contactrepo.findById(contactid);
				Contact contact = usercontact.get();
				this.contactrepo.delete(contact);
				System.out.println("contact delete with id" + contact.getcId());
				session.setAttribute("message", new message("Successfully deleted!!", "danger"));
			}

		}

		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("this is contact delete");
		return "redirect:/user/index/0";

	}

	// to edit the specific contact
	@RequestMapping(path = "/edit/{contactId}", method = RequestMethod.GET)
	public String editForm(@PathVariable("contactId") Integer contactId, Model m, Principal principal) {
		try {
			String username = principal.getName();
			User user = this.userrepo.getUserByUsername(username);

			// fetching the contact from the database
			Optional<Contact> contact = this.contactrepo.findById(contactId);
			Contact contact2 = contact.get();
			m.addAttribute("title", "SmartContact-EditContact");
			m.addAttribute("contact", contact2);
			m.addAttribute("user", user);
		} catch (Exception e) {
			System.out.println("Error " + e.getMessage());
			e.printStackTrace();
		}

		return "normal/editContactForm";
	}

	// edit contact
	@PostMapping("/editcontact")
	public String editContact(@ModelAttribute Contact contact, @RequestParam("profileimage") MultipartFile file,
			HttpSession session, Principal principal) throws IOException {

		try {
			Contact oldContact = this.contactrepo.findById(contact.getcId()).get();

			// image
			if (!file.isEmpty()) {

				// delete the old photo
				File deletefile = new ClassPathResource("static/img").getFile();
				File file2 = new File(deletefile, oldContact.getImage());
				file2.delete();

				// upload a new photo
				File savefile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImage(file.getOriginalFilename());

			} else {
				contact.setImage(oldContact.getImage());
			}
			User user = this.userrepo.getUserByUsername(principal.getName());
			contact.setUser(user);

			session.setAttribute("message", new message("contact updated successfully!!", "success"));

			this.contactrepo.save(contact);

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new message("something went wrong!!", "danger"));
		}

		return "redirect:/user/" + contact.getcId() + "/contact";
	}

	@PostMapping("/search/{page}")
	public String Search(@RequestParam("search") String search, @PathVariable("page") Integer page, Principal principal,
			Model model, HttpSession session) {
		User user = this.userrepo.getUserByUsername(principal.getName());
		Pageable pageable = PageRequest.of(page, 3);

		// retriving all the contact of the user.
		Page<Contact> usercontact = this.contactrepo.findAllContactByUserId(user.getId(), pageable);
		for (Contact c : usercontact) {
			if (c.getName().equals(search))
				model.addAttribute("contacts", c);

		}

		model.addAttribute("search", search);
		model.addAttribute("currentpage", page);
		model.addAttribute("totalpage", usercontact.getTotalPages());

		model.addAttribute("user", user);
		System.out.println("search keyword " + search);
		return "normal/searchcontact";
	}

	// getting the logged in user profile
	@GetMapping("/profile")
	public String userProfile(Principal principal, Model m) {
		User user = this.userrepo.getUserByUsername(principal.getName());
		m.addAttribute("user", user);
		m.addAttribute("title", "smartContact-userProfile");
		return "normal/userprofile";
	}

	// to edit the user details
	@GetMapping("/{userid}/edit")
	public String editUser(@PathVariable("userid") Integer userid, Model model) {
		User user = this.userrepo.findById(userid).get();
		model.addAttribute("user", user);
		model.addAttribute("title", "smartContact-editUser");
		return "normal/userEditForm";
	}

	@GetMapping("/settings")
	public String openSettings(Model model,Principal principal) {
		User user = this.userrepo.getUserByUsername(principal.getName());
		model.addAttribute("user", user);
		model.addAttribute("title", "smartContact-Settings");
		return "normal/settings";
	}
	@PostMapping("/password")
	public String ChangePassword(@RequestParam("old_password") String oldpassword
			,@RequestParam("new_password") String newpassword,Principal principal,HttpSession session)
	{
		System.out.println("old password "+oldpassword);
		System.out.println("new password "+newpassword);
		
		User user = this.userrepo.getUserByUsername(principal.getName());
		
		
		if(this.passwordencoder.matches(oldpassword, user.getPassword()))
		{
			user.setPassword(this.passwordencoder.encode(newpassword));
			this.userrepo.save(user);
			System.out.println("password changed successfully !!");
			session.setAttribute("message", new message("your password changed successfully !! please login with your new password","success"));
		}
		else 
		{
			System.out.println("password is not changed !!");
			session.setAttribute("message", new message("your password not changed !! please login with your old password","danger"));
		}
		
		return "login";
		
		
	}
}
