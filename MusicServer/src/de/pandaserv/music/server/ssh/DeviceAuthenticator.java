/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.server.ssh;

import de.pandaserv.music.server.database.DeviceDatabase;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Properties;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

/**
 * Authenticate devices using public keys from database.
 *
 * @author ich
 */
public class DeviceAuthenticator implements PublickeyAuthenticator {

    static Logger logger = LoggerFactory.getLogger(DeviceAuthenticator.class);

    @Override
    public boolean authenticate(String username, PublicKey pk, ServerSession ss) {
        logger.info("Authenticating ssh connection for device {}", username);
        
        // get stored public key from database
        Properties deviceConfig = DeviceDatabase.getInstance().getDeviceProperties(username);
        
        if (deviceConfig.containsKey("ssh-forward-publickey")) {
            // the key in the database is base64 encoded
            String base64key = deviceConfig.getProperty("ssh-forward-publickey");
            BASE64Decoder decoder = new BASE64Decoder();
            try {
                byte[] encodedPublicKey = decoder.decodeBuffer(base64key);

                // TODO: maybe support other formats too
                // parse OpenSSH key
                OpenSSHKeyReader keyReader = new OpenSSHKeyReader(encodedPublicKey);
                KeyFactory rsaFactory = KeyFactory.getInstance("RSA");
                BigInteger exp = new BigInteger(keyReader.getKeyElement());
                BigInteger mod = new BigInteger(keyReader.getKeyElement());
                RSAPublicKeySpec rsaKey = new RSAPublicKeySpec(mod, exp);
                PublicKey devicePublicKey = rsaFactory.generatePublic(rsaKey);
                
                logger.debug("Offered public key: {}", pk);
                logger.debug("Stored public key:  {}", devicePublicKey);
                
                return devicePublicKey.equals(pk);
            } catch (IOException ex) { // thrown from BASE64Decoder
                logger.error("Unable to decode the device's stored public key.", ex);
            } catch (NoSuchAlgorithmException ex) {
                logger.error("Unable to create RSA key factory.", ex);
            } catch (OpenSSHKeyReader.KeyParseException | InvalidKeySpecException ex) {
                logger.error("Unable to parse the device's stored public key.", ex);
            }

        } else {
            logger.info("Rejecting ssh connection for unknown username {}", username);
        }
        return false;
    }
}
