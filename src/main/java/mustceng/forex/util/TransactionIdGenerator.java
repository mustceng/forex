package mustceng.forex.util;

import java.util.UUID;

public class TransactionIdGenerator {

    /**
     * Generates a unique transaction identifier.
     * @return A unique String representing a transaction ID.
     */
    public static String generateUniqueTransactionId() {
        return UUID.randomUUID().toString();
    }
}
