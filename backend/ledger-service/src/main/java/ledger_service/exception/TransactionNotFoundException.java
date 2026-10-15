package ledger_service.exception;

import org.springframework.http.HttpStatus;

public class TransactionNotFoundException extends LedgerException {

    public TransactionNotFoundException() {
        super(
                HttpStatus.NOT_FOUND,
                "TRANSACTION_NOT_FOUND",
                "Transaction not found"
        );
    }

    public TransactionNotFoundException(String message) {
        super(
                HttpStatus.NOT_FOUND,
                "TRANSACTION_NOT_FOUND",
                message
        );
    }
}
