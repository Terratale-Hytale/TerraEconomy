package terratale.Helpers;

import java.math.BigDecimal;

import terratale.models.BankAccount;
import terratale.models.User;

public abstract class PorcentualHelper {
    public static Double calculatePorcentual(BigDecimal porcentualPoints) {
        Double total = getAllMoneyBank();

        return Math.floor(porcentualPoints.multiply(BigDecimal.valueOf(total)).doubleValue());
    } 

    public static Double getAllMoneyBank () {
        return User.getAllMoney() + BankAccount.getAllAccountsMoney();
    }
}
