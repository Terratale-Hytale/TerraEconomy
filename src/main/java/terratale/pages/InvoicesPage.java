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

import terratale.Helpers.InvoiceHelper;
import terratale.Helpers.InvoiceStatus;
import terratale.models.BankAccount;
import terratale.models.Invoice;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class InvoicesPage extends InteractiveCustomUIPage<InvoicesPage.UIEventPayload> {

    private final List<Invoice> invoices;
    private String searchFilter = "";
    private final UUID playerUUID;

    public InvoicesPage(@Nonnull PlayerRef playerRef,
                        @Nonnull CustomPageLifetime lifetime,
                        @Nonnull List<Invoice> invoices,
                        @Nonnull UUID playerUUID
                    ) {
        super(playerRef, lifetime, UIEventPayload.CODEC);
        this.invoices = invoices;
        this.playerUUID = playerUUID;
    }

    public static class UIEventPayload {
        public String action;
        public String query; // <- texto del input
        public String id; // <- ID de la factura
        
        // En tu SDK: add() ENTRE campos (porque append devuelve FieldBuilder)
        public static final BuilderCodec<UIEventPayload> CODEC =
                ((BuilderCodec.Builder<UIEventPayload>) (BuilderCodec.Builder<UIEventPayload>)
                        BuilderCodec.builder(UIEventPayload.class, UIEventPayload::new)
                                .append(new KeyedCodec<>("ACTION", Codec.STRING),
                                        (UIEventPayload o, String v) -> o.action = v,
                                        (UIEventPayload o) -> o.action
                                )
                                .add()
                                // IMPORTANTE: key con @ (como recomiendan en ejemplos reales)
                                .append(new KeyedCodec<>("@QUERY", Codec.STRING),
                                        (UIEventPayload o, String v) -> o.query = v,
                                        (UIEventPayload o) -> o.query
                                )
                                .add()
                                .append(new KeyedCodec<>("ID", Codec.STRING),
                                        (UIEventPayload o, String v) -> o.id = v,
                                        (UIEventPayload o) -> o.id
                                )
                                .add()
                ).build();
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiCommandBuilder,
                      @Nonnull UIEventBuilder uiEventBuilder,
                      @Nonnull Store<EntityStore> store) {

        uiCommandBuilder.append("Pages/Invoices.ui");

        // Mantener el texto visible tras cada rebuild (si tu TextField usa otra prop, ajusta aquí)
        uiCommandBuilder.set("#SearchInput.Value", searchFilter == null ? "" : searchFilter);

        // INPUT -> tiempo real
        // Esto es exactamente el patrón recomendado: mapear @QUERY a #SearchInput.Value
        // (NO se manda literal, se evalúa y viaja el valor real del input)
        EventData inputData = EventData.of("ACTION", "input_search")
                .append("@QUERY", "#SearchInput.Value");

        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.ValueChanged,
                "#SearchInput",
                inputData,
                false // no bloquear UI mientras escribes
        );

        // Botón limpiar (opcional)
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#ClearButton",
                EventData.of("ACTION", "clear"),
                false
        );

        // (Opcional) Botón buscar: realmente ya no hace falta, pero lo dejamos por si quieres “Enter/click”
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#SearchButton",
                EventData.of("ACTION", "search"),
                false
        );

        // -----------------------------
        // Filtrado por ID (searchFilter)
        // -----------------------------
        List<Invoice> filtered = invoices;

        if (searchFilter != null && !searchFilter.trim().isEmpty()) {
            String q = searchFilter.trim();
            try {
                filtered = invoices.stream()
                        .filter(inv -> inv.getPayerAccountNumber() != null && inv.getPayerAccountNumber().equals(q) ||
                                       inv.getReceptorAccountNumber() != null && inv.getReceptorAccountNumber().equals(q))
                        .collect(Collectors.toList());
            } catch (NumberFormatException ignored) {
                // Si escriben letras, no filtramos por ID (puedes cambiar esto si quieres)
                filtered = invoices;
            }
        }

        List<Invoice> sorted = filtered.stream()
                .sorted(Comparator.comparing((Invoice i) -> !("pending".equalsIgnoreCase(i.getStatus())))
                        .thenComparing(Invoice::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(20)
                .collect(Collectors.toList());

        List<BankAccount> accounts = BankAccount.getAllByOwner(playerUUID);

        List<String> accountNumbers = accounts.stream()
                .map(BankAccount::getAccountNumber)
                .collect(Collectors.toList());

        // Render lista
        for (int i = 0; i < sorted.size(); i++) {
            Invoice invoice = sorted.get(i);

            uiCommandBuilder.append("#ContentList", "Pages/Invoice.ui");

            if (InvoiceStatus.PENDING.equals(invoice.getStatus())) {
                uiCommandBuilder.append("#ContentList[" + i + "] #SecondRow", "Pages/PayButton.ui");
                // Aquí podrías añadir bindings para el botón pagar, si quieres
                uiEventBuilder.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        "#ContentList[" + i + "] #PayInvoiceButton",
                        EventData.of("ACTION", "pay_invoice")
                                .append("ID", String.valueOf(invoice.getId())),
                        false
                );

                uiEventBuilder.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        "#ContentList[" + i + "] #CancelInvoiceButton",
                        EventData.of("ACTION", "cancel_invoice")
                                .append("ID", String.valueOf(invoice.getId())),
                        false
                );
            }

            uiCommandBuilder.set("#ContentList[" + i + "] #InvoiceID.Text", "#" + invoice.getId());

            String status = (invoice.getStatus() == null ? "UNKNOWN" : invoice.getStatus().toUpperCase());
            if (invoice.isOverdue()) status += " [VENCIDA]";
            uiCommandBuilder.set("#ContentList[" + i + "] #Status.Text", status);

            uiCommandBuilder.set("#ContentList[" + i + "] #Amount.Text", String.format("$%.2f", invoice.getAmount()));

            uiCommandBuilder.set("#ContentList[" + i + "] #From.Text", "De: " + invoice.getReceptorAccountNumber() + 
                    (accountNumbers.contains(invoice.getReceptorAccountNumber()) ? " (Tu)" : ""));
            uiCommandBuilder.set("#ContentList[" + i + "] #To.Text", "A: " + invoice.getPayerAccountNumber() + 
                    (accountNumbers.contains(invoice.getPayerAccountNumber()) ? " (Tu)" : ""));

            String dueDate = (invoice.getDueDate() == null) ? "-" : invoice.getDueDate().toString();
            uiCommandBuilder.set("#ContentList[" + i + "] #DueDate.Text", dueDate);

            String description = invoice.getDescription() == null ? "" : invoice.getDescription();
            if (description.length() > 80) description = description.substring(0, 77) + "...";
            uiCommandBuilder.set("#ContentList[" + i + "] #Description.Text", description);

        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull UIEventPayload data) {
        super.handleDataEvent(ref, store, data);

        if (data == null || data.action == null) return;

        switch (data.action) {
            case "input_search" -> {
                searchFilter = (data.query == null) ? "" : data.query;
            }
            case "clear" -> {
                searchFilter = "";
                this.rebuild();
            }
            case "search" -> {
                // si quieres que el botón busque también
                this.rebuild();
            }
            case "pay_invoice" -> {
                if (data instanceof InvoicesPage.UIEventPayload payload && payload != null) {
                    String idStr = payload.action.equals("pay_invoice") ? payload.id : null;
                    if (idStr != null) {
                        try {
                            InvoiceHelper.payInvoice(Integer.parseInt(idStr), playerUUID);
                            this.playerRef.sendMessage(Message.raw("Factura #" + idStr + " pagada con éxito."));
                            invoices.forEach(inv -> {
                                if (String.valueOf(inv.getId()).equals(idStr)) {
                                    inv.setStatus(InvoiceStatus.PAID);
                                }
                            });
                            this.rebuild();
                        } catch (RuntimeException e) {
                            this.playerRef.sendMessage(Message.raw("Error al pagar la factura #" + idStr + ": " + e.getMessage()));
                        }
                    }
                }
            }
            case "cancel_invoice" -> {
                if (data instanceof InvoicesPage.UIEventPayload payload && payload != null) {
                    String idStr = payload.action.equals("cancel_invoice") ? payload.id : null;
                    if (idStr != null) {
                        try {
                            Boolean result = InvoiceHelper.rejectInvoice(Integer.parseInt(idStr), playerUUID);
                            if (result) {
                                this.playerRef.sendMessage(Message.raw("Factura #" + idStr + " cancelada con éxito."));
                            } else {
                                this.playerRef.sendMessage(Message.raw("No se pudo cancelar la factura #" + idStr));
                            }
                            invoices.forEach(inv -> {
                                if (String.valueOf(inv.getId()).equals(idStr)) {
                                    inv.setStatus(InvoiceStatus.CANCELLED);
                                }
                            });
                            this.rebuild();
                        } catch (RuntimeException e) {
                            this.playerRef.sendMessage(Message.raw("Error al cancelar la factura #" + idStr + ": " + e.getMessage()));
                        }
                    }
                }
            }
        }
    }
}
