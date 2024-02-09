package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.razorpay.*;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.repository.ContactRepository;
import com.smart.repository.MyOrdersRespository;
import com.smart.repository.UserRepository;


import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private MyOrdersRespository myOrdersRespository;
	
	@ModelAttribute // this method will get called for each below methods
	public void addCommonDataToModel(Model model, Principal principal) {
		String userName = 
				principal.getName();
		
		
		// get user by user email
		User user = userRepository.findByEmail(userName).get();
		
//		System.out.println(user);
		
		model.addAttribute("user", user);
	}
	
	@GetMapping("/index")
	public String dashboard(Model model, Principal principal){
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	
	// add contact handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title", "Add contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	// processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute("contact") Contact contact, @RequestParam("profileImage") MultipartFile file,  Principal principal, HttpSession session) {
		
		try {
			// get current user
			String userName = principal.getName();
			User user = userRepository.findByEmail(userName).get();
			
			// processing and uploading file
			if(file.isEmpty()) {
				
				// set default image
				contact.setImage("profile.png");
				
			}else {
				contact.setImage(file.getOriginalFilename());
				File f =  new ClassPathResource("static/img").getFile();
				System.out.println(f.getAbsolutePath());
			    Path path = Paths.get(f.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			
			contact.setUser(user);
			user.getContacts().add(contact);
			
			
			userRepository.save(user);
			System.out.println(contact);
			
			// success message
			session.setAttribute("message", new Message("Your contact is added", "success"));
		}catch(Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong! try again", "danger"));
		}
		return "normal/add_contact_form";
	}
	
	// show contacts 
	// data per page = 5
	// index = 0
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,  Model model, Principal principal) {
		
		// get all contacts from current user
		String userName = principal.getName();
		User user = userRepository.findByEmail(userName).get();
		
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = contactRepository.findContactsByUser(user.getId(), pageable);
	
		
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		
		
		model.addAttribute("title", "Show User Contacts");
		return "normal/show_contacts";
		
	}
	
	// showing contact details
	@GetMapping("/contact/{cid}")
	public String showContactDetails(@PathVariable Integer cid, Model model, Principal principal) {
		
		Optional<Contact> optional = contactRepository.findById(cid);
		Contact contact = null;
		if(optional.isPresent()) {
			contact = optional.get();
		}
		
		String userName = principal.getName();
		User user = userRepository.findByEmail(userName).get();
		
		// security check
		if(contact != null && user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
		}
		
		return "normal/contact_detail";
	}
	
	
	// delete contact
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable Integer cid, Model model, Principal principal, HttpSession session) {
		
		Optional<Contact> optional = contactRepository.findById(cid);
		Contact contact = null;
		if(optional.isPresent()) {
			contact = optional.get();
		}
		
		
		String userName = principal.getName();
		User user = userRepository.findByEmail(userName).get();
		
		// security check
		if(contact != null && user.getId() == contact.getUser().getId()) {
			user.getContacts().remove(contact);
			userRepository.save(user);
		    session.setAttribute("message", new Message("Contact deleted successfully...", "success"));
		}
		
		
		
		return "redirect:/user/show-contacts/0";
	}
	
	
	// open update form
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable Integer cid, Model model) {
		
		model.addAttribute("title", "Update Contact");
		
		Contact contact = contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);
		return "normal/update_form";
	}
	
	// process update form
	@PostMapping("/process-update")
	public String processUpdateForm(@ModelAttribute Contact contact,  @RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
		
		try {
			
			// old contact details
			Contact oldContact = contactRepository.findById(contact.getCid()).get();
			
			// image
			if(!file.isEmpty()) {
				
				// update profile
				
				// delete previous profile
				File deleteFile =  new ClassPathResource("static/img").getFile();
				File file2 = new File(deleteFile, oldContact.getImage());
				file2.delete();
				
				
				// update new profile
				File f =  new ClassPathResource("static/img").getFile();
			    Path path = Paths.get(f.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}else {
				contact.setImage(oldContact.getImage());
			}
			
			// Set current user in updated contact
			
			
			User user = userRepository.findByEmail(principal.getName()).get();
			contact.setUser(user);
			
			contactRepository.save(contact);
			 session.setAttribute("message", new Message("Contact updated successfully...", "success"));
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		System.out.println(contact.getName());
		return "redirect:/user/contact/"+contact.getCid();
	}
	
	
	// User Profile
	@GetMapping("/profile")
	public String userProfile(Model model) {
		
		model.addAttribute("title", "User Profile");
		
		return "normal/profile";
	}
	
	// Open Setting
	@GetMapping("/setting")
	public String openSetting(Model model) {
		
		model.addAttribute("title", "Settings");
		
		return "normal/setting";
	}
	
	// change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {
		
		// get current user
		String userName = principal.getName();
		User currentUser = userRepository.findByEmail(userName).get();
		
		
		if(passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
			
			// change the password
			currentUser.setPassword(passwordEncoder.encode(newPassword));
			userRepository.save(currentUser);
			 session.setAttribute("message", new Message("Your password changed successfully...", "success"));
			
		}else {
			
			// error
			 session.setAttribute("message", new Message("Enter valid old password...", "danger"));
			 return "redirect:/user/setting";
		}
		
//		System.out.println("OldPassword : "+oldPassword);
//		System.out.println("NewPassword : "+newPassword);
		return "redirect:/user/index";
	}
	
	
	// create order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data, Principal principal) throws Exception {
//		System.out.println(data);
		
		int amount =  Integer.parseInt(data.get("amount").toString());
	
		RazorpayClient client = new RazorpayClient("key_id", "key_secret");
		
		JSONObject ob = new JSONObject();
		ob.put("amount", amount*100); // amount * 100 paisa
		ob.put("currency", "INR");
		ob.put("receipt", "txn_85895");
		
		// Creating order
		Order order = client.Orders.create(ob); 
		
		// save order in database
		MyOrder myOrder = new MyOrder();
		myOrder.setAmount(order.get("amount")+"");
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("created");
		myOrder.setUser(userRepository.findByEmail(principal.getName()).get());
		myOrder.setReceipt(order.get("receipt"));
		
		myOrdersRespository.save(myOrder);
		
		
//		System.out.println(order);
	  
		return order.toString();
	}
	
	// update payment order
	
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data){
//		System.out.println(data);
		
		
		MyOrder myOrder = myOrdersRespository.findByOrderId(data.get("order_id").toString());
		myOrder.setPaymentId(data.get("payment_id").toString());
		myOrder.setStatus(data.get("status").toString());
		
		myOrdersRespository.save(myOrder);
		
		
		return ResponseEntity.ok(Map.of("msg", "updated"));
	}
}
