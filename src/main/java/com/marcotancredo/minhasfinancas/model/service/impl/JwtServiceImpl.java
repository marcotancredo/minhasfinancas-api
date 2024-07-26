package com.marcotancredo.minhasfinancas.model.service.impl;

import com.marcotancredo.minhasfinancas.model.entity.Usuario;
import com.marcotancredo.minhasfinancas.model.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

    public static final DateTimeFormatter FORMAT_HOUR_MINUTE = DateTimeFormatter.ofPattern("HH:mm");
    @Value("${jwt.expiracao}")
    private String expiracao;
    @Value("${jwt.chave-assinatura}")
    private String chaveAssinatura;

    @Override
    public String gerarToken(Usuario usuario) {
        LocalDateTime dataHoraExpiraEm = LocalDateTime.now().plusMinutes(Long.parseLong(expiracao));
        Date dataExpiraEm = Date.from(dataHoraExpiraEm.atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .setExpiration(dataExpiraEm)
                .setSubject(usuario.getEmail())
                .claim("userId", usuario.getId())
                .claim("nome", usuario.getNome())
                .claim("horaExpiracao", dataHoraExpiraEm.toLocalTime().format(FORMAT_HOUR_MINUTE))
                .signWith(SignatureAlgorithm.HS512, chaveAssinatura)
                .compact();
    }

    @Override
    public Claims obterClaims(String token) throws ExpiredJwtException {
        return Jwts.parser()
                .setSigningKey(chaveAssinatura)
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public boolean isTokenValido(String token) {
        try {
            Claims claims = obterClaims(token);
            LocalDateTime dataHoraExpira = claims.getExpiration().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();

            return !LocalDateTime.now().isAfter(dataHoraExpira);
        } catch (ExpiredJwtException ex) {
            return false;
        }
    }

    @Override
    public String obterLoginUsuario(String token) {
        Claims claims = obterClaims(token);
        return claims.getSubject();
    }
}
