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

import terratale.models.Bank;
import terratale.models.BankAccount;
import terratale.models.BankAccountOwner;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

public class BanksPage extends InteractiveCustomUIPage<BanksPage.UIEventPayload> {

    private List<Bank> banks;

    public BanksPage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime, List<Bank> banks) {
        super(playerRef, lifetime, UIEventPayload.CODEC);
        this.banks = banks;
    }

    public static class UIEventPayload {

        public String id;

        public static final BuilderCodec<UIEventPayload> CODEC = ((BuilderCodec.Builder<UIEventPayload>) (BuilderCodec.Builder<UIEventPayload>)
                BuilderCodec.builder(UIEventPayload.class, UIEventPayload::new)
                        .append(new KeyedCodec<>("ID", Codec.STRING),
                                (UIEventPayload o, String v) -> o.id = v,
                                (UIEventPayload o) -> o.id
                        )
                        .add()).build();
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder,
            @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/Accounts.ui");
        uiCommandBuilder.append("#ContentList", "Pages/BankInfo.ui");

        for (int i = 0; i < banks.size(); i++) {
            uiCommandBuilder.append("#ContentList", "Pages/Bank.ui");
            uiCommandBuilder.set("#ContentList[" + (i + 1) + "] #BankText.Text", banks.get(i).getName());
            uiCommandBuilder.set("#ContentList[" + (i + 1) + "] #Deposit.Text", banks.get(i).getDepositFee() + "%");
            uiCommandBuilder.set("#ContentList[" + (i + 1) + "] #Withdraw.Text", banks.get(i).getWithdrawFee() + "%");
            uiCommandBuilder.set("#ContentList[" + (i + 1) + "] #Transfer.Text", banks.get(i).getTransactionsFee() + "%");

            EventData uiData = new EventData();
            uiData.events().put("ID", banks.get(i).getId().toString());

            uiEventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    "#ContentList[" + (i + 1) + "] #CreateAccount",
                    uiData,
                    false);
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
            @Nonnull UIEventPayload data) {
        super.handleDataEvent(ref, store, data);
        boolean changed = false;

        UUID playerUUID = this.playerRef.getUuid();
        BankAccount newAccount = new BankAccount(Integer.parseInt(data.id));
        newAccount.save();
        BankAccountOwner owner = new BankAccountOwner(newAccount.getId(), playerUUID);
        owner.save();

        this.playerRef.sendMessage(Message.raw("Cuenta bancaria creada con éxito."));
    }

}
