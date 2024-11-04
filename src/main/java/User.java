import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private final int cardId;
    private final int[] pin;
    private boolean isCardLocked;

    public User(int cardId, int[] pin) {
        this.cardId = cardId;
        this.pin = pin;
        this.isCardLocked = false;
    }
    public void lockCard() {
        this.isCardLocked = true;
    }
}
