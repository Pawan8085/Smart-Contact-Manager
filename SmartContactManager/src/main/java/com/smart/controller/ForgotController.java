package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.entities.User;
import com.smart.repository.UserRepository;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	
	// open email id form 
	@GetMapping("/forgot")
	public String openEmailForm(Model model) {
		
		model.addAttribute("title", "Forgot Password");
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session, Model model) {
		
				
		// check email exist or not
		if(userRepository.findByEmail(email).isEmpty()) {
			session.setAttribute("message", "Enter a valid email!");
			return "forgot_email_form";
		}
		
		// generating otp of 4 digit
		
		Random random = new Random();
		
		int a = random.nextInt(10);
		int b = random.nextInt(10);
		int c = random.nextInt(10);
		int d = random.nextInt(10);
		int e = random.nextInt(10);
		int f = random.nextInt(10);
		
		String otp = a+""+b+""+c+""+d+""+e+""+f;
//		System.out.println(otp);
		
		
		// code for sending otp
		
		String subject = "OTP from SCM";
		String message = ""
				+ "<div style='border : 1px solid #e2e2e2; padding : 20px'>"
				+ "<h1>"
				+ "OPT is "
				+ "<b>"+otp
				+ "</b>"
				+ "</h1>"
				+ "</div>";
		String from = "kumarpawanm8085@gmail.com";
		boolean flag = emailService.send(from, email, subject, message);
		
		if(flag) {
			
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		}else {
			
			session.setAttribute("message", "Enter a valid email!");
			return "forgot_email_form";
		}
		
		
	}
	
	
	// verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") String otp, HttpSession session) {
		 
		String myOtp = (String) session.getAttribute("myotp");
	
		
		if(myOtp.equals(otp)) {
			
			
			return "password_change_form";
		}else {
			
			session.setAttribute("message", "You have entered wrong otp!");
			return "verify_otp";
		}
	}
	
	
	// change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {
		
		String email = (String) session.getAttribute("email");
		User user = userRepository.findByEmail(email).get();
		
		// change password
		user.setPassword(passwordEncoder.encode(newpassword));
		userRepository.save(user);
		
		session.setAttribute("message", "You have entered wrong otp!");
		return "redirect:/signIn?change=password changed successfully";
	}
}
