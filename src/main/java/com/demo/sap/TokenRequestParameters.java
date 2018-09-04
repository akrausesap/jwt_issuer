package com.demo.sap;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class TokenRequestParameters {
	
	@NotNull
	private String subject;
	
	private String issuer ="DummyJWTIssuer";
	
	private String[] scopes = null;
	
	private String audience = null;
	private int expirationDurationMinutes = 0;
	private int notBeforeMinutesInThePast = 0;
	private boolean includeIssuedAt = false;
		
}
