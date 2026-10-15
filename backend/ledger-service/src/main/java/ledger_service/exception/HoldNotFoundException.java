package ledger_service.exception;

import org.springframework.http.HttpStatus;

public class HoldNotFoundException extends LedgerException {

    public HoldNotFoundException() {
        super(
                HttpStatus.NOT_FOUND,
                "HOLD_NOT_FOUND",
                "Hold not found"
        );
    }

    public HoldNotFoundException(String message) {
        super(
                HttpStatus.NOT_FOUND,
                "HOLD_NOT_FOUND",
                message
        );
    }
}
