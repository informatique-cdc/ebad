package fr.icdc.ebad.web.rest.dto;

/**
 * Created by dtrouillet on 07/03/2016.
 */
public class RolesDTO {
    private String loginUser;
    private boolean roleUser;
    private boolean roleAdmin;

    public boolean isRoleUser() {
        return roleUser;
    }

    public void setRoleUser(boolean roleUser) {
        this.roleUser = roleUser;
    }

    public boolean isRoleAdmin() {
        return roleAdmin;
    }

    public void setRoleAdmin(boolean roleAdmin) {
        this.roleAdmin = roleAdmin;
    }

    public String getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(String loginUser) {
        this.loginUser = loginUser;
    }
}
