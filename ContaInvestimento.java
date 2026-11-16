public class ContaInvestimento extends ContaBancaria {
    public static final String CONSERVADOR = "Conservador";
    public static final String MODERADO = "Moderado";
    public static final String ARROJADO = "Arrojado";

    private String perfilRisco;
    private final double taxaCorretagem;

    public ContaInvestimento(String agencia, String numero, String senha, Cliente titular,
                             String perfilRisco, double taxaCorretagem) {
        super(agencia, numero, senha, titular);
        if (taxaCorretagem < 0) {
            throw new IllegalArgumentException("A taxa de corretagem não pode ser negativa.");
        }
        validarPerfil(perfilRisco);
        this.perfilRisco = perfilRisco;
        this.taxaCorretagem = taxaCorretagem;
    }

    private static void validarPerfil(String perfil) {
        if (!CONSERVADOR.equals(perfil) && !MODERADO.equals(perfil) && !ARROJADO.equals(perfil)) {
            throw new IllegalArgumentException("Perfil de risco inválido: " + perfil);
        }
    }

    public String getPerfilRisco() {
        return perfilRisco;
    }

    public void setPerfilRisco(String perfilRisco) {
        validarPerfil(perfilRisco);
        this.perfilRisco = perfilRisco;
    }

    public double getTaxaCorretagem() {
        return taxaCorretagem;
    }

    /** Rentabilidade mensal estimada de cada perfil. */
    public double getRentabilidadeMensal() {
        switch (perfilRisco) {
            case CONSERVADOR:
                return 0.006;
            case MODERADO:
                return 0.009;
            default:
                return 0.013;
        }
    }

    @Override
    public String getTipoConta() {
        return "Conta Investimento (" + perfilRisco + ")";
    }

    /** Todo resgate paga corretagem sobre o valor retirado. */
    @Override
    public void sacar(double valor) {
        validarValor(valor);
        double corretagem = Transacao.arredondar(valor * taxaCorretagem);
        debitar(valor + corretagem, String.format("Resgate de %s (corretagem de %s)",
                Transacao.formatarMoeda(valor), Transacao.formatarMoeda(corretagem)));
    }

    @Override
    public void processarFimDeMes() {
        double rendimento = Transacao.arredondar(getSaldo() * getRentabilidadeMensal());
        if (rendimento > 0) {
            creditar(rendimento, "Rentabilidade mensal do perfil " + perfilRisco);
        }
    }
}
