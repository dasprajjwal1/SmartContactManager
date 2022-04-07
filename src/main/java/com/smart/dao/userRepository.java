package com.smart.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.entities.User;

public interface userRepository extends JpaRepository<User, Integer> {
	@Query("select user from User user where user.email = :email")
	public User getUserByUsername(@RequestParam("email") Object email);
	
}
