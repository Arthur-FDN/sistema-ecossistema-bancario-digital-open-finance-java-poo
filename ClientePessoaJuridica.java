public class ClientePessoaJuridica extends Cliente {
    private double faturamentoAnual;
    private final String nomeFantasia;
    private String ramoAtividade;

    public ClientePessoaJuridica(String razaoSocial, String cnpj, String endereco, String nivelKyc,
                                 double faturamentoAnual, String nomeFantasia, String ramoAtividade) {
        super(razaoSocial, cnpj, endereco, nivelKyc);
        if (faturamentoAnual < 0) {
            throw new IllegalArgumentException("O faturamento anual não pode ser negativo.");
        }
        this.faturamentoAnual = faturamentoAnual;
        this.nomeFantasia = nomeFantasia;
        this.ramoAtividade = ramoAtividade;
    }

    public double getFaturamentoAnual() {
        return faturamentoAnual;
    }

    public void setFaturamentoAnual(double faturamentoAnual) {
        if (faturamentoAnual < 0) {
            throw new IllegalArgumentException("O faturamento anual não pode ser negativo.");
        }
        this.faturamentoAnual = faturamentoAnual;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public String getRamoAtividade() {
        return ramoAtividade;
    }

    public void setRamoAtividade(String ramoAtividade) {
        this.ramoAtividade = ramoAtividade;
    }

    /** Para PJ, a renda mensal de referência é o faturamento anual dividido por 12. */
    @Override
    public double getRendaMensalReferencia() {
        return faturamentoAnual / 12.0;
    }

    @Override
    public String getTipoCliente() {
        return "Pessoa Jurídica";
    }
}
