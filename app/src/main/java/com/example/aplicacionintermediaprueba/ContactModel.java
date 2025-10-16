package com.example.aplicacionintermediaprueba;

public class ContactModel {
    private int id;
    private String phoneNo;
    private String name;

    public ContactModel(int id, String name, String phoneNo) {
        this.id = id;
        this.phoneNo = validate(phoneNo);
        this.name = name;
    }

    private String validate(String phone) {
        if (phone == null) return "";
        StringBuilder sb = new StringBuilder();
        // Si no empieza con +, le agregamos + y quitamos espacios/guiones
        if (phone.length() > 0 && phone.charAt(0) != '+') {
            sb.append("+");
            for (int i = 0; i < phone.length(); i++) {
                char ch = phone.charAt(i);
                if (ch != ' ' && ch != '-') sb.append(ch);
            }
        } else {
            for (int i = 0; i < phone.length(); i++) {
                char ch = phone.charAt(i);
                if (ch != ' ' && ch != '-') sb.append(ch);
            }
        }
        return sb.toString();
    }

    public int getId() { return id; }
    public String getPhoneNo() { return phoneNo; }
    public String getName() { return name; }
}

