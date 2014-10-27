package utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

public class Password {
	private final static int ITERATION_NUMBER = 5;
	
	private static byte[] base64ToByte(String data){
		return DatatypeConverter.parseBase64Binary(data);
    }
	
	private static String byteToBase64(byte[] data){
		return DatatypeConverter.printBase64Binary(data);
	}
	
	private static byte[] salt = base64ToByte("hsgkg4938fgbk7jhf:d");
	
	/**
	 * creates the md5-hash with salt and multiple iteration
	 * 
	 * @param password The Password to be hashed.
	 * @return The hashed password.
	 */
	public static String hashPassword(String password){
        if(password == null) return null;
		
        MessageDigest digest;
        byte[] input = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
	        digest.reset();
	        digest.update(salt);
	        try {
				input = digest.digest(password.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
	        for (int i = 0; i < ITERATION_NUMBER ; i++) {
	            digest.reset();
	            input = digest.digest(input);
	        }
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
        return byteToBase64(input);
	}
	
	/***
	 * checks if the password is complex enough (length)
	 * @param password password to be checked
	 * @return
	 */
	public static String isPasswordComplex(String password){
		if(password.length() < 5)
			return "nicht ausreichend: Passwortlänge ist kleiner 5";
		return null;
	}
}
