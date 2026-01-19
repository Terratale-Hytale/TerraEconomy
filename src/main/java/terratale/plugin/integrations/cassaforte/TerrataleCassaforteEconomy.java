package terratale.plugin.integrations.cassaforte;

import it.cassaforte.api.economy.AbstractEconomy;
import it.cassaforte.api.economy.EconomyResponse;

import java.util.UUID;

public class TerrataleCassaforteEconomy extends AbstractEconomy {

    private final TerrataleEconomyService service;

    public TerrataleCassaforteEconomy(TerrataleEconomyService service) {
        super();
        this.service = service;
        System.out.println("[TerrataleCassaforteEconomy] Constructor llamado");
    }

    @Override
    public String getName() {
        System.out.println("[TerrataleCassaforteEconomy] getName() llamado");
        return "Terratale";
    }

    @Override
    public String currencyNameSingular() {
        return "moneda";
    }

    @Override
    public String currencyNamePlural() {
        return "monedas";
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public double getBalance(UUID playerId) {
        System.out.println("[TerrataleCassaforteEconomy] getBalance() llamado para " + playerId);
        return service.getBalance(playerId);
    }

    @Override
    public EconomyResponse depositPlayer(UUID playerId, double amount) {
        System.out.println("[TerrataleCassaforteEconomy] depositPlayer() llamado: " + amount + " para " + playerId);
        return service.deposit(playerId, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(UUID playerId, double amount) {
        System.out.println("[TerrataleCassaforteEconomy] withdrawPlayer() llamado: " + amount + " para " + playerId);
        return service.withdraw(playerId, amount);
    }

    @Override
    public boolean has(UUID playerId, double amount) {
        return service.getBalance(playerId) >= amount;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean hasAccount(UUID playerId) {
        // En Terratale, las cuentas se crean automáticamente
        return true;
    }

    @Override
    public String format(double amount) {
        return String.format("$%.2f", amount);
    }

    @Override
    public boolean hasBankSupport() {
        return false; // Terratale tiene su propio sistema de bancos
    }
}
