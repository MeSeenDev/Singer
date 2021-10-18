package ru.meseen.dev.singer


import com.android.apksig.ApkVerifier
import ru.meseen.dev.singer.exceptions.NotVerifiedException
import ru.meseen.dev.singer.verify.Certificate
import ru.meseen.dev.singer.verify.Signer
import java.io.File
import java.security.MessageDigest
import java.security.PublicKey
import java.security.cert.X509Certificate
import java.security.interfaces.DSAKey
import java.security.interfaces.ECKey
import java.security.interfaces.RSAKey


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
            number = signerNumber.plus(1)
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

