package ledger_service.exception;

import org.springframework.http.HttpStatus;

public class InvalidEntryException extends LedgerException {

    public InvalidEntryException() {
        super(
                HttpStatus.BAD_REQUEST,
                "INVALID_ENTRY",
                "Invalid ledger entry"
        );
    }

    public InvalidEntryException(String message) {
        super(
                HttpStatus.BAD_REQUEST,
                "INVALID_ENTRY",
                message
        );
    }
}
