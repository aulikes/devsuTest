package com.devsu.user.application.port;

/**
 * Puerto de consulta de catálogos usado por la capa de aplicación.
 * Esta interfaz permite validar códigos antes de orquestar la persistencia.
 */
public interface CatalogQueryPort {

    // Se verifica si existe el código de género proporcionado
    boolean genderExists(String genderCode);

    // Se verifica si existe el código de tipo de identificación proporcionado
    boolean identificationTypeExists(String idTypeCode);
}
