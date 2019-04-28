package org.chainify.qiwi_blockchain;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.Key;
import java.security.SecureRandom;

public class ECKeyPair {
    private BigInteger priv;
    private byte[] pub;
    private boolean compressed;

    public static ECKeyPair createNew(boolean compressed) {
        X9ECParameters curve = ECNamedCurveTable.getByName("secp256k1");
        ECDomainParameters domainParams = new ECDomainParameters(curve.getCurve(), curve.getG(), curve.getN(), curve.getH(), curve.getSeed());

        SecureRandom secureRandom = new SecureRandom();
        ECKeyGenerationParameters keyParams = new ECKeyGenerationParameters(domainParams, secureRandom);

        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        generator.init(keyParams);
        AsymmetricCipherKeyPair keyPair = generator.generateKeyPair();

        ECPrivateKeyParameters privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
        ECPublicKeyParameters publicKey = (ECPublicKeyParameters) keyPair.getPublic();
        ECKeyPair k = new ECKeyPair();
        k.priv = privateKey.getD();
        k.compressed = compressed;
        if (compressed) {
            ECPoint q = publicKey.getQ();
            k.pub = new ECPoint.Fp(domainParams.getCurve(), q.getX(), q.getY(), true).getEncoded();
        } else {
            k.pub = publicKey.getQ().getEncoded();
        }
        return k;
    }

}