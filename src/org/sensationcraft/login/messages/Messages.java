package org.sensationcraft.login.messages;

import org.bukkit.ChatColor;

/**
 *
 * @author DarkSeraphim
 */
public enum Messages
{

	ALREADY_REGISTERED("&c&lYour account has already been registered."),
	ALREADY_LOGGEDIN("&c&lYou are already logged in."),
	INCORRECT_PASSWORD("&c&lIncorrect password!"),
	IP_DOESNT_MATCH("Your IP does not match with the last authenticated IP."),
	IP_LOCKOUT("Your account has been temporarily locked for entering too many incorrects passwords."),
	INVALID_SYNTAX("&c&lInvalid syntax! Correct usage: "),
	LOCKED("&c&lThat account is locked by an administrator."),
	LOGGING_IN("&a&l&oLogging in..."),
	LOGIN_SUCCESS("&a&lYou have been logged in."),
	LOGOUT("&a&lYou are no longer logged in."),
	NOT_LOGGEDIN("&e&lPlease login using /login <password>"),
	NOT_REGISTERED_YET("&e&lPlease register using /register <password>"),
	NOT_REGISTERED("&c&lThat account is not registered. Use /register <password> to register."),
	NEW_PLAYER("&e&l************************************************\n"+
			"&e&l         WELCOME TO SENSATION CRAFT\n"+
			"\n"+
			"&e&l    Please register using /register <password>\n"+
			"&e&l************************************************"),
        PASSWORD_BLACKLISTED("&c&lPlease choose another password."),
        PASSWORD_CHANGED("&a&lYour password has been changed."),
        PASSWORD_CHANGED_OTHER("&a&lPassword has been changed."),
        PASSWORD_NOT_CHANGED("&c&lFailed to change your password."),
        PASSWORD_TOO_SHORT("&c&lYour password is too short. At least 6 characters are required."),
        REGISTER_SUCCESS("&a&lYour account has been registered.\n&e&lUse /login <password> to proceed.\n&e&lYour password is: &n&l&e%password%&e&l. DO NOT FORGET"),
        RELOAD_LOGOUT("&a&lServer reloaded, you have been automaticly logged out."),
        SAFEGUARD_ALREADY_DISABLED("&c&lSafegaurd already disabled."),
        SAFEGUARD_ALREADY_ENABLED("&c&lSafegaurd already enabled."),
        SAFEGUARD_DISABLED("&a&lSafegaurd disabled."),
        SAFEGUARD_ENABLED("&a&lSafegaurd enabled. Don't forget to read the FAQ on the forum!"),
        SAFEGUARD_INFO("&6&lThis is meant to be another layer of security for your account. Before enabling this feature, please read the FAQ in the General Discussion or ask a member of staff!"),
        UNREGISTER_SUCCESS("&a&lYour account has been unregistered."),
        UNREGISTER_FAILED("&c&lFailed to unregister the account."),
        USERNAME_BLACKLISTED("&c&lPlease choose another username"),
        WELCOME_BACK("&e&lPlease login using /login <password>");

	private final String message;

	private final String uncoloured;

	Messages(final String msg)
	{
		this.message = ChatColor.translateAlternateColorCodes('&', msg);
		this.uncoloured = ChatColor.stripColor(this.message);
	}

	public String getMessage()
	{
		return new StringBuilder("\n").append(this.message).append("\n").toString();
	}

	public String getNonColoured()
	{
		return this.uncoloured;
	}

	public static boolean isSCLoginMessage(String message)
	{
		message = ChatColor.stripColor(message);
		if(message.contains("\\u003c")) message = message.replace("\\u003c", "<");
		if(message.contains("\\u003e")) message = message.replace("\\u003e", ">");
		for(final Messages msg : Messages.values())
		{
			final String nocolour = msg.getNonColoured();
			if(message.contains(nocolour))
				return true;
			if(nocolour.contains(message) || (nocolour.contains("%password%") && message.contains(nocolour.substring(nocolour.lastIndexOf("\n")+1, nocolour.indexOf("%password%")))))
				return true;
		}
		return message.contains("An error occurred") ||
				message.contains("You are no longer immune to PvP combat.") ||
				message.contains("You are protected from PvP combat for the next 2 hours. This effect will cancel if voluntarily engage in combat.") ||
				message.contains("This player is temporarily immune to PvP damage.");
	}
}
