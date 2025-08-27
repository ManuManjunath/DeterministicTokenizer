import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;

public class DeterministicTokenizer {

    // 32-byte key for AES-256
    private static final String SECRET_KEY = "0123456789abcdefghijklmnopqrstuv";
    // Fixed IV (deterministic) – normally should be random, but we keep it fixed here
    private static final String INIT_VECTOR = "001_init_vector_";

    // Encrypt (Tokenize)
    public static String tokenize(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            throw new RuntimeException("Error while tokenizing: " + ex.getMessage(), ex);
        }
    }

    // Decrypt (De-tokenize)
    public static String detokenize(String token) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.getDecoder().decode(token));
            return new String(original);
        } catch (Exception ex) {
            throw new RuntimeException("Error while detokenizing: " + ex.getMessage(), ex);
        }
    }

    public static void main(String[] args) {
        String creditCardNum = "1234 5678 9012 3456";

        // Tokenize
        String token = tokenize(creditCardNum);
        System.out.println("Tokenized: " + token);

        // Detokenize
        String original = detokenize(token);
        System.out.println("Detokenized: " + original);
    }
}
