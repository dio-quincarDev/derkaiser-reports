package com.derkaiser.auth.service;

import java.util.List;

import com.derkaiser.auth.commons.dto.response.UserResponse;

public interface UserManagementService {
	List<UserResponse>listUsers(); 
	
}
