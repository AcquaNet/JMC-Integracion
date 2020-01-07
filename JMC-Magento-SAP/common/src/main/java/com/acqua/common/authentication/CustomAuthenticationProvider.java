package com.acqua.common.authentication;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider; 
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
	
	@Value("${authentication.user.name}")
	String userName;
	
	@Value("${authentication.user.password}")
	String userPwd;
	
	@Override
	public Authentication authenticate(Authentication authValue) throws AuthenticationException {
		
		String name = authValue.getName().trim();

		String secratePass = authValue.getCredentials().toString().trim();

		try {

			if (name.equals(userName) && secratePass.equals(userPwd)) {
				
				List<GrantedAuthority> grantedAuths = new ArrayList<>();
				
				grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
				
				Authentication auth = new UsernamePasswordAuthenticationToken(name, secratePass, grantedAuths);
				
				return auth;
				
			} else {
				
				AuthentificationErrorCode autErrorCode = new AuthentificationErrorCode();
				autErrorCode.setErrorMessage("Bad Credentials entered");
				autErrorCode.setHttpStatus(400);
				
				AuthentificationResponseError responseError = new AuthentificationResponseError();
				responseError.setErrors(autErrorCode);
				 
				throw new AuthentificationException(responseError);
			}

		} catch (AuthenticationException e) {
			
			AuthentificationErrorCode autErrorCode = new AuthentificationErrorCode();
			autErrorCode.setErrorMessage(e.getMessage());
			autErrorCode.setHttpStatus(400);
			
			AuthentificationResponseError responseError = new AuthentificationResponseError();
			responseError.setErrors(autErrorCode);
			 
			throw new AuthentificationException(responseError);

		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}
