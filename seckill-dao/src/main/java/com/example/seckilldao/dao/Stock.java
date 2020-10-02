package com.example.seckilldao.dao;

/**
 * @author : wucong
 * @Date : 2020/9/22 23:35
 * @Description :
 */
public class Stock {
    private Integer id;

    private String name;

    private Integer count;

    private Integer sale;

    private Integer version;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getSale() {
        return sale;
    }

    public void setSale(Integer sale) {
        this.sale = sale;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        StringBuilder sbr = new StringBuilder();
        sbr.append(getClass().getSimpleName());
        sbr.append(" [");
        sbr.append("Hash= ").append(hashCode());
        sbr.append(", id=").append(id);
        sbr.append(", name=").append(name);
        sbr.append(", count=").append(count);
        sbr.append(", sale=").append(sale);
        sbr.append(", version=").append(version);
        sbr.append("] ");
        return sbr.toString();
    }
}
