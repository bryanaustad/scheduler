package org.lds.security;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.context.ServletContextAware;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler, ServletContextAware {

	@Qualifier("applicationProperties")
	@Autowired()
	private Properties properties;

	private ServletContext servletContext;

	@Override
	public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			AuthenticationException e) throws IOException, ServletException {
		String message;

		try {
			throw e;
		}
		catch (BadCredentialsException ex) {
			message = (String) properties.get("authentication.error.badcredentials");
		}
		catch (LockedException ex) {
			message = (String) properties.get("authentication.error.locked");
		}
		catch (AuthenticationException ex) {
			message = (String) properties.get("authentication.error.default");
		}

		httpServletRequest.setAttribute("errorMessage", message);
		servletContext.getRequestDispatcher("/login.jsp?error=1").forward(httpServletRequest, httpServletResponse);
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
}