package it.mirea.patients2;

public class User {
    private String email, pass, name, phone, level;

    public User() {}

    public User(String email, String pass, String name, String phone, String level) {
        this.email = email;
        this.pass = pass;
        this.name = name;
        this.phone = phone;
        this.level = level;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }
    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLevel() { return level; }
    public void setLevel(String level) {
        this.level = level;
    }
}
