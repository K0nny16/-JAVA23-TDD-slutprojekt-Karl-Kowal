import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//85%
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
    @DisplayName("Correct PIN")
    void testCorrectPin() {
        when(mockBank.getUserByCardId(cardId)).thenReturn(user);
        boolean result = bankomat.insertCard(cardId, new int[]{5, 6, 7, 8});
        assertTrue(result);
    }
    @Test
    @DisplayName("Wrong PIN increases failed attempts and shows remaining attempts")
    void testWrongPin() {
        when(mockBank.getUserByCardId(cardId)).thenReturn(user);
        when(mockBank.getFailedAttempts(cardId)).thenReturn(1);
        SecurityException exception = assertThrows(SecurityException.class, () -> bankomat.insertCard(cardId, new int[]{0, 0, 0, 0}));
        assertAll(
                () -> assertEquals("Incorrect PIN. Attempts left: 1", exception.getMessage()),
                () -> verify(mockBank).incrementFailedAttempts(cardId),
                () -> verify(mockBank,times(2)).getFailedAttempts(cardId)
        );
    }
    @Test
    @DisplayName("Three wrong PIN attempts lock card and shows locked message")
    void testLockCard() {
        when(mockBank.getUserByCardId(cardId)).thenReturn(user);
        when(mockBank.getFailedAttempts(cardId)).thenReturn(3);
        SecurityException exception = assertThrows(SecurityException.class, () -> bankomat.insertCard(cardId, new int[]{0, 0, 0, 0}));
        assertAll(
                () -> assertEquals("Card is locked due to too many failed attempts!", exception.getMessage()),
                () -> verify(mockBank).lockCard(cardId),
                () -> verify(mockBank).getFailedAttempts(cardId)
        );
    }
    @Test
    @DisplayName("Access Locked Card prevents login with locked message")
    void testAccessLockedCard() {
        user.lockCard();
        when(mockBank.getUserByCardId(cardId)).thenReturn(user);
        SecurityException exception = assertThrows(SecurityException.class, () -> bankomat.insertCard(cardId, new int[]{5, 6, 7, 8}));
        assertAll(
                () -> assertEquals("User is locked!", exception.getMessage()),
                () -> verify(mockBank).getUserByCardId(cardId)
        );
    }
    @Test
    @DisplayName("PIN integrity checks for valid length and no negative numbers")
    void testPINIntegrity() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> bankomat.insertCard(cardId, new int[]{5, -6, 7, 8})),
                () -> assertThrows(IllegalArgumentException.class, () -> bankomat.insertCard(cardId, new int[]{5, 6, 7, 8, 9}))
        );
    }
    @Test
    @DisplayName("User not found throws NullPointerException")
    void testUserNotFound() {
        when(mockBank.getUserByCardId(cardId)).thenReturn(null);
        NullPointerException exception = assertThrows(NullPointerException.class, () -> bankomat.insertCard(cardId, new int[]{5, 6, 7, 8}));
        assertAll(
                () -> assertEquals("User not found!", exception.getMessage()),
                () -> verify(mockBank).getUserByCardId(cardId)
        );
    }
    @Test
    @DisplayName("Deposit is processed correctly")
    void testDeposit() {
        double amount = 100.0;
        bankomat.deposit(cardId, amount);
        assertAll(
                () -> verify(mockBank).deposit(cardId, amount),
                () -> assertThrows(IllegalArgumentException.class, () -> bankomat.deposit(cardId, -50))
        );
    }
    @Test
    @DisplayName("Check balance is retrieved correctly")
    void testCheckBalance() {
        double amount = 100.0;
        when(mockBank.getBalance(cardId)).thenReturn(amount);
        bankomat.balance(cardId);
        assertAll(
                () -> verify(mockBank).getBalance(cardId),
                () -> assertEquals(amount, mockBank.getBalance(cardId))
        );
    }
    @Test
    @DisplayName("Withdraw with sufficient and insufficient balance")
    void testWithdraw() {
        double sufficientAmount = 100.0;
        double insufficientAmount = 200.0;
        when(mockBank.getBalance(cardId)).thenReturn(150.0);
        bankomat.withdraw(cardId, sufficientAmount);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> bankomat.withdraw(cardId, insufficientAmount));
        assertAll(
                () -> verify(mockBank).withdraw(cardId, sufficientAmount),
                () -> assertEquals("Withdraw amount must be greater than current balance!", exception.getMessage()),
                () -> verify(mockBank, never()).withdraw(cardId, insufficientAmount)
        );
    }
    @Test
    @DisplayName("Bank ID retrieval")
    void testBankID() {
        when(mockBank.getBankName()).thenReturn("Test Bank Name");
        assertAll(
                () -> assertEquals("Test Bank Name", bankomat.displayBankName()),
                () -> verify(mockBank).getBankName()
        );
    }
}

