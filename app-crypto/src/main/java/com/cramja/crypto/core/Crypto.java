package com.cramja.crypto.core;

import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.security.auth.x500.X500PrivateCredential;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

public final class Crypto {

    private Crypto() {
    }

    /**
     * @return date in second precision
     */
    private static Date dateWithOffset(Date date, long offsetSeconds) {
        return new Date(((date.getTime() / 1000) + offsetSeconds) * 1000);
    }

    public static KeyPair generateRsaKeyPair() {
        final KeyPairGenerator kpg;
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }

    public static X509CertificateHolder createTrustAnchor(KeyPair keyPair, String algorithm)
            throws OperatorCreationException {
        X500Name name = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.C, "US")
                .addRDN(BCStyle.ST, "CA")
                .addRDN(BCStyle.L, "Palo Alto")
                .addRDN(BCStyle.O, "net.spehl")
                .addRDN(BCStyle.CN, "Test Root Certificate")
                .build();

        Date now = new Date();
        X509v1CertificateBuilder cert = new JcaX509v1CertificateBuilder(
                name,
                BigIntegers.createRandomBigInteger(128, new SecureRandom()),
                dateWithOffset(now, 0),
                dateWithOffset(now, TimeUnit.DAYS.toSeconds(365)),
                name,
                keyPair.getPublic());
        ContentSigner signer = new JcaContentSignerBuilder(algorithm)
                .setProvider("BC")
                .build(keyPair.getPrivate());
        return cert.build(signer);
    }

    public static X500PrivateCredential createSelfSignedCredentials() {
        try {
            JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter()
                    .setProvider("BC");
            KeyPair keyPair = generateRsaKeyPair();
            X509CertificateHolder selfSignedHolder = createTrustAnchor(keyPair, "SHA256withRSA");
            X509Certificate cert = certConverter.getCertificate(selfSignedHolder);
            return new X500PrivateCredential(
                    cert,
                    keyPair.getPrivate(),
                    "default"
            );
        } catch (Exception e) {
            throw new RuntimeException("failed to create self-signed credentials", e);
        }
    }

    public static String encodePem(Certificate certificate) {
        try {
            return encodePem("CERTIFICATE", certificate.getEncoded());
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodePem(PrivateKey key) {
        return encodePem("RSA PRIVATE KEY", key.getEncoded());
    }

    private static String encodePem(String type, byte[] encoded) {
        try {
            final StringWriter certWriter = new StringWriter();
            try (PemWriter writer = new PemWriter(certWriter)) {
                PemObject pemObject = new PemObject(type, encoded);
                writer.writeObject(pemObject);
            }
            return certWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException("unable to encode pem object", e);
        }
    }

}
