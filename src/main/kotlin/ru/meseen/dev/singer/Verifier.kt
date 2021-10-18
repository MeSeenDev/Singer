package ru.meseen.dev.singer


import com.android.apksig.ApkVerifier
import ru.meseen.dev.singer.exceptions.NotVerifiedException
import java.io.File
import java.security.MessageDigest
import java.security.PublicKey
import java.security.cert.X509Certificate
import java.security.interfaces.DSAKey
import java.security.interfaces.ECKey
import java.security.interfaces.RSAKey
import javax.security.auth.x500.X500Principal


/**
 * @author Vyacheslav Doroshenko
 */
object Verifier {

    fun verify(file: File): Results<Certificate> {
        val verifier = ApkVerifier.Builder(file).build()
        return try {
            verifier.verify().run {
                return if ((isVerified)
                ) {
                    val certificate = Certificate()
                    certificate.isVerifiedUsingV1Scheme = isVerifiedUsingV1Scheme
                    certificate.isVerifiedUsingV2Scheme = isVerifiedUsingV2Scheme
                    certificate.isVerifiedUsingV3Scheme = isVerifiedUsingV3Scheme
                    certificate.isVerifiedUsingV4Scheme = isVerifiedUsingV4Scheme
                    certificate.isSourceStampVerified = isSourceStampVerified
                    certificate.numberOfSignerCertificates = signerCertificates.size

                    certificate.signers = signerCertificates
                        .mapIndexed { index, x509Certificate -> createSinger(index, x509Certificate) }
                    Results.Success(certificate)
                } else {
                    Results.Error(NotVerifiedException(if (errors.isNotEmpty()) errors.joinToString("\n") else "Не удалось верифицировать"))
                }
            }
        } catch (e: Throwable) {
            Results.Error(e)
        }
    }

    private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

    private fun createSinger(signerNumber: Int, cert: X509Certificate): Signer {
        val sha256: MessageDigest = MessageDigest.getInstance("SHA-256")
        val sha1: MessageDigest = MessageDigest.getInstance("SHA-1")
        val md5: MessageDigest = MessageDigest.getInstance("MD5")

        return Signer().apply {
            number = signerNumber + 1
            certificateDN = cert.subjectX500Principal

            val encodedCert = cert.encoded
            certificateSHA256 = sha256.digest(encodedCert).toHexString()
            certificateSHA1 = sha1.digest(encodedCert).toHexString()
            certificateMD5 = md5.digest(encodedCert).toHexString()

            val publicKey = cert.publicKey
            algorithm = publicKey.algorithm
            keySize = getKeySize(publicKey)
            val encodedKey = publicKey.encoded
            publicKeySHA256 = sha256.digest(encodedKey).toHexString()
            publicKeySHA1 = sha1.digest(encodedKey).toHexString()
            publicKeyMD5 = md5.digest(encodedKey).toHexString()
        }
    }

    private fun getKeySize(publicKey: PublicKey?): Int {
        var keySize1 = -1
        when (publicKey) {
            is RSAKey -> {
                keySize1 = (publicKey as RSAKey).modulus.bitLength()
            }
            is ECKey -> {
                keySize1 = (publicKey as ECKey).params.order.bitLength()
            }
            is DSAKey -> {
                val dsaParams = (publicKey as DSAKey).params
                if (dsaParams != null) {
                    keySize1 = dsaParams.p.bitLength()
                }
            }
        }
        return keySize1
    }

}

data class Certificate(
    var isVerifiedUsingV1Scheme: Boolean = false,
    var isVerifiedUsingV2Scheme: Boolean = false,
    var isVerifiedUsingV3Scheme: Boolean = false,
    var isVerifiedUsingV4Scheme: Boolean = false,
    var isSourceStampVerified: Boolean = false,
    var numberOfSignerCertificates: Int = 0,
    var signers: List<Signer> = listOf()
) {
    fun standardOut() =
        "Verified using v1 scheme (JAR signing): ${isVerifiedUsingV1Scheme}\n" +
                "Verified using v2 scheme (APK Signature Scheme v2): ${isVerifiedUsingV2Scheme}\n" +
                "Verified using v3 scheme (APK Signature Scheme v3): ${isVerifiedUsingV3Scheme}\n" +
                "Verified using v4 scheme (APK Signature Scheme v4): ${isVerifiedUsingV4Scheme}\n" +
                "Verified for SourceStamp: ${isSourceStampVerified}\n" +
                "Number of signers: ${numberOfSignerCertificates}\n"

}

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
