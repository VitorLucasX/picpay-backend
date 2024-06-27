package com.picpay.desafio.authorization;

public record Authorization(String status, Data data) {
    public static record Data(boolean authorization) {}

    public boolean isAuthorized() {
        return status.equals("Autorizado");
    }

    public static void main(String[] args) {
        Authorization authorization = new Authorization("success", new Data(true));

        System.out.println("Status: " + authorization.status());
        System.out.println("Authorization: " + authorization.data().authorization());
    }
}
