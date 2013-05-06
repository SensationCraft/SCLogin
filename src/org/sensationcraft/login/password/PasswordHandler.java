package org.sensationcraft.login.password;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class PasswordHandler
{
	/*
         * The following code is based on xAuth's hashing and it is used
         * to check passwords in their database. This is used so it is possible
         * to gradually move to our own hashing.
         * 
         * © All credits go to their respective authors
         */
	public static String hash(String toHash)
	{
		String salt = whirlpool(UUID.randomUUID().toString()).substring(0, 12);
		String hash = whirlpool(salt + toHash);
		int saltPos = (toHash.length() >= hash.length() ? hash.length() - 1 : toHash.length());
		return hash.substring(0, saltPos) + salt + hash.substring(saltPos);
	}
        
        public static String hash(String toHash, String algorithm) 
        {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			md.update(toHash.getBytes());
			byte[] digest = md.digest();
			return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}		
	}

	public static String whirlpool(String toHash)
	{
		Whirlpool w = new Whirlpool();
		byte[] digest = new byte[Whirlpool.DIGESTBYTES];
		w.NESSIEinit();
		w.NESSIEadd(toHash);
		w.NESSIEfinalize(digest);
		return Whirlpool.display(digest);
	}
}
