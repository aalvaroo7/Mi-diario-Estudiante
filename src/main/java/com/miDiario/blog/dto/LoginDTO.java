package com.miDiario.blog.dto;

public class LoginDTO {

    private String identificador;  // Puede ser email o nombreUsuario
    private String password;

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
//Dios salvaemos a todos. Amén.