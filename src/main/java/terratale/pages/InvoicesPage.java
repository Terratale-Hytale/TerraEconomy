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

import terratale.models.Invoice;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public class InvoicesPage extends InteractiveCustomUIPage<InvoicesPage.UIEventPayload> {

    private List<Invoice> invoices;
    private String searchFilter = "";

    public InvoicesPage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime, List<Invoice> invoices) {
        super(playerRef, lifetime, UIEventPayload.CODEC);
        this.invoices = invoices;
    }

    public static class UIEventPayload {
        public String action;

        public static final BuilderCodec<UIEventPayload> CODEC = ((BuilderCodec.Builder<UIEventPayload>) (BuilderCodec.Builder<UIEventPayload>)
                BuilderCodec.builder(UIEventPayload.class, UIEventPayload::new)
                        .append(new KeyedCodec<>("ACTION", Codec.STRING),
                                (UIEventPayload o, String v) -> o.action = v,
                                (UIEventPayload o) -> o.action
                        )
                        .add()).build();
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder,
            @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/Invoices.ui");

        // Filtrar por ID si hay búsqueda activa
        List<Invoice> filteredInvoices = invoices;
        if (!searchFilter.isEmpty()) {
            try {
                int searchId = Integer.parseInt(searchFilter);
                filteredInvoices = invoices.stream()
                        .filter(invoice -> invoice.getId().equals(searchId))
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                // Si no es un número válido, mostrar todas
                filteredInvoices = invoices;
            }
        }

        // Ordenar: pendientes primero, luego por fecha
        List<Invoice> sortedInvoices = filteredInvoices.stream()
                .sorted(Comparator.comparing((Invoice i) -> !i.getStatus().equals("pending"))
                        .thenComparing(Invoice::getDueDate))
                .limit(20)  // Máximo 20 facturas
                .collect(Collectors.toList());

        // Agregar facturas a la lista
        for (int i = 0; i < sortedInvoices.size(); i++) {
            Invoice invoice = sortedInvoices.get(i);
            uiCommandBuilder.append("#ContentList", "Pages/Invoice.ui");
            
            // Formatear el ID
            String invoiceId = "#" + invoice.getId();
            uiCommandBuilder.set("#ContentList[" + i + "] #InvoiceID.Text", invoiceId);
            
            // Formatear el estado con color
            String status = invoice.getStatus().toUpperCase();
            if (invoice.isOverdue()) {
                status += " [VENCIDA]";
            }
            uiCommandBuilder.set("#ContentList[" + i + "] #Status.Text", status);
            
            // Formatear el monto
            String amount = String.format("$%.2f", invoice.getAmount());
            uiCommandBuilder.set("#ContentList[" + i + "] #Amount.Text", amount);
            
            // De y Para
            uiCommandBuilder.set("#ContentList[" + i + "] #From.Text", "De: " + invoice.getReceptorAccountNumber());
            uiCommandBuilder.set("#ContentList[" + i + "] #To.Text", "A: " + invoice.getPayerAccountNumber());
            
            // Fecha de vencimiento
            String dueDate = invoice.getDueDate().toString();
            uiCommandBuilder.set("#ContentList[" + i + "] #DueDate.Text", dueDate);
            
            // Descripción
            String description = invoice.getDescription();
            if (description.length() > 80) {
                description = description.substring(0, 77) + "...";
            }
            uiCommandBuilder.set("#ContentList[" + i + "] #Description.Text", description);
        }

        // Agregar eventos para los botones
        EventData searchData = new EventData();
        searchData.events().put("ACTION", "search");
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#SearchButton",
                searchData,
                true);

        EventData clearData = new EventData();
        clearData.events().put("ACTION", "clear");
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#ClearButton",
                clearData,
                false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
            @Nonnull UIEventPayload data) {
        super.handleDataEvent(ref, store, data);
        
        if ("search".equals(data.action)) {
            // El botón de búsqueda fue presionado, pero el valor ya debería estar en searchFilter
            // desde eventos anteriores del input
            if (!searchFilter.isEmpty()) {
                this.playerRef.sendMessage(Message.raw("Buscando factura #" + searchFilter));
            } else {
                this.playerRef.sendMessage(Message.raw("Ingresa un ID de factura"));
            }
            this.rebuild();
        } else if ("clear".equals(data.action)) {
            searchFilter = "";
            this.playerRef.sendMessage(Message.raw("Búsqueda limpiada"));
            this.rebuild();
        } else if (data.action != null && !data.action.isEmpty()) {
            // El usuario escribió algo en el campo de búsqueda
            searchFilter = data.action;
        }
    }
}
