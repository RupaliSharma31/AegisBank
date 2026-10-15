package ledger_service.exception;

import org.springframework.http.HttpStatus;

public class AlreadyReversedException extends LedgerException {

    public AlreadyReversedException() {
        super(
                HttpStatus.CONFLICT,
                "ALREADY_REVERSED",
                "Transaction has already been reversed"
        );
    }

    public AlreadyReversedException(String message) {
        super(
                HttpStatus.CONFLICT,
                "ALREADY_REVERSED",
                message
        );
    }
}
