package com.util;

/**
 * Token验证失败异常
 */
public class TokenValidationFailedException extends Exception {

	public TokenValidationFailedException(String msg){
		super(msg);
	}
}
