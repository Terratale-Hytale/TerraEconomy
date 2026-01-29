package terratale.pages;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import terratale.models.Invoice;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public class InvoicesPage extends InteractiveCustomUIPage<InvoicesPage.BindingData> {

    private List<Invoice> invoices;

    public InvoicesPage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime, List<Invoice> invoices) {
        super(playerRef, lifetime, BindingData.CODEC);
        this.invoices = invoices;
    }

    public static class BindingData {
        public static final BuilderCodec<BindingData> CODEC = BuilderCodec.builder(BindingData.class, BindingData::new)
                .build();
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder,
            @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/Invoices.ui");

        // Ordenar: pendientes primero, luego por fecha
        List<Invoice> sortedInvoices = invoices.stream()
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
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
            @Nonnull BindingData data) {
        super.handleDataEvent(ref, store, data);
    }
}
