package br.com.fiap.geladeira.model;

/**
 * Papeis de acesso da API.
 * ADMIN gerencia a rede, VOLUNTARIO opera as geladeiras, DOADOR registra doacoes
 * e SENSOR e o dispositivo IoT que envia leituras.
 */
public enum Papel { ADMIN, VOLUNTARIO, DOADOR, SENSOR }
