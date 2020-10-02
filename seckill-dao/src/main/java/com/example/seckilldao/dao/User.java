package com.example.seckilldao.dao;

/**
 * @author : wucong
 * @Date : 2020/10/2 10:45
 * @Description :
 */
public class User {
    private Integer id;
    private String userName;

    public User(Integer id, String userName) {
        this.id = id;
        this.userName = userName;
    }

    public User() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userName=").append(userName);
        sb.append("]");
        return sb.toString();
    }
}
