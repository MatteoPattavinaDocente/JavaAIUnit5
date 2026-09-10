package com.example.demo.security;

import com.example.demo.exception.NonAutenticatoException;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/** Risolve i parametri @CurrentUser, fallendo con 401 se il token manca o non e' valido. */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && UtenteAutenticato.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mav,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        Object utente = webRequest.getAttribute(JwtAuthFilter.ATTRIBUTO_UTENTE, RequestAttributes.SCOPE_REQUEST);
        if (utente == null) {
            throw new NonAutenticatoException("Token mancante, non valido o scaduto");
        }
        return utente;
    }
}
