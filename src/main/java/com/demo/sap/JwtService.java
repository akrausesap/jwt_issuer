package com.demo.sap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.jose4j.jwk.JsonWebKey.OutputControlLevel;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class JwtService {
	
	private RsaJsonWebKey rsaJsonWebKey; 
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	public JwtService() throws JoseException {
		rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);	
		rsaJsonWebKey.setKeyId(UUID.randomUUID().toString());
		logger.debug("New JSON Web Key generated");
	}
	
	@GetMapping("/jwk")
	@ApiOperation(value="Get Key", notes="This function returns the JWK value that can be used "
			+ "to verify the JWT signature")
	public ResponseEntity<String> getJwk () {	
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);
		
		return new ResponseEntity<String>(
				String.format("{ \"keys\":[%s]}", 
						rsaJsonWebKey.toJson(OutputControlLevel.PUBLIC_ONLY)),
				responseHeaders,
				HttpStatus.OK);
	}
	
	private JwtClaims createClaims(TokenRequestParameters parameters) {
		
		JwtClaims claims = new JwtClaims();
		claims.setSubject(parameters.getSubject());
		
		claims.setIssuer(
				parameters.getIssuer());		
							
		if (parameters.getScopes() != null) {
			claims.setStringListClaim("scopes",				 
					Arrays.asList(parameters.getScopes()));		
		}
		
		
		if (parameters.getAudience() != null) {
			claims.setAudience(parameters.getAudience());	
		}
		
		if (parameters.getExpirationDurationMinutes() != 0) {
			claims.setExpirationTimeMinutesInTheFuture(parameters.getExpirationDurationMinutes());
		}
		
		if (parameters.getNotBeforeMinutesInThePast() != 0) {
			claims.setNotBeforeMinutesInThePast(parameters.getNotBeforeMinutesInThePast());
		}
		
		//claims.setGeneratedJwtId();
		if (parameters.isIncludeIssuedAt()) {
			claims.setIssuedAtToNow();
		}
		
		return claims;
		
	}
	
	
	
	@RequestMapping(path="/oauth2/token/query", params="subject")
	@ApiOperation(value="Create JWT Token", notes="This function returns the JWT token that can be used "
			+ "to test Authorization. It accepts Query Parameters.")
	public ResponseEntity<Map<String, Object>> getToken(
			@RequestParam(name="subject", required=true) @NotNull String subject,
			@RequestParam(name="issuer", required=false) String issuer, 
			@RequestParam(name="scopes", required=false) String scopesString, 
			@RequestParam(name="audience", required=false) String audience, 
			@RequestParam(name="expirationDurationMinutes", defaultValue="0") int expirationDurationMinutes,
			@RequestParam(name="notBeforeMinutesInThePast", defaultValue="0") int notBeforeMinutesInThePast, 
			@RequestParam(name="includeIssuedAt", defaultValue="false") boolean includeIssuedAt)
					throws JoseException, MalformedClaimException {
		
		TokenRequestParameters parameters = new TokenRequestParameters();
		
		parameters.setSubject(subject);
		
		if (issuer != null) {
			parameters.setIssuer(issuer);
		}
		
		if (scopesString != null) {
			parameters.setScopes(scopesString.split(","));
		}
		
		if (audience != null) {
			parameters.setAudience(audience);
		}
		
		if (expirationDurationMinutes != 0) {
			parameters.setExpirationDurationMinutes(expirationDurationMinutes);
		}
		
		if (notBeforeMinutesInThePast != 0) {
			parameters.setNotBeforeMinutesInThePast(notBeforeMinutesInThePast);
		}
		
		if (includeIssuedAt) {
			parameters.setIncludeIssuedAt(includeIssuedAt);
		}
		
		return getToken(parameters);	
	}
	
	
	
	@RequestMapping(path="/oauth2/token/body", method=RequestMethod.POST, consumes="application/json")
	@ApiOperation(value="Create JWT Token", notes="This function returns the JWT token that can be used "
			+ "to test Authorization. It accepts a Json call.")
	public ResponseEntity<Map<String, Object>> getToken(
			@RequestBody @Valid TokenRequestParameters parameters) throws JoseException, MalformedClaimException {
		
		JwtClaims claims = createClaims(parameters);
		
		JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(claims.toJson());
		jws.setKey(rsaJsonWebKey.getPrivateKey());
		jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
		String jwt = jws.getCompactSerialization();
		
		logger.debug(String.format("JWT Token for %s generated: %s", parameters.getSubject(), jwt));	
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		result.put("access_token", jwt);
		result.put("token_type", "Bearer");	
		
		if (claims.getExpirationTime() != null && claims.getIssuedAt() != null) {
			result.put("expires_in", (claims.getExpirationTime().getValue() - claims.getIssuedAt().getValue()));
		}
		
		return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);		
	}

}
