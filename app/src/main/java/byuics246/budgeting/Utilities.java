package byuics246.budgeting;

import java.util.Random;

public class Utilities {
    int randomStringLength = 20;

    /**
     * creates a random string of a set length
     * @return
     */
    String randomString() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < randomStringLength; i++) {
            char c = (char)(r.nextInt((int)(Character.MAX_VALUE)));
            sb.append(c);
        }
        return sb.toString();
    }
}
