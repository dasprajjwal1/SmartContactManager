package com.smart.config;

import java.nio.file.attribute.UserPrincipalNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.dao.userRepository;
import com.smart.entities.User;

public class userDetailsServiceimpl implements UserDetailsService {

	@Autowired
	private userRepository userrepo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		// fetching user from database User userByUsername =
		User userByUsername = userrepo.getUserByUsername(username);
		if (userByUsername == null) {
			throw new UsernameNotFoundException("could not found the user with username " + username);
		}
		/* System.out.println("user "+userByUsername.getEmail()); */
		customUserDetails customuserdetails = new customUserDetails(userByUsername);
		return customuserdetails;
	}

}
