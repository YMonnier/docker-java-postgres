package com.yseemonnier.dbDocker;

/**
 * Project Jdbc.
 * Package fr.univtln.ysee.jdbc.
 * File Client.java.
 * Created by Ysee on 16/10/2016 - 21:56.
 * www.yseemonnier.com
 * https://github.com/YMonnier
 */
public class Client {
    private String name;
    private int age;

    public Client(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
