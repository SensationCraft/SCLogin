package org.sensationcraft.login.messages;

import org.bukkit.ChatColor;

/**
 *
 * @author DarkSeraphim
 */
public enum Messages
{
    
    ALREADY_REGISTERED("&cI'm sorry, you are already registered. You can use '/changepw' to change your password."),
    ALREADY_LOGGEDIN("&cYou are already logged in."),
    INCORRECT_PASSWORD("Incorrect password!"),
    IP_DOESNT_MATCH("Your IP does not match with the last ip you authenticated with."),
    IP_LOCKOUT("Your ip has been temporarily locked out because you surpassed the amount of tries when entering your password"),
    INVALID_SYNTAX("&cInvalid syntax! Correct usage: "),
    LOCKED("&cThat account is locked by an administrator."),
    LOGIN_SUCCESS("&aYou have been logged in."),
    LOGOUT("&aYou are no longer logged in."),
    NOT_LOGGEDIN("&cYou have to login first."),
    NOT_REGISTERED("&cThat account is not registered. Use /register <password> <confirm password> to register."),
    NEW_PLAYER("Welcome to SensationCraft. It does not appear you logged in before. Please register using /register <password> <password>"),
    PASSWORD_BLACKLISTED("&cPlease pick another password."),
    PASSWORD_CHANGED("&aPassword changed."),
    PASSWORD_TOO_SHORT("&cYour entered password is too short. At least 6 characters are required."),
    PASSWORDS_DONT_MATCH("&cYour entered password and the confirmation password don't seem to match."),
    REGISTER_SUCCESS("&aYou are now registered, use /login <password> to login."),
    RELOAD_LOGOUT("&aServer reloaded, you have been automagically logged out."),
    SAFEGUARD_ALREADY_DISABLED("&cSafegaurd already disabled."),
    SAFEGUARD_ALREADY_ENABLED("&cSafegaurd already enabled."),
    SAFEGUARD_DISABLED("&aSafegaurd disabled."),
    SAFEGUARD_ENABLED("&aSafegaurd enabled. Don't forget to read the FAQ on the fora!"),
    SAFEGUARD_INFO("&6This is ment to be another layer of security for your account. Before enabling this feature, please read the FAQ in the 'News' section!"),
    UNREGISTER_SUCCESS("&aAccount unregistered. Use /register to register the account."),
    UNREGISTER_FAILED("&cFailed to unregister the account."),
    USERNAME_BLACKLISTED("&cPlease pick another username"),
    WELCOME_BACK("&cWelcome back to SensationCraft. Please login using /login <password>");
    
    private final String message;

    Messages(String msg)
    {
        this.message = ChatColor.translateAlternateColorCodes('&', msg);
    }

    public String getMessage()
    {
        return this.message;
    }
}
