package mustceng.forex.repository;

import mustceng.forex.model.ConversionTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ConversionTransactionRepository extends JpaRepository<ConversionTransaction, Long> {

    /**
     * Finds a conversion transaction by its unique transaction ID.
     * @param transactionId The unique identifier of the transaction.
     * @return An Optional containing the ConversionTransaction if found, otherwise empty.
     */
    Optional<ConversionTransaction> findByTransactionId(String transactionId);

    /**
     * Finds a paginated list of conversion transactions by transaction date.
     * @param startDate and endDate The date to filter transactions by.
     * @param pageable Pagination information.
     * @return A Page of ConversionTransaction objects.
     */
    Page<ConversionTransaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
