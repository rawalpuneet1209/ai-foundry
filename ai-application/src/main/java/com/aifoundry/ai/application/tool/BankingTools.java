package com.aifoundry.ai.application.tool;

import com.aifoundry.ai.application.tool.ToolServices.*;
import java.math.*;
import java.time.*;
import java.util.*;

public final class BankingTools {
  private BankingTools() {}

  private static String required(Request r, String key) {
    Object v = r.arguments().get(key);
    if (v == null || v.toString().isBlank())
      throw new IllegalArgumentException(key + " is required");
    return v.toString();
  }

  public static String mask(String value) {
    if (value == null) return "****";
    String s = value.replaceAll("[^A-Za-z0-9]", "");
    return "****" + s.substring(Math.max(0, s.length() - 4));
  }

  private abstract static class Base implements Tool {
    private final Definition d;

    Base(String n, String desc, boolean approval) {
      d = new Definition(n, desc, Map.of("type", "object"), approval);
    }

    public Definition definition() {
      return d;
    }

    Result success(Request r, Map<String, Object> out) {
      return new Result(r.requestId(), r.toolName(), true, out, null);
    }
  }

  public static final class TransactionLookupTool extends Base {
    public TransactionLookupTool() {
      super("transaction-lookup", "Look up simulated transactions", false);
    }

    public Result execute(Request r) {
      String id = mask(required(r, "accountId"));
      return success(
          r,
          Map.of(
              "accountId",
              id,
              "transactions",
              List.of(
                  Map.of(
                      "id",
                      "txn-demo-1",
                      "timestamp",
                      Instant.now().minus(Duration.ofDays(1)).toString(),
                      "amount",
                      "-42.50",
                      "currency",
                      "USD",
                      "status",
                      "POSTED"))));
    }
  }

  public static final class AccountSummaryTool extends Base {
    public AccountSummaryTool() {
      super("account-summary", "Return a simulated account summary", false);
    }

    public Result execute(Request r) {
      return success(
          r,
          Map.of(
              "accountId",
              mask(required(r, "accountId")),
              "accountType",
              "CHECKING",
              "availableBalance",
              "2450.00",
              "currentBalance",
              "2500.00",
              "currency",
              "USD"));
    }
  }

  public static final class CardDetailsTool extends Base {
    public CardDetailsTool() {
      super("card-details", "Return simulated card details", false);
    }

    public Result execute(Request r) {
      return success(
          r,
          Map.of(
              "cardId",
              mask(required(r, "cardId")),
              "status",
              "ACTIVE",
              "limit",
              "5000.00",
              "availableLimit",
              "4200.00",
              "expiryMonth",
              12,
              "expiryYear",
              2030));
    }
  }

  public static final class FreezeCardTool extends Base {
    public FreezeCardTool() {
      super("freeze-card", "Freeze a simulated card", true);
    }

    public Result execute(Request r) {
      return success(
          r,
          Map.of(
              "cardId",
              mask(required(r, "cardId")),
              "actionId",
              UUID.randomUUID().toString(),
              "status",
              "SIMULATED_FROZEN"));
    }
  }

  public static final class CardReplacementRequestTool extends Base {
    public CardReplacementRequestTool() {
      super("card-replacement-request", "Create a simulated replacement request", true);
    }

    public Result execute(Request r) {
      return success(
          r,
          Map.of(
              "cardId",
              mask(required(r, "cardId")),
              "requestId",
              UUID.randomUUID().toString(),
              "status",
              "SIMULATED_SUBMITTED"));
    }
  }

  public static final class LoanEligibilityCheckTool extends Base {
    public LoanEligibilityCheckTool() {
      super("loan-eligibility-check", "Calculate illustrative loan eligibility", false);
    }

    public Result execute(Request r) {
      BigDecimal income = new BigDecimal(required(r, "monthlyIncome")),
          debt = new BigDecimal(required(r, "monthlyDebt")),
          requested = new BigDecimal(required(r, "requestedAmount"));
      BigDecimal capacity =
          income.multiply(new BigDecimal("0.40")).subtract(debt).max(BigDecimal.ZERO);
      BigDecimal estimate = capacity.multiply(new BigDecimal("36"));
      return success(
          r,
          Map.of(
              "requestedAmount",
              requested,
              "estimatedMaximum",
              estimate,
              "potentiallyEligible",
              estimate.compareTo(requested) >= 0,
              "disclaimer",
              "Illustrative only; this is not an underwriting decision."));
    }
  }
}
