package fr.icdc.ebad.config.newoauth;

/**
 * @author Chinna
 * @since 26/3/18
 */
public enum SocialProvider {

	FACEBOOK("facebook"),
	KEYCLOAK("keycloak"),
	TWITTER("twitter"),
	LINKEDIN("linkedin"),
	GOOGLE("google"),
	GITHUB("github"),
	LOCAL("local");

	private String providerType;

	SocialProvider(final String providerType) {
		this.providerType = providerType;
	}

	public String getProviderType() {
		return providerType;
	}

}
