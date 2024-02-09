package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class AppConfig {
	
	
	@Bean
	public SecurityFilterChain springSecurityConfiguration(HttpSecurity http)throws Exception{
		
		http.authorizeHttpRequests()
		.requestMatchers("/admin/**").hasAuthority("ADMIN")
		.requestMatchers("/user/**").hasAuthority("USER")
		.requestMatchers( "/**").permitAll()
		.and()
		.csrf().disable()
		.formLogin()
		.loginPage("/signIn")
		.loginProcessingUrl("/dologin")
		.defaultSuccessUrl("/user/index")
		.and()
		.httpBasic();
		
		return http.build();
		
	}
	
	@Bean
	PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}

}
