package ledger_service.exception;

import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends LedgerException {

    public InsufficientFundsException() {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "INSUFFICIENT_FUNDS",
                "Insufficient available balance"
        );
    }

    public InsufficientFundsException(String message) {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "INSUFFICIENT_FUNDS",
                message
        );
    }
}
