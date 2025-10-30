package com.derkaiser.auth.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.derkaiser.auth.commons.dto.request.LoginRequest;
import com.derkaiser.auth.commons.dto.request.UserEntityRequest;
import com.derkaiser.auth.commons.dto.response.TokenResponse;
import com.derkaiser.auth.commons.model.entity.UserEntity;
import com.derkaiser.auth.commons.model.enums.UserRole;
import com.derkaiser.auth.repository.UserEntityRepository;
import com.derkaiser.auth.service.AuthService;
import com.derkaiser.auth.service.JwtService;
import com.derkaiser.exceptions.DuplicateEmailException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
	private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
	
	private final UserEntityRepository userEntityRepository;
	private final PasswordEncoder passWordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	
	
	@Override
	public TokenResponse createUser(@Valid UserEntityRequest userEntityRequest) {
		log.info("Intentando crear usuario para email: {}", userEntityRequest.getEmail());
		
		if (userEntityRepository.findByEmail(userEntityRequest.getEmail()).isPresent()) {
		 log.warn("Intento de crear usuario con email existente: {}", userEntityRequest.getEmail());
		 throw new DuplicateEmailException("El email ya esta registrado").
		}
		
		UserEntity userToSave = mapToEntity(userEntityRequest, UserRole.USER);
		UserEntity userCreated = userEntityRepository.save(userToSave);
		log.info("Usuario creado exitosamente con ID: {}", userCreated.getId());
		
		return jwtService.generateToken(userCreated.getEmail(), userCreated.getRole().name());
	}

	@Override
	public TokenResponse login(LoginRequest loginRequest) {
		log.info("Intentando login para usuario: {}", loginRequest.getEmail());
		
		Authentication authentication = authenticationManager.authenticate(

				new UsernamePasswordAuthenticationToken(
						loginRequest.getEmail(),
						loginRequest.getPassword()
						)
				
				);
		
		UserEntity user = (UserEntity) authentication.getPrincipal();		
		return jwtService.generateToken(user.getEmail(), user.getRole().name()) ;
	}
	
	private UserEntity mapToEntity (UserEntityRequest userEntityRequest, UserRole role) {
		
	      return UserEntity.builder()
	                .email(userEntityRequest.getEmail())
	                .password(passWordEncoder.encode(userEntityRequest.getPassword()))
	                .firstName(userEntityRequest.getFirstName())
	                .lastName(userEntityRequest.getLastName())
	                .role(role)
	                .build()
		
	}

}
