package fr.icdc.ebad.service.util;

import com.jcraft.jsch.UserInfo;

public class SUserInfo implements UserInfo {
    private String password;
    private String passPhrase;

    public SUserInfo(String password, String passPhrase) {
        this.password = password;
        this.passPhrase = passPhrase;
    }

	@Override
	public String getPassphrase() {
		return passPhrase;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean promptPassword(String message) {
		return true;
	}

	@Override
	public boolean promptPassphrase(String message) {
		return false;
	}

	@Override
	public boolean promptYesNo(String message) {
		return false;
	}

	@Override
	public void showMessage(String message) {
		// noop
	}

}
