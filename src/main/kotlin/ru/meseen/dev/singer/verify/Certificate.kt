package ru.meseen.dev.singer.verify

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