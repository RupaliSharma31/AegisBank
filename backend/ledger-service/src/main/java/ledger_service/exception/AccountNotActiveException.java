package ledger_service.exception;

import org.springframework.http.HttpStatus;

public class AccountNotActiveException extends LedgerException {

    public AccountNotActiveException() {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "ACCOUNT_NOT_ACTIVE",
                "Account is not active"
        );
    }

    public AccountNotActiveException(String message) {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "ACCOUNT_NOT_ACTIVE",
                message
        );
    }
}