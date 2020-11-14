package gtanks.exceptions;

import gtanks.logger.Logger;

public class GTanksServerException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public GTanksServerException(String error) {
        super(error);
        Logger.error(this);
    }
}
