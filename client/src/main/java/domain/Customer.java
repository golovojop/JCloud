package domain;

public class Customer {
    private Integer id;
    private String login;
    private String pass;

    public Customer() {
    }

    public Customer(String login, String pass) {
        this.login = login;
        this.pass = pass;
    }

    public Customer(Integer id, String login, String pass) {
        this.id = id;
        this.login = login;
        this.pass = pass;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}

