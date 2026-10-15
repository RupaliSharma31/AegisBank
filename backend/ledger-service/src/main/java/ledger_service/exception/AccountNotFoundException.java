package ledger_service.exception;

import org.springframework.http.HttpStatus;

public class AccountNotFoundException extends LedgerException {

    public AccountNotFoundException() {
        super(
                HttpStatus.NOT_FOUND,
                "ACCOUNT_NOT_FOUND",
                "Account not found"
        );
    }

    public AccountNotFoundException(String message) {
        super(
                HttpStatus.NOT_FOUND,
                "ACCOUNT_NOT_FOUND",
                message
        );
    }
}
