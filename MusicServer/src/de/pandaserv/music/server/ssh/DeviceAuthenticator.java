/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.server.ssh;

import de.pandaserv.music.server.database.DeviceDatabase;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Properties;
import java.util.logging.Level;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authenticate devices using public keys from database.
 *
 * @author ich
 */
public class DeviceAuthenticator implements PublickeyAuthenticator {

    static Logger logger = LoggerFactory.getLogger(DeviceAuthenticator.class);

    @Override
    public boolean authenticate(String username, PublicKey pk, ServerSession ss) {
        logger.info("Authenticating device {} with public key {}",
                new Object[]{username, pk.toString()});
        
        // get stored public key from database
        Properties deviceConfig = DeviceDatabase.getInstance().getDeviceProperties(username);
        if (deviceConfig.containsKey("ssh-forward-publickey")) {
            String encodedPublicKey = deviceConfig.getProperty("ssh-forward-publickey");
            try {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey.getBytes());
                PublicKey deviceKey = keyFactory.generatePublic(publicKeySpec);
                
                return deviceKey.equals(pk);
            } catch (NoSuchAlgorithmException ex) {
                logger.error("Unable to load RSA algorithm: ", ex);
            } catch (InvalidKeySpecException ex) {
                logger.error("The device's publickey stored in the database is invalid: ", ex);
            }
        }
        

        return false;
    }
}
