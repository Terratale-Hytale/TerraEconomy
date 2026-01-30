package terratale.responses;

import terratale.models.Invoice;

public class InvoicePaymentResponse {
    
    private Invoice invoice;
    private double totalDeducted;
    private double bankTransferFee;
    private double governmentFee;
    private double newBalance;

    public InvoicePaymentResponse(Invoice invoice, double totalDeducted, double bankTransferFee, double governmentFee, double newBalance) {
        this.invoice = invoice;
        this.totalDeducted = totalDeducted;
        this.bankTransferFee = bankTransferFee;
        this.governmentFee = governmentFee;
        this.newBalance = newBalance;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public double getTotalDeducted() {
        return totalDeducted;
    }

    public double getBankTransferFee() {
        return bankTransferFee;
    }

    public double getGovernmentFee() {
        return governmentFee;
    }

    public double getNewBalance() {
        return newBalance;
    }
}
