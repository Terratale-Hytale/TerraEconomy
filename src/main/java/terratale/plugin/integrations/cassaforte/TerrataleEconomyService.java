package terratale.plugin.integrations.cassaforte;

import terratale.models.User;
import it.cassaforte.api.economy.EconomyResponse;

import java.util.UUID;
import com.hypixel.hytale.logger.HytaleLogger;

public class TerrataleEconomyService {

    private final HytaleLogger logger;

    public TerrataleEconomyService(HytaleLogger logger) {
        this.logger = logger;
    }

    public double getBalance(UUID playerId) {
        logger.atSevere().log("getting balance for " + playerId );
        try {
            User user = User.find(playerId);
            if (user == null) {
                return 0.0;
            }
            return user.getMoney();
        } catch (Exception e) {
            logger.atSevere().log("Error getting balance for " + playerId + ": " + e.getMessage());
            return 0.0;
        }
    }

    public EconomyResponse deposit(UUID playerId, double amount) {
        try {
            if (amount < 0) {
                return new EconomyResponse(
                    0.0,
                    0.0,
                    EconomyResponse.ResponseType.FAILURE,
                    "La cantidad debe ser positiva"
                );
            }

            User user = User.find(playerId);
            if (user == null) {
                return new EconomyResponse(
                    0.0,
                    0.0,
                    EconomyResponse.ResponseType.FAILURE,
                    "Usuario no encontrado"
                );
            }

            double oldBalance = user.getMoney();
            double newBalance = oldBalance + amount;
            user.setMoney(newBalance);
            user.saveMoney();

            return new EconomyResponse(
                amount,
                newBalance,
                EconomyResponse.ResponseType.SUCCESS,
                "Depósito realizado exitosamente"
            );
        } catch (Exception e) {
            logger.atSevere().log("Error depositing for " + playerId + ": " + e.getMessage());
            return new EconomyResponse(
                0.0,
                0.0,
                EconomyResponse.ResponseType.FAILURE,
                "Error interno: " + e.getMessage()
            );
        }
    }

    public EconomyResponse withdraw(UUID playerId, double amount) {
        try {
            if (amount < 0) {
                return new EconomyResponse(
                    0.0,
                    0.0,
                    EconomyResponse.ResponseType.FAILURE,
                    "La cantidad debe ser positiva"
                );
            }

            User user = User.find(playerId);
            if (user == null) {
                return new EconomyResponse(
                    0.0,
                    0.0,
                    EconomyResponse.ResponseType.FAILURE,
                    "Usuario no encontrado"
                );
            }

            double oldBalance = user.getMoney();
            if (oldBalance < amount) {
                return new EconomyResponse(
                    0.0,
                    oldBalance,
                    EconomyResponse.ResponseType.FAILURE,
                    "Fondos insuficientes"
                );
            }

            double newBalance = oldBalance - amount;
            user.setMoney(newBalance);
            user.saveMoney();

            return new EconomyResponse(
                amount,
                newBalance,
                EconomyResponse.ResponseType.SUCCESS,
                "Retiro realizado exitosamente"
            );
        } catch (Exception e) {
            logger.atSevere().log("Error withdrawing for " + playerId + ": " + e.getMessage());
            return new EconomyResponse(
                0.0,
                0.0,
                EconomyResponse.ResponseType.FAILURE,
                "Error interno: " + e.getMessage()
            );
        }
    }
}
