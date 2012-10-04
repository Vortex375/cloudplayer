package de.pandaserv.music.server.ssh;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read OpenSSH key files.
 *
 * OpenSSH key files have the following format: [length (uint32)|format
 * string|length (uint32)|exponent|length (uint32)|modulus]
 *
 * @author ich
 */
//TODO: error checking
public class OpenSSHKeyReader {

    public class KeyParseException extends Exception {

        public KeyParseException(String msg) {
            super(msg);
        }
    }
    static Logger logger = LoggerFactory.getLogger(OpenSSHKeyReader.class);
    private byte[] data;
    private int pos;
    private String type;

    public OpenSSHKeyReader(byte[] data) throws KeyParseException {
        this.data = data;
        pos = 0;

        type = readString();
        logger.debug("Key type: {}", type);
    }

    public PublicKey getKey() throws KeyParseException {
        if (type.equals("ssh-rsa")) {
            return readKeyRSA();
        } else if (type.equals("ssh-dss")) {
            return readKeyDSA();
        } else {
            throw new KeyParseException(("unknown key type: " + type));
        }
    }

    private PublicKey readKeyRSA() throws KeyParseException {
        final BigInteger e, n;
        e = new BigInteger(readKeyElement());
        n = new BigInteger(readKeyElement());

        final KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(new RSAPublicKeySpec(n, e));
        } catch (InvalidKeySpecException ex) {
            throw new KeyParseException("malformed key");
        } catch (NoSuchAlgorithmException ex) {
            throw new KeyParseException(("unable to get factory for RSA keys"));
        }
    }

    private PublicKey readKeyDSA() throws KeyParseException {
        final BigInteger p, q, g, y;
        p = new BigInteger(readKeyElement());
        q = new BigInteger(readKeyElement());
        g = new BigInteger(readKeyElement());
        y = new BigInteger(readKeyElement());;

        final KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("DSA");
            return keyFactory.generatePublic(new DSAPublicKeySpec(y, p, q, g));
        } catch (InvalidKeySpecException ex) {
            throw new KeyParseException("malformed key");
        } catch (NoSuchAlgorithmException ex) {
            throw new KeyParseException(("unable to get factory for DSA keys"));
        }
    }

    /**
     * Return the exponent and modulus of the public key. The first invocation
     * returns the exponent, the second invocation returns the modulus. Calling
     * this function more than two times is an error.
     *
     * @return
     */
    private byte[] readKeyElement() throws KeyParseException {
        int len = readUInt32();
        logger.debug("found field with length {} at ({}/{})",
                new Object[]{len, pos, data.length});

        if (len < 0 || len > data.length - pos) {
            throw new KeyParseException("invalid length field: " + len);
        }
        byte[] ret = new byte[len];

        System.arraycopy(data, pos, ret, 0, len);
        pos += len;
        return ret;
    }

    private int readUInt32() throws KeyParseException {
        if (pos >= data.length - 4) {
            throw new KeyParseException("no more data");
        }
        int ret = 0;
        for (int i = 0; i < 4; i++) {
            ret += (data[pos + i] << 8 * (3 - i));
        }
        pos += 4;
        return ret;
    }

    private String readString() throws KeyParseException {
        int len = readUInt32();
        if (pos >= data.length - len) {
            throw new KeyParseException("no more data");
        }
        
        String ret = new String(data, pos, len);
        pos += len;
        return ret;
    }
}
