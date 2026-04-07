package com.allanhenrique.clashapi.controllers.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException; // NOVO
import org.springframework.dao.DataIntegrityViolationException; // NOVO
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice // Esta anotação faz a classe "vigiar" todos os Controllers
public class ResourceExceptionHandler {

   //Transforma erro 500 em 400 quando campos obrigatórios faltam ou estão errados.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", Instant.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Erro de Validação");

        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f ->
                fieldErrors.put(f.getField(), f.getDefaultMessage())
        );

        error.put("errors", fieldErrors);
        error.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


     //Transforma erro 500 em 400 quando o usuário manda "texto" no lugar de um "ID número".

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> typeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", Instant.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Parâmetro Inválido");
        error.put("message", String.format("O parâmetro '%s' recebeu o valor '%s' que é inválido. Esperado: %s",
                e.getName(), e.getValue(), e.getRequiredType().getSimpleName()));
        error.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


     //Quando você tenta buscar um ID que não existe no banco.

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> entityNotFound(NoSuchElementException e, HttpServletRequest request) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", Instant.now());
        error.put("status", HttpStatus.NOT_FOUND.value());
        error.put("error", "Recurso não encontrado");
        error.put("message", "O ID solicitado não existe no nosso banco de dados.");
        error.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }


     //Transforma o erro de enviar "string" onde deveria ser "objeto" em 400.

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> malformedJson(HttpMessageNotReadableException e, HttpServletRequest request) {
        e.printStackTrace();
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", Instant.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Corpo da requisição inválido");
        error.put("message", "O JSON enviado possui erros de sintaxe ou tipos de dados incompatíveis.");
        error.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


     //Pega erros de restrição do Postgres (FK, Unique, etc) e retorna 400.
    @ExceptionHandler({
            org.springframework.dao.DataIntegrityViolationException.class,
            org.hibernate.exception.ConstraintViolationException.class,
            org.springframework.orm.jpa.JpaSystemException.class // <--- ESTE resolve o erro do 0x00!
    })
    public ResponseEntity<Map<String, Object>> databaseErrors(Exception e, HttpServletRequest request) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", Instant.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Erro de Requisição no Banco");
        error.put("message", "A operação violou uma regra do banco ou contém caracteres inválidos.");
        error.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


     //Mostra qualquer outro tipo de erro inesperado.

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> genericError(Exception e, HttpServletRequest request) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", Instant.now());
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("error", "Erro interno no servidor");
        error.put("message", "Ocorreu um erro inesperado. Verifique os logs.");
        error.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}