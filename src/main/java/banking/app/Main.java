package banking.app;

import banking.ui.LoginScreen;
import javax.swing.SwingUtilities;


public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginScreen::new);
    }
}
