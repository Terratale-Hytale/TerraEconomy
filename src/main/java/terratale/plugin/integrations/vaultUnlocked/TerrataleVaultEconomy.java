package terratale.plugin.integrations.vaultUnlocked;

import net.milkbowl.vault2.economy.AccountPermission;
import net.milkbowl.vault2.economy.Economy;
import net.milkbowl.vault2.economy.EconomyResponse;
import terratale.models.User;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TerrataleVaultEconomy implements Economy {

    @Override
    public String getName() {
        return "TerrataleVaultEconomy";
    }

    @Override
    public boolean hasMultiCurrencySupport() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    } 

    @Override
    public BigDecimal getBalance(String name, UUID uuid) {
        User user = User.find(uuid);
        if (user == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(user.getMoney());
    }

    @Override
    public boolean hasSharedAccountSupport() {
        return false;
    }

    @Override
    public int fractionalDigits(String pluginName) {
        return 2;
    }

    @Override
    public String format(BigDecimal amount) {
        return String.format("%.2f", amount);
    }

    @Override
    public String format(String pluginName, BigDecimal amount) {
        return String.format("%.2f", amount);
    }

    @Override
    public String format( BigDecimal amount, String currency) {
        return String.format("%.2f", amount) + " " + currency;
    }

    @Override
    public String format(String pluginName, BigDecimal amount, String currency) {
        return String.format("%.2f", amount) + " " + currency;
    }

    @Override
    public boolean hasCurrency( String currency) {
        return true;
    }

    @Override
    public String getDefaultCurrency(String pluginName) {
        return "Lira";
    }

    @Override
    public String defaultCurrencyNamePlural(String pluginName) {
        return "Liras";
    }

    @Override
    public String defaultCurrencyNameSingular(String pluginName) {
        return "Lira";
    }

    @Override
    public Collection<String> currencies() {
        return java.util.List.of("Lira");
    }

    @Override
    public boolean createAccount(UUID accountID, String name) {
         throw new UnsupportedOperationException("Unimplemented method 'createAccount'");
    }

    @Override
    public boolean createAccount(UUID accountID, String name, boolean player) {
        throw new UnsupportedOperationException("Unimplemented method 'createAccount'");
    }

    @Override
    public boolean createAccount(UUID accountID, String name, String worldName) {
         throw new UnsupportedOperationException("Unimplemented method 'createAccount'");
    }

    @Override
    public boolean createAccount(UUID accountID, String name, String worldName,
            boolean player) {
        throw new UnsupportedOperationException("Unimplemented method 'createAccount'");
    }

    @Override
    public  Map<UUID, String> getUUIDNameMap() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUUIDNameMap'");
    }

    @Override
    public Optional<String> getAccountName( UUID accountID ) {
         throw new UnsupportedOperationException("Unimplemented method 'createAccount'");
    }

    @Override
    public boolean hasAccount( UUID accountID) {
         throw new UnsupportedOperationException("Unimplemented method 'createAccount'");
    }

    @Override
    public boolean hasAccount( UUID accountID,  String worldName) {
         throw new UnsupportedOperationException("Unimplemented method 'createAccount'");
    }

    @Override
    public boolean renameAccount( UUID accountID,  String name) {
        throw new UnsupportedOperationException("Unimplemented method 'createAccount'");
    }

    @Override
    public boolean renameAccount( String plugin,  UUID accountID,  String name) {
         throw new UnsupportedOperationException("Unimplemented method 'createAccount'");
    }

    @Override
    public boolean deleteAccount( String plugin,  UUID accountID) {
         throw new UnsupportedOperationException("Unimplemented method 'createAccount'");
    }

    @Override
    public boolean accountSupportsCurrency( String plugin,  UUID accountID,  String currency) {
        return true;
    }

    @Override
    public boolean accountSupportsCurrency( String plugin,  UUID accountID,  String currency,
             String world) {
        return true;
    }

    @Override
    public  BigDecimal getBalance( String pluginName,  UUID accountID,  String world) {
        User user = User.find(accountID);
        if (user == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(user.getMoney());
    }

    @Override
    public  BigDecimal getBalance( String pluginName,  UUID accountID,  String world,
             String currency) {
        User user = User.find(accountID);
        if (user == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(user.getMoney());
    }

    @Override
    public boolean has( String pluginName,  UUID accountID,  BigDecimal amount) {
        User user = User.find(accountID);
        if (user == null) {
            return false;
        }
        return user.getMoney() >= amount.doubleValue();
    }

    @Override
    public boolean has( String pluginName,  UUID accountID,  String worldName,
             BigDecimal amount) {
        User user = User.find(accountID);
        if (user == null) {
            return false;
        }
        return user.getMoney() >= amount.doubleValue();
    }

    @Override
    public boolean has( String pluginName,  UUID accountID,  String worldName,
             String currency,  BigDecimal amount) {
        User user = User.find(accountID);
        if (user == null) {
            return false;
        }
        return user.getMoney() >= amount.doubleValue();
    }

    @Override
    public  EconomyResponse withdraw( String pluginName,  UUID accountID,
             BigDecimal amount) {
        User user = User.find(accountID);
        if (user == null) {
            return new EconomyResponse(new BigDecimal(0), new BigDecimal(0), EconomyResponse.ResponseType.FAILURE, "Account not found");
        }
        if (user.getMoney() < amount.doubleValue()) {
            return new EconomyResponse(new BigDecimal(0), new BigDecimal(user.getMoney()), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }
        user.setMoney(user.getMoney() - amount.doubleValue());
        return new EconomyResponse(amount, new BigDecimal(user.getMoney()), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public  EconomyResponse withdraw( String pluginName,  UUID accountID,
             String worldName,  BigDecimal amount) {
        User user = User.find(accountID);
        if (user == null) {
            return new EconomyResponse(new BigDecimal(0), new BigDecimal(0), EconomyResponse.ResponseType.FAILURE, "Account not found");
        }
        if (user.getMoney() < amount.doubleValue()) {
            return new EconomyResponse(new BigDecimal(0), new BigDecimal(user.getMoney()), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }
        user.setMoney(user.getMoney() - amount.doubleValue());
        return new EconomyResponse(amount, new BigDecimal(user.getMoney()), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public  EconomyResponse withdraw( String pluginName,  UUID accountID,
             String worldName,  String currency,  BigDecimal amount) {
        User user = User.find(accountID);
        if (user == null) {
            return new EconomyResponse(new BigDecimal(0), new BigDecimal(0), EconomyResponse.ResponseType.FAILURE, "Account not found");
        }
        if (user.getMoney() < amount.doubleValue()) {
            return new EconomyResponse(new BigDecimal(0), new BigDecimal(user.getMoney()), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }
        user.setMoney(user.getMoney() - amount.doubleValue());
        return new EconomyResponse(amount, new BigDecimal(user.getMoney()), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public  EconomyResponse deposit( String pluginName,  UUID accountID,
             BigDecimal amount) {
        User user = User.find(accountID);
        if (user == null) {
            return new EconomyResponse(new BigDecimal(0), new BigDecimal(0), EconomyResponse.ResponseType.FAILURE, "Account not found");
        }

        user.setMoney(user.getMoney() + amount.doubleValue());
        return new EconomyResponse(amount, new BigDecimal(user.getMoney()), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public  EconomyResponse deposit( String pluginName,  UUID accountID,
             String worldName,  BigDecimal amount) {
        User user = User.find(accountID);
        if (user == null) {
            return new EconomyResponse(new BigDecimal(0), new BigDecimal(0), EconomyResponse.ResponseType.FAILURE, "Account not found");
        }

        user.setMoney(user.getMoney() + amount.doubleValue());
        return new EconomyResponse(amount, new BigDecimal(user.getMoney()), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public  EconomyResponse deposit( String pluginName,  UUID accountID,
             String worldName,  String currency,  BigDecimal amount) {
            User user = User.find(accountID);
            if (user == null) {
                return new EconomyResponse(new BigDecimal(0), new BigDecimal(0), EconomyResponse.ResponseType.FAILURE, "Account not found");
            }

            user.setMoney(user.getMoney() + amount.doubleValue());
            return new EconomyResponse(amount, new BigDecimal(user.getMoney()), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public boolean createSharedAccount( String pluginName,  UUID accountID,  String name,
             UUID owner) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createSharedAccount'");
    }

    @Override
    public boolean isAccountOwner( String pluginName,  UUID accountID,  UUID uuid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isAccountOwner'");
    }

    @Override
    public boolean setOwner( String pluginName,  UUID accountID,  UUID uuid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setOwner'");
    }

    @Override
    public boolean isAccountMember( String pluginName,  UUID accountID,  UUID uuid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isAccountMember'");
    }

    @Override
    public boolean addAccountMember( String pluginName,  UUID accountID,  UUID uuid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addAccountMember'");
    }

    @Override
    public boolean addAccountMember( String pluginName,  UUID accountID,  UUID uuid,
             AccountPermission... initialPermissions) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addAccountMember'");
    }

    @Override
    public boolean removeAccountMember( String pluginName,  UUID accountID,  UUID uuid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeAccountMember'");
    }

    @Override
    public boolean hasAccountPermission( String pluginName,  UUID accountID,  UUID uuid,
             AccountPermission permission) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasAccountPermission'");
    }

    @Override
    public boolean updateAccountPermission( String pluginName,  UUID accountID,  UUID uuid,
             AccountPermission permission, boolean value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateAccountPermission'");
    }
}
