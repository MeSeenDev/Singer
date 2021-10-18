package ru.meseen.dev.singer.verify

import javax.security.auth.x500.X500Principal

data class Signer(
    var number: Int = 0,
    var certificateDN: X500Principal = X500Principal(""),
    var certificateSHA256: String = "",
    var certificateSHA1: String = "",
    var certificateMD5: String = "",
    var algorithm: String = "",
    var keySize: Int = -1,
    var publicKeySHA256: String = "",
    var publicKeySHA1: String = "",
    var publicKeyMD5: String = "",
) {
    fun standardOut() =
        "Signer #$number certificate DN: $certificateDN\n" +
                "Signer #$number certificate SHA-256 digest: $certificateSHA256\n" +
                "Signer #$number certificate SHA-1 digest: $certificateSHA1\n" +
                "Signer #$number certificate MD5 digest: $certificateMD5\n" +
                "Signer #$number key algorithm: RSA\n" +
                "Signer #$number key size (bits): $keySize\n" +
                "Signer #$number public key SHA-256 digest: $publicKeySHA256\n" +
                "Signer #$number public key SHA-1 digest: $publicKeySHA1\n" +
                "Signer #$number public key MD5 digest: $publicKeyMD5  \n"

}