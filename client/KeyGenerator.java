import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class KeyGenerator {
    public static void main(String[] args) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            String b64 = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
            System.out.println("VALID_KEY_START");
            System.out.println(b64);
            System.out.println("VALID_KEY_END");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
