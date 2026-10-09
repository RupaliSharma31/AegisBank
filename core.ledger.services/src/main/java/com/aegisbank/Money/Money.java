package com.aegisbank.Money;

import java.util.Currency;
import java.util.Objects;
import java.util.Set;

public record Money(long minorUnits, String currency) {

    private static final Set<String> SUPPORTED = Set.of("INR", "USD", "EUR", "GBP");

    public Money {
        if (currency == null) {
            throw new IllegalArgumentException("Currency is required");
        }
        if (!SUPPORTED.contains(currency)) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }
        Currency.getInstance(currency);
    }

    public Money plus(Money other) {
        sameCurrency(other);
        return new Money(Math.addExact(minorUnits, other.minorUnits), currency);
    }

    public Money minus(Money other) {
        sameCurrency(other);
        return new Money(Math.subtractExact(minorUnits, other.minorUnits), currency);
    }

    public boolean isPositive() {
        return minorUnits > 0;
    }

    private void sameCurrency(Money other) {
        Objects.requireNonNull(other, "Other Money is required");
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + currency + " vs " + other.currency);
        }
    }
}
