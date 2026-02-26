package com.duelmath.core.config

import com.duelmath.BuildConfig

// ── Wrapper de seguridad para valores sensibles ───────────────────────────
// toString() retorna "***" para evitar logging accidental en producción.
@JvmInline
value class Secret(val value: String) {
    override fun toString(): String = "***"
}

// ── Modelos de configuración jerárquica ───────────────────────────────────

data class NetworkConfig(
    val apiBaseUrl: String,
    val wsBaseUrl: String,
    val connectTimeoutSeconds: Long,
    val readTimeoutSeconds: Long,
    val writeTimeoutSeconds: Long
)

data class AuthConfig(
    val googleWebClientId: Secret
)

// ── Singleton de configuración cargado desde BuildConfig ─────────────────
// Todos los módulos Hilt consumen de aquí. Ningún valor hardcodeado fuera
// de este objeto.
object AppConfig {

    val network: NetworkConfig by lazy {
        NetworkConfig(
            apiBaseUrl            = BuildConfig.API_BASE_URL,
            wsBaseUrl             = BuildConfig.WS_BASE_URL,
            connectTimeoutSeconds = BuildConfig.CONNECT_TIMEOUT_SECONDS.toLong(),
            readTimeoutSeconds    = BuildConfig.READ_TIMEOUT_SECONDS.toLong(),
            writeTimeoutSeconds   = BuildConfig.WRITE_TIMEOUT_SECONDS.toLong()
        )
    }

    val auth: AuthConfig by lazy {
        AuthConfig(
            googleWebClientId = Secret(BuildConfig.GOOGLE_WEB_CLIENT_ID)
        )
    }
}
