import java.util.Arrays;
import java.util.Scanner;

public class Bankomat {
   private final Bank bank;

   public Bankomat(Bank bank) {
       this.bank = bank;
   }

   public boolean insertCard(int cardId,int[] pin){
       if(pin.length != 4 || Arrays.stream(pin).allMatch(num -> num < 0)) throw new IllegalArgumentException("Pin length do not equal 4 or have negative numbers!"); //
       User user = bank.getUserByCardId(cardId);
       if(user == null) throw new NullPointerException("User not found!"); //
       if(user.isCardLocked()) throw new SecurityException("User is locked!");
       if(Arrays.equals(user.getPin(), pin)){
           return true;
       }
       else {
           int failedAttempts = bank.getFailedAttempts(cardId);
           if(failedAttempts >= 2){
               bank.lockCard(cardId);
               throw new SecurityException("Card is locked due to too many failed attempts!");
           }else {
               bank.incrementFailedAttempts(cardId);
               throw new SecurityException("Incorrect PIN. Attempts left: "+(2 - bank.getFailedAttempts(cardId)));
           }
       }
   }
   public void menu(int cardId, int[] pin){
       if(insertCard(cardId,pin)){
           Scanner scanner = new Scanner(System.in);
           boolean exit = false;
           while(!exit){
           System.out.println("1: Check balance \n 2: Deposit \n 3: Withdraw \n 4: Exit");
           String input = scanner.nextLine();
               switch(input){
                   case "1":
                       balance(cardId);
                       break;

                   case "2":
                       System.out.print("Enter amount to deposit:");
                       double depositAmount = scanner.nextDouble();
                       deposit(cardId,depositAmount);
                       break;

                   case "3":
                       System.out.print("Enter amount to withdraw:");
                       double withdrawAmount = scanner.nextDouble();
                       withdraw(cardId,withdrawAmount);
                       break;

                   case "4":
                       exit = true;
                       break;

                   default:
                       System.out.println("Wrong input!");
               }
           }
           scanner.close();
       }
   }

   void balance(int cardId){
        double currentBalance = bank.getBalance(cardId);
        System.out.println("Current balance: "+currentBalance);
   }
   void deposit(int cardId,double depositAmount){
        if(depositAmount <= 0) throw new IllegalArgumentException("Deposit amount must be positive!");
        bank.deposit(cardId,depositAmount);
        System.out.println("Deposited "+depositAmount+" to account "+cardId);
   }
   void withdraw(int cardId,double withdrawAmount){
        double currentBalance = bank.getBalance(cardId);
        if(withdrawAmount > currentBalance) throw new IllegalArgumentException("Withdraw amount must be greater than current balance!");
        else if(withdrawAmount <= 0) throw new IllegalArgumentException("Amount must be greater than zero!");
        else {
            bank.withdraw(cardId,withdrawAmount);
            System.out.println("Withdrawn "+withdrawAmount+" from account "+cardId);
        }
   }
}
