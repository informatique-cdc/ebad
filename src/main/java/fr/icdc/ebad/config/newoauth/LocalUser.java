package fr.icdc.ebad.config.newoauth;

import fr.icdc.ebad.domain.Authority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Chinna
 */
public class LocalUser extends User implements OAuth2User, OidcUser {

	/**
	 *
	 */
	private static final long serialVersionUID = -2845160792248762779L;
	private final OidcIdToken idToken;
	private final OidcUserInfo userInfo;
	private Map<String, Object> attributes;
	private fr.icdc.ebad.domain.User user;

	public LocalUser(final String userID, final String password, final boolean enabled, final boolean accountNonExpired, final boolean credentialsNonExpired,
					 final boolean accountNonLocked, final Collection<? extends GrantedAuthority> authorities, final fr.icdc.ebad.domain.User user) {
		this(userID, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities, user, null, null);
	}

	public LocalUser(final String userID, final String password, final boolean enabled, final boolean accountNonExpired, final boolean credentialsNonExpired,
					 final boolean accountNonLocked, final Collection<? extends GrantedAuthority> authorities, final fr.icdc.ebad.domain.User user, OidcIdToken idToken,
					 OidcUserInfo userInfo) {
		super(userID, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
		this.user = user;
		this.idToken = idToken;
		this.userInfo = userInfo;
	}

	public static LocalUser create(fr.icdc.ebad.domain.User user, Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo) {
		List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();
		for (Authority authority : user.getAuthorities()) {
			grantedAuthorityList.add(new SimpleGrantedAuthority(authority.getName()));
		}
		LocalUser localUser = new LocalUser(user.getEmail(), user.getPassword(), user.isActivated(), true, true, true, grantedAuthorityList,
				user, idToken, userInfo);
		localUser.setAttributes(attributes);
		return localUser;
	}

	@Override
	public String getName() {
		return this.user.getFirstName();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return this.attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public Map<String, Object> getClaims() {
		return this.attributes;
	}

	@Override
	public OidcUserInfo getUserInfo() {
		return this.userInfo;
	}

	@Override
	public OidcIdToken getIdToken() {
		return this.idToken;
	}

	public fr.icdc.ebad.domain.User getUser() {
		return user;
	}
}
