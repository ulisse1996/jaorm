package io.github.ulisse1996.jaorm.vendor.specific;

public interface QuoteSpecific {

    QuoteSpecific NONE = toQuote -> toQuote; // TODO Implement me

    String toQuoteIdentifier(String toQuote);
}
