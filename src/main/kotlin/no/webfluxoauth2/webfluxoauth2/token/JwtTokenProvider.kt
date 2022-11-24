package no.webfluxoauth2.webfluxoauth2.token

import io.jsonwebtoken.*
import no.webfluxoauth2.webfluxoauth2.config.AppProperties
import no.webfluxoauth2.webfluxoauth2.model.UserPrincipal
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtTokenProvider(private val appProperties: AppProperties) : ITokenProvider {
    override fun createToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserPrincipal
        val now = Date()
        val expiryDate = Date(now.time + appProperties.auth.tokenExpirationMsec)

        val claims: MutableMap<String, Any> = HashMap()
        claims["role"] = userPrincipal.authorities

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userPrincipal.id.toString())
            .setIssuedAt(Date())
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, appProperties.auth.tokenSecret)
            .compact()
    }

    override fun validateToken(authToken: String?): Boolean {
        try {
            Jwts.parser().setSigningKey(appProperties.auth.tokenSecret).parseClaimsJws(authToken)
            return true
        } catch (ex: SignatureException) {
            logger.error("Invalid JWT signature")
        } catch (ex: MalformedJwtException) {
            logger.error("Invalid JWT token")
        } catch (ex: ExpiredJwtException) {
            logger.error("Expired JWT token")
        } catch (ex: UnsupportedJwtException) {
            logger.error("Unsupported JWT token")
        } catch (ex: IllegalArgumentException) {
            logger.error("No JWT token provided")
        }
        return false
    }

    fun getUserIdFromToken(token: String?): Long {
        val claims = getAllClaimsFromToken(token)
        return claims.subject.toLong()
    }

    fun getUserRoleFromToken(token: String?): String {
        val claims = getAllClaimsFromToken(token)
        return claims["role"] as String
    }

    private fun getAllClaimsFromToken(token: String?): Claims {
        return Jwts.parser()
            .setSigningKey(appProperties.auth.tokenSecret)
            .parseClaimsJws(token)
            .body
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    }
}