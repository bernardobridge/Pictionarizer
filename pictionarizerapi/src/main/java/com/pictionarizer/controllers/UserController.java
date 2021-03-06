package com.pictionarizer.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.pictionarizer.model.LoginValue;
//import com.pictionarizer.model.Login;
import com.pictionarizer.model.User;
import com.pictionarizer.model.Word;
import com.pictionarizer.repos.UserRepository;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class UserController {
	
	private int passwordMinLength = 8;
	
	private UserRepository repository;
	
	// logger for a debugging purpose
	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	UserController(UserRepository repository){
		this.repository = repository;
	}
	
	@RequestMapping(value = "/users", method = RequestMethod.GET)
	public List<User> getUsers(){
		return repository.findAll();
	}
	
	@RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
	public User getUser(@PathVariable("id") int id) {
		return repository.findById(id).get();
	}
	
//	@RequestMapping(value = "/user/name/{id}", method = RequestMethod.GET)
//	public String getUserName(@PathVariable("id") int id) {
//		return repository.findById(id).get().getName();
//	}
	
	public class Error {
		String message; 
		
		public Error(String message) {
			this.message = message; 
		}
		
		public String getMessage() {
			return message; 
		}
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public ResponseEntity<?> checkIfValidUser(
			@RequestParam("email") String email,
			@RequestParam("password") String password) {  
		int userId = 0;
		
		List<User> userList = repository.findAll();
		
		for(User user: userList) {
			String userEmail = user.getEmail();
			String userPassword = user.getPassword();
			String inputEmail = email;
			String inputPassword = password;
			if(userEmail.equals(inputEmail) && userPassword.equals(inputPassword)) {
				userId = user.getId();
			}
			//LOGGER.info("userEmail: " + userEmail);
			//LOGGER.info("inputEmail: " + inputEmail);
			//LOGGER.info("userPassword: " + userPassword);
			//LOGGER.info("inputPassword: " + inputPassword);
			//LOGGER.info("userId: " + String.valueOf(userId));
		}	
		
		if(userId > 0) {
			LoginValue fetchedUserId = new LoginValue();
			fetchedUserId.setUserId(userId);
			//LOGGER.info("The content of userId: " + userId);
			return new ResponseEntity<>(fetchedUserId, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					new Error("The email address and the password don't match"),  
					HttpStatus.NOT_FOUND
			);
		}
	}
	
	// converts File's data type so it will be compatible on back-end side
	// front-end (MultipartFile) -> back-end (byte[ ]) -> database (LongBlob)
	User convertUser(
			String name,
			String ownLanguage,
			String targetLanguage,
			String country,
			String email,
			String password,
			MultipartFile image,
			String description) {	
		
		User user = new User(); 	
		user.setName(name);
		user.setOwnLanguage(ownLanguage);
		user.setTargetLanguage(targetLanguage);
		user.setCountry(country);
		user.setEmail(email);
		user.setPassword(password);
		user.setDescription(description);
		
		// needs to be Optional in case the value of the image is null
		// -> prevents NullPointerException in the if clause right below 
		Optional<MultipartFile> imageOpt = Optional.ofNullable(image);
		
		// if the user input includes MultipartFile, assign it in the word object, otherwise do nothing
		if(imageOpt.isPresent()) {
			try {
				user.setImage(image.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return user;
	}
	
	@RequestMapping(value = "/user", method = RequestMethod.POST)
	public User saveUser(
			@RequestParam("name") String name,
			@RequestParam("ownLanguage") String ownLanguage,
			@RequestParam("targetLanguage") String targetLanguage,
			@RequestParam("country") String country,
			@RequestParam("email") String email,
			@RequestParam("password") String password,
			@RequestParam(value = "image", required = false) MultipartFile image,
			@RequestParam("description") String description) {
				
		User user = new User();
		
		user = convertUser(
					name,
					ownLanguage,
					targetLanguage,
					country,
					email,
					password,
					image,
					description);
		
		// if the user doesn't provide a profile image, 
		//assign the default-avatar used for the test user whose id number is 2
		Optional<MultipartFile> imageOpt = Optional.ofNullable(image);		
		if(!imageOpt.isPresent()) {
			Optional<User> optUser = Optional.ofNullable(user);
			optUser = repository.findById(2);  // Test User's id number is 2
			byte[] userImage = optUser.get().getImage();
			user.setImage(userImage);
		}
		
		return repository.save(user);
	}
	
	@RequestMapping(value = "/user/{id}", method = RequestMethod.PUT)
	public User updateUser(
			@RequestParam("name") String name,
			@RequestParam("ownLanguage") String ownLanguage,
			@RequestParam("targetLanguage") String targetLanguage,
			@RequestParam("country") String country,
			@RequestParam("email") String email,
			@RequestParam("password") String password,
			@RequestParam(value = "image", required = false) MultipartFile image,
			@RequestParam("description") String description,
			@PathVariable("id") int id) {
		
		User user = null;
		Optional<User> optUser = Optional.ofNullable(user);
		optUser = repository.findById(id);
		
		if(!optUser.isPresent()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found");
		}
		
		user = optUser.get();
		
		if(password.length() < passwordMinLength) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
					"Password must be at least " + String.valueOf(passwordMinLength) + "characters") ;
		}
		
		user = convertUser(
				name,
				ownLanguage,
				targetLanguage,
				country,
				email,
				password,
				image,
				description);
		
		Optional<MultipartFile> imageOpt = Optional.ofNullable(image);
		
		// if the user input doesn't include a MultipartFile, assign the existing image data
		// that has been fetched by repository.findById(id) in the beginning of this method
		if(!imageOpt.isPresent()) {
			byte[] userImage = optUser.get().getImage();
			user.setImage(userImage);
		}
		
		int userId = optUser.get().getId();
		user.setId(userId);
			
		return repository.save(user);
	}
	
	@RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE)
	public void deleteUser(@PathVariable("id") int id) {
		repository.deleteById(id);
	}
	
	// displays an image on the front-end side that is specified by the path <img src={` `}>
	@GetMapping("/user/uploaded-image/{userId}")
	ResponseEntity<byte[]> userImage(@PathVariable int userId){	
		Optional<User> user = repository.findById(userId);
		byte[] image = user.get().getImage();
		return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
	}
	
}
