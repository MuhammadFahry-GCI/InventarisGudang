package com.inventaris.model;

public class User {
    private int idUser;
    private String nama;
    private String username;
    private String role;

    public User(int idUser, String nama, String username, String role) {
        this.idUser = idUser;
        this.nama = nama;
        this.username = username;
        this.role = role;
    }

    public int getIdUser() {
        return idUser;
    }

    public String getNama() {
        return nama;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public boolean isAdmin() {
        return "admin".equals(role);
    }
}
