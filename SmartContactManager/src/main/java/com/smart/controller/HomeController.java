package com.smart.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.repository.UserRepository;


import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manager");
		
		return "home";
	}
	
	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}
	
	@GetMapping("/signup")
	public String signUp(Model model, Principal principal) {
		
		if(principal != null) {
			
			return "redirect:/user/index";
		}
		model.addAttribute("title", "SignUp - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	// handler for registering user
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result,   @RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,  HttpSession session) {
		
		System.out.println(result.hasErrors());
		try {
			if(!agreement) {
//				System.out.println("You have not agreed the terms & condition");
				throw new Exception("You have not agreed the terms & condition");
			}
			
		
			if(result.hasErrors()) {
				
//				System.out.println(result.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			user.setRole("USER");
			user.setEnabled(true);
			user.setImageUrl("defaulf.png");
		
		
			
			System.out.println(user);
			userRepository.save(user);
			
			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully Registered !", "alert-success"));
			return "signup";
		}catch (DataIntegrityViolationException ex) {
	        // Duplicate email entry violation
	        model.addAttribute("user", user);
	        session.setAttribute("message", new Message("Email address already exists. Please use a different email.", "alert-danger"));
	        return "signup";
	    } catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong ! "+e.getMessage(), "alert-danger"));
			return "signup";
		}
		
		
		
		
		
	}
	
	// handler for custom login
	@GetMapping("/signIn")
	public String customLogin(Model model, Principal principal) {
		
		if(principal != null) {
			
			return "redirect:/user/index";
		}
		
		
        return "login";
	}
	
	
}
