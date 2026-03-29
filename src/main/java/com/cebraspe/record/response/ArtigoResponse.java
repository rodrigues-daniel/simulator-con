package com.cebraspe.record.response;

public record ArtigoResponse(
        Long id,
        String identificador,
        String texto,
        Boolean destaque) {
}