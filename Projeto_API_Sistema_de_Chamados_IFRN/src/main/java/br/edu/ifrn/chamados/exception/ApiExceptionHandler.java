package br.edu.ifrn.chamados.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {
    record Erro(Instant timestamp,int status,String mensagem,String caminho) {}
    private ResponseEntity<Erro> resposta(HttpStatus s,String m,HttpServletRequest r){return ResponseEntity.status(s).body(new Erro(Instant.now(),s.value(),m,r.getRequestURI()));}
    @ExceptionHandler(Erros.NaoEncontrado.class) ResponseEntity<Erro> notFound(Erros.NaoEncontrado e,HttpServletRequest r){return resposta(HttpStatus.NOT_FOUND,e.getMessage(),r);}
    @ExceptionHandler({Erros.RegraNegocio.class,Erros.Conflito.class}) ResponseEntity<Erro> conflict(RuntimeException e,HttpServletRequest r){return resposta(HttpStatus.CONFLICT,e.getMessage(),r);}
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<Erro> invalid(MethodArgumentNotValidException e,HttpServletRequest r){return resposta(HttpStatus.BAD_REQUEST,e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).collect(Collectors.joining("; ")),r);}
    @ExceptionHandler({ConstraintViolationException.class,IllegalArgumentException.class}) ResponseEntity<Erro> bad(Exception e,HttpServletRequest r){return resposta(HttpStatus.BAD_REQUEST,e.getMessage(),r);}
    @ExceptionHandler(HttpMessageNotReadableException.class) ResponseEntity<Erro> unreadable(HttpMessageNotReadableException e,HttpServletRequest r){return resposta(HttpStatus.BAD_REQUEST,"Corpo JSON inválido ou ausente.",r);}
}


