package terratale.Helpers;

import terratale.models.BankAccount;
import terratale.models.User;

public abstract class PorcentualHelper {
    public static double calculatePorcentual(double porcentualPoints) {
        Double total = 0.0;

        total = total + User.getAllMoney();
        total = total + BankAccount.getAllAccountsMoney();

        return (porcentualPoints / 100) * total;
    } 
}
