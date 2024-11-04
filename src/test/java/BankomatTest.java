import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BankomatTest {
    private Bank mockBank;
    private Bankomat bankomat;

    @BeforeEach
    void setUp() {
        mockBank = Mockito.mock(Bank.class);
        bankomat = new Bankomat(mockBank);
    }

    @Test
    @DisplayName("Correct PIN")
    void testCorrectPin(){
        User user = new User(1234, new int[]{5, 6, 7, 8});
        when(mockBank.getUserByCardId(1234)).thenReturn(user);
        boolean result = bankomat.insertCard(1234,new int[]{5, 6, 7, 8});
        assertTrue(result);
    }
    @Test
    @DisplayName("Wrong PIN")
    void testWrongPin(){
        User user = new User(1234, new int[]{5, 6, 7, 8});
        when(mockBank.getUserByCardId(1234)).thenReturn(user);
        when(mockBank.getFailedAttempts(1234)).thenReturn(1);
        //Kollar samtidigt ifall rätt exceptions slängs.
        Exception exception = assertThrows(Exception.class,() -> bankomat.insertCard(1234,new int[]{0, 0, 0, 0}) );
        assertEquals("Incorrect PIN. Attempts left: 1",exception.getMessage());
        verify(mockBank).incrementFailedAttempts(1234);
    }
    @Test
    @DisplayName("Lock Card")
    void testLockCard(){
        User user = new User(1234, new int[]{5, 6, 7, 8});
        when(mockBank.getUserByCardId(1234)).thenReturn(user);
        when(mockBank.getFailedAttempts(1234)).thenReturn(2);
        Exception exception = assertThrows(Exception.class,() -> bankomat.insertCard(1234,new int[]{0, 0, 0, 0}));
        assertEquals("Card is locked due to too many failed attempts!",exception.getMessage());
        verify(mockBank).lockCard(1234);
    }
    @Test
    @DisplayName("Access Locked Card")
    void testAccessLockedCard(){
        User user = new User(1234, new int[]{5, 6, 7, 8});
        user.lockCard();
        when(mockBank.getUserByCardId(1234)).thenReturn(user);
        Exception exception = assertThrows(Exception.class,() -> bankomat.insertCard(1234,new int[]{5, 6, 7, 8}));
        assertEquals("User is locked!", exception.getMessage());
    }
    @Test
    @DisplayName("PIN integrity")
    void testPINIntegrity(){
        assertThrows(NullPointerException.class, () -> bankomat.insertCard(1234,new int[]{5, -6, 7, 8}));
        assertThrows(IllegalArgumentException.class, () -> bankomat.insertCard(1234,new int[]{5, 6, 7, 8, 9}));
    }
    @Test
    @DisplayName("User not found")
    void testUserNotFound(){
        when(mockBank.getUserByCardId(1234)).thenReturn(null);
        NullPointerException exception = assertThrows(NullPointerException.class, () -> bankomat.insertCard(1234,new int[]{5, 6, 7, 8}));
        assertEquals("User not found!", exception.getMessage());
    }
    @Test
    @DisplayName("Deposit")
    void testDeposit(){
        int cardId = 1234;
        double amount = 100.0;
        bankomat.deposit(cardId,amount);
        verify(mockBank).deposit(cardId,amount);
    }
    @Test
    @DisplayName("Check Balance")
    void testCheckBalance(){
        int cardId = 1234;
        double amount = 100.0;
        when(mockBank.getBalance(cardId)).thenReturn(amount);
        bankomat.balance(cardId);
        verify(mockBank).getBalance(cardId);
    }
    @Test
    @DisplayName("Withdraw with sufficient balance")
    void testWithdrawWithSufficientBalance(){
        int cardId = 1234;
        double amount = 100.0;
        when(mockBank.getBalance(cardId)).thenReturn(200.0);
        bankomat.withdraw(cardId,amount);
        verify(mockBank).withdraw(cardId,amount);
    }
    @Test
    @DisplayName("Withdraw with insufficient balance")
    void testWithdrawWithInsufficientBalance(){
        int cardId = 1234;
        double amount = 100.0;
        when(mockBank.getBalance(cardId)).thenReturn(50.0);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> bankomat.withdraw(cardId,amount));
        assertEquals("Withdraw amount must be greater than current balance!", exception.getMessage());
        //Kollar så att metoden aldrig körs med never()!
        verify(mockBank,never()).withdraw(cardId,amount);
    }
}
