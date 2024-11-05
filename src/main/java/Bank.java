public interface Bank {
    User getUserByCardId(int cardId);
    void lockCard(int cardId);
    int getFailedAttempts(int cardId);
    void incrementFailedAttempts(int cardId);
    double getBalance(int cardId);
    void deposit(int cardId, double amount);
    void withdraw(int cardId, double amount);
    String getBankName();
}
