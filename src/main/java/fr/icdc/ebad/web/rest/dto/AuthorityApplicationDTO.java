package fr.icdc.ebad.web.rest.dto;

/**
 * Created by dtrouillet on 07/03/2016.
 */
public class AuthorityApplicationDTO {
    private String loginUser;
    private boolean addModo;
    private boolean addUser;
    private boolean removeUser;
    private boolean removeModo;
    private Long idApplication;

    public boolean isAddModo() {
        return addModo;
    }

    public void setAddModo(boolean addModo) {
        this.addModo = addModo;
    }

    public boolean isAddUser() {
        return addUser;
    }

    public void setAddUser(boolean addUser) {
        this.addUser = addUser;
    }

    public boolean isRemoveModo() {
        return removeModo;
    }

    public void setRemoveModo(boolean removeModo) {
        this.removeModo = removeModo;
    }

    public boolean isRemoveUser() {
        return removeUser;
    }

    public void setRemoveUser(boolean removeUser) {
        this.removeUser = removeUser;
    }

    public Long getIdApplication() {
        return idApplication;
    }

    public void setIdApplication(Long idApplication) {
        this.idApplication = idApplication;
    }

    public String getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(String loginUser) {
        this.loginUser = loginUser;
    }
}
