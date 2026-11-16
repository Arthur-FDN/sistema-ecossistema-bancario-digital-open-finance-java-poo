public class ContaCorrente extends ContaBancaria {
    private final double limiteChequeEspecial;
    private final double taxaManutencaoMensal;

    public ContaCorrente(String agencia, String numero, String senha, Cliente titular,
                         double limiteChequeEspecial, double taxaManutencaoMensal) {
        super(agencia, numero, senha, titular);
        if (limiteChequeEspecial < 0 || taxaManutencaoMensal < 0) {
            throw new IllegalArgumentException("Limite e taxa não podem ser negativos.");
        }
        this.limiteChequeEspecial = limiteChequeEspecial;
        this.taxaManutencaoMensal = taxaManutencaoMensal;
    }

    public double getLimiteChequeEspecial() {
        return limiteChequeEspecial;
    }

    public double getTaxaManutencaoMensal() {
        return taxaManutencaoMensal;
    }

    /** Na conta corrente, o cheque especial soma-se ao saldo disponível. */
    @Override
    public double getSaldoDisponivel() {
        return getSaldo() + limiteChequeEspecial;
    }

    @Override
    public String getTipoConta() {
        return "Conta Corrente";
    }

    @Override
    public void processarFimDeMes() {
        try {
            debitar(taxaManutencaoMensal, "Tarifa de manutenção mensal");
        } catch (IllegalStateException e) {
            System.out.println("  Tarifa não cobrada na conta " + getNumero() + ": " + e.getMessage());
        }
    }
}
