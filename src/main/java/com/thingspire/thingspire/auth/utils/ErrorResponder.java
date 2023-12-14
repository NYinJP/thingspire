package com.thingspire.thingspire.auth.utils;

import com.google.gson.Gson;
import com.thingspire.thingspire.auth.handler.MemberAccessDeniedHandler;
import com.thingspire.thingspire.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.thingspire.thingspire.auth.handler.MemberAuthenticationEntryPoint.logExceptionMessage;

public class ErrorResponder {
    public static void sendErrorResponse(HttpServletResponse response, HttpStatus status) throws IOException {
        Gson gson = new Gson();
        ErrorResponse errorResponse = ErrorResponse.of(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(status.value());
        response.getWriter().write(gson.toJson(errorResponse, ErrorResponse.class));
    }

    public static void sendErrorResponse(HttpServletResponse response,AuthenticationException authException, Exception exception) throws IOException {
        Gson gson = new Gson();
        String errorMessage = exception != null ? exception.getMessage() : authException.getMessage();
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.UNAUTHORIZED, errorMessage);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        response.getWriter().write(gson.toJson(errorResponse, ErrorResponse.class));


//        logExceptionMessage(authException, exception);
//
//        ErrorResponse response = ErrorResponse.of(HttpStatus.UNAUTHORIZED, errorMessage);
//        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    public static void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        Gson gson = new Gson();
        ErrorResponse errorResponse = ErrorResponse.of(status, message);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(status.value());

        if (message.equals("Invalid JWT signature") ||
                message.equals("Request failed with status code 401") || message.equals("Request failed with status code 402") ||
                message.equals("An unexpected error occurred")) {
            response.getWriter().write(gson.toJson(new ErrorResponse[]{errorResponse}, ErrorResponse[].class));
        } else {
            response.getWriter().write(gson.toJson(errorResponse, ErrorResponse.class));
        }
    }

}
