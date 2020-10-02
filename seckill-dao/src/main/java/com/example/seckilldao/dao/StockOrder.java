package com.example.seckilldao.dao;

import java.util.Date;

/**
 * @author : wucong
 * @Date : 2020/9/22 23:40
 * @Description :
 */
public class StockOrder {
    private Integer id;

    private Integer sid;

    private String name;

    private Integer userId;

    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSid() {
        return sid;
    }

    public void setSid(Integer sid) {
        this.sid = sid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StackOrder{");
        sb.append("Hash = ").append(hashCode());
        sb.append("id=").append(id);
        sb.append(", sid=").append(sid);
        sb.append(", name='").append(name).append('\'');
        sb.append(", userId=").append(userId);
        sb.append(", createTime=").append(createTime);
        sb.append('}');
        return sb.toString();
    }
}
