package org.lds.service;

import org.lds.stack.ldsaccount.LdsAccountDetails;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class LdsAccountDetailFactoryBean implements FactoryBean<LdsAccountDetails> {
	@Override
	public LdsAccountDetails getObject() throws Exception {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return null;
		}
		return (LdsAccountDetails) authentication.getDetails();
	}

	@Override
	public Class<LdsAccountDetails> getObjectType() {
		return LdsAccountDetails.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}
}
