package ledger_service.exception;

import org.springframework.http.HttpStatus;

public class CurrencyMismatchException extends LedgerException {

    public CurrencyMismatchException() {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "CURRENCY_MISMATCH",
                "Entry currency does not match account currency"
        );
    }

    public CurrencyMismatchException(String message) {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "CURRENCY_MISMATCH",
                message
        );
    }
}
