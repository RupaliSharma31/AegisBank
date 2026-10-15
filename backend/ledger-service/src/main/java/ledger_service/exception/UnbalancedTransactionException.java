package ledger_service.exception;

import org.springframework.http.HttpStatus;

public class UnbalancedTransactionException extends LedgerException {

    public UnbalancedTransactionException() {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "UNBALANCED_TRANSACTION",
                "Debits and credits must balance for each currency"
        );
    }

    public UnbalancedTransactionException(String message) {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "UNBALANCED_TRANSACTION",
                message
        );
    }
}