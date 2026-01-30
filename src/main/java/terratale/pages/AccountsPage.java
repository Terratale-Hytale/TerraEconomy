package terratale.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import terratale.models.BankAccount;

import java.util.List;

import javax.annotation.Nonnull;

public class AccountsPage extends InteractiveCustomUIPage<AccountsPage.BindingData> {

    private List<BankAccount> accounts;

    public AccountsPage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime, List<BankAccount> accounts) {
        super(playerRef, lifetime, BindingData.CODEC);
        this.accounts = accounts;
    }


    public static class BindingData {
        public static final BuilderCodec<BindingData> CODEC = BuilderCodec.builder(BindingData.class, BindingData::new)

                .build();
    }
    
    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/Accounts.ui");

        for (int i = 0; i < accounts.size(); i++) {
            uiCommandBuilder.append("#ContentList", "Pages/Account.ui");
            System.out.println("Adding account to UI: " + accounts.get(i).getAccountNumber());
            uiCommandBuilder.set("#ContentList[" + i + "] #AccountLabel.Value", accounts.get(i).getAccountNumber());
            uiCommandBuilder.set("#ContentList[" + i + "] #AccountText.Text", " - Banco: " + accounts.get(i).getBank().getName() + " - Saldo: " + String.format("%.2f", accounts.get(i).getBalance()) + " Liras");
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull BindingData data) {
        super.handleDataEvent(ref, store, data);
        boolean changed = false;

        if (changed) {
             this.playerRef.sendMessage(Message.raw("Changes processed."));
        }
    }

}
