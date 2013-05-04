package org.sensationcraft.login.password;

/**
 *
 * @author DarkSeraphim
 */
public enum PasswordType
{
	DEFAULT(0),
	WHIRLPOOL(1),
	MD5(2, "MD5"),
	SHA1(3, "SHA1"),
	SHA256(4, "SHA-256"),
	AUTHME_SHA256(5);

	private int type;
	private String algorithm;

	PasswordType(int type) 
        {
		this(type, null);
	}

	PasswordType(int type, String algorithm) 
        {
		this.type = type;
		this.algorithm = algorithm;
	}

	public int getType()
        {
            return type; 
        }
	public String getAlgorithm() 
        {
            return algorithm; 
        }

	public static PasswordType getType(int type) 
        {
		for (PasswordType t : values())
			if (t.type == type)
				return t;

		return null;
	}
}