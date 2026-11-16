/**
 * Solicitação de crédito com análise automática baseada em KYC e renda do cliente.
 */
public class EmprestimoFinanciamento {
    public static final String PENDENTE = "PENDENTE";
    public static final String APROVADO = "APROVADO";
    public static final String REPROVADO = "REPROVADO";

    /** A parcela não pode comprometer mais que 30% da renda mensal. */
    private static final double COMPROMETIMENTO_MAXIMO = 0.30;

    private final Cliente cliente;
    private final double valorSolicitado;
    private final double taxaJurosMensal;
    private final int quantidadeParcelas;
    private String status;
    private String motivoAnalise;

    public EmprestimoFinanciamento(Cliente cliente, double valorSolicitado,
                                   double taxaJurosMensal, int quantidadeParcelas) {
        if (valorSolicitado <= 0) {
            throw new IllegalArgumentException("O valor solicitado deve ser maior que zero.");
        }
        if (taxaJurosMensal <= 0) {
            throw new IllegalArgumentException("A taxa de juros mensal deve ser maior que zero.");
        }
        if (quantidadeParcelas <= 0) {
            throw new IllegalArgumentException("A quantidade de parcelas deve ser maior que zero.");
        }
        this.cliente = cliente;
        this.valorSolicitado = valorSolicitado;
        this.taxaJurosMensal = taxaJurosMensal;
        this.quantidadeParcelas = quantidadeParcelas;
        this.status = PENDENTE;
        this.motivoAnalise = "Aguardando análise de crédito";
    }

    public Cliente getCliente() {
        return cliente;
    }

    public double getValorSolicitado() {
        return valorSolicitado;
    }

    public double getTaxaJurosMensal() {
        return taxaJurosMensal;
    }

    public int getQuantidadeParcelas() {
        return quantidadeParcelas;
    }

    public String getStatus() {
        return status;
    }

    public String getMotivoAnalise() {
        return motivoAnalise;
    }

    public boolean isAprovado() {
        return APROVADO.equals(status);
    }

    /** Parcela fixa pela Tabela Price. */
    public double calcularValorParcela() {
        double i = taxaJurosMensal;
        double parcela = valorSolicitado * i / (1 - Math.pow(1 + i, -quantidadeParcelas));
        return Transacao.arredondar(parcela);
    }

    public double calcularTotalAPagar() {
        return Transacao.arredondar(calcularValorParcela() * quantidadeParcelas);
    }

    public String analisarCredito() {
        if (cliente.isKycBasico()) {
            return decidir(REPROVADO, "KYC básico: é necessário completar o cadastro");
        }
        double parcela = calcularValorParcela();
        double limiteParcela = Transacao.arredondar(cliente.getRendaMensalReferencia() * COMPROMETIMENTO_MAXIMO);
        if (parcela > limiteParcela) {
            return decidir(REPROVADO, String.format(
                    "parcela de %s excede 30%% da renda (máximo de %s)",
                    Transacao.formatarMoeda(parcela), Transacao.formatarMoeda(limiteParcela)));
        }
        return decidir(APROVADO, String.format(
                "parcela de %s cabe na renda (máximo de %s)",
                Transacao.formatarMoeda(parcela), Transacao.formatarMoeda(limiteParcela)));
    }

    private String decidir(String novoStatus, String motivo) {
        this.status = novoStatus;
        this.motivoAnalise = motivo;
        return novoStatus;
    }

    @Override
    public String toString() {
        return String.format("Empréstimo de %s em %dx de %s (%.2f%% a.m.) para %s | %s - %s",
                Transacao.formatarMoeda(valorSolicitado), quantidadeParcelas,
                Transacao.formatarMoeda(calcularValorParcela()), taxaJurosMensal * 100,
                cliente.getNomeCompleto(), status, motivoAnalise);
    }
}
