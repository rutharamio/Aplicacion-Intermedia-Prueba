package com.example.aplicacionintermediaprueba;

public class ContactModel {
    private int id;
    private String phoneNo;
    private String name;

    // Constructor con validación del número
    public ContactModel(int id, String name, String phoneNo) {
        this.id = id;
        this.name = name;
        this.phoneNo = validatePhone(phoneNo);
    }

    // Método para validar el número de teléfono
    private String validatePhone(String phone) {
        if (phone == null) return "";
        StringBuilder sb = new StringBuilder();

        // Si no empieza con '+', lo agregamos y limpiamos espacios/guiones
        if (phone.length() > 0 && phone.charAt(0) != '+') {
            sb.append("+");
        }

        for (int i = 0; i < phone.length(); i++) {
            char ch = phone.charAt(i);
            if (ch != ' ' && ch != '-') {
                sb.append(ch);
            }
        }

        return sb.toString();
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getName() {
        return name;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = validatePhone(phoneNo);
    }
}
