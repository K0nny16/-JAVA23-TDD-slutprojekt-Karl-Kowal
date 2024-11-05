import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BankomatTest {
    private Bank mockBank;
    private Bankomat bankomat;
    private final int cardId = 1234;
    private User user;

    @BeforeEach
    void setUp() {
        mockBank = Mockito.mock(Bank.class);
        bankomat = new Bankomat(mockBank);
        user = new User(cardId, new int[]{5, 6, 7, 8});
    }
    @Test
    @DisplayName("Correct PIN allows login")
    void testCorrectPin() {
        when(mockBank.getUserByCardId(cardId)).thenReturn(user);
        boolean result = bankomat.insertCard(cardId, new int[]{5, 6, 7, 8});
        assertTrue(result);
    }
    @Test
    @DisplayName("Wrong PIN increases failed attempts")
    void testWrongPin() {
        when(mockBank.getUserByCardId(cardId)).thenReturn(user);
        when(mockBank.getFailedAttempts(cardId)).thenReturn(1);
        SecurityException exception = assertThrows(SecurityException.class, () -> bankomat.insertCard(cardId, new int[]{0, 0, 0, 0}));
        assertEquals("Incorrect PIN. Attempts left: 1", exception.getMessage());
        verify(mockBank).incrementFailedAttempts(cardId);
    }
    @Test
    @DisplayName("Three wrong PIN attempts lock card")
    void testLockCard() {
        when(mockBank.getUserByCardId(cardId)).thenReturn(user);
        when(mockBank.getFailedAttempts(cardId)).thenReturn(2);
        SecurityException exception = assertThrows(SecurityException.class, () -> bankomat.insertCard(cardId, new int[]{0, 0, 0, 0}));
        assertEquals("Card is locked due to too many failed attempts!", exception.getMessage());
        verify(mockBank).lockCard(cardId);
    }
    @Test
    @DisplayName("Access Locked Card prevents login")
    void testAccessLockedCard() {
        user.lockCard();
        when(mockBank.getUserByCardId(cardId)).thenReturn(user);
        SecurityException exception = assertThrows(SecurityException.class, () -> bankomat.insertCard(cardId, new int[]{5, 6, 7, 8}));
        assertEquals("User is locked!", exception.getMessage());
    }
    @Test
    @DisplayName("PIN integrity checks for valid length and no negative numbers")
    void testPINIntegrity() {
        assertThrows(NullPointerException.class, () -> bankomat.insertCard(cardId, new int[]{5, -6, 7, 8}));
        assertThrows(IllegalArgumentException.class, () -> bankomat.insertCard(cardId, new int[]{5, 6, 7, 8, 9}));
    }
    @Test
    @DisplayName("User not found throws NullPointerException")
    void testUserNotFound() {
        when(mockBank.getUserByCardId(cardId)).thenReturn(null);
        NullPointerException exception = assertThrows(NullPointerException.class, () -> bankomat.insertCard(cardId, new int[]{5, 6, 7, 8}));
        assertEquals("User not found!", exception.getMessage());
    }
    @Test
    @DisplayName("Deposit is processed correctly")
    void testDeposit() {
        double amount = 100.0;
        bankomat.deposit(cardId, amount);
        verify(mockBank).deposit(cardId, amount);
    }
    @Test
    @DisplayName("Check balance is retrieved correctly")
    void testCheckBalance() {
        double amount = 100.0;
        when(mockBank.getBalance(cardId)).thenReturn(amount);
        bankomat.balance(cardId);
        verify(mockBank).getBalance(cardId);
    }
    @Test
    @DisplayName("Withdraw with sufficient balance is processed correctly")
    void testWithdrawWithSufficientBalance() {
        double amount = 100.0;
        when(mockBank.getBalance(cardId)).thenReturn(200.0);
        bankomat.withdraw(cardId, amount);
        verify(mockBank).withdraw(cardId, amount);
    }
    @Test
    @DisplayName("Withdraw with insufficient balance throws exception and is not processed")
    void testWithdrawWithInsufficientBalance() {
        double amount = 100.0;
        when(mockBank.getBalance(cardId)).thenReturn(50.0);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> bankomat.withdraw(cardId, amount));
        assertEquals("Withdraw amount must be greater than current balance!", exception.getMessage());
        verify(mockBank, never()).withdraw(cardId, amount);
    }
    @Test
    @DisplayName("Bank ID")
    void testBankID() {
        when(mockBank.getBankName()).thenReturn("Test Bank Name");
        assertEquals("Test Bank Name",bankomat.displayBankName());
        verify(mockBank).getBankName();
    }
}
