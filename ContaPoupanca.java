public class ContaPoupanca extends ContaBancaria {
    private final double taxaRendimentoDiaria;
    private final int diaAniversario;

    public ContaPoupanca(String agencia, String numero, String senha, Cliente titular,
                         double taxaRendimentoDiaria, int diaAniversario) {
        super(agencia, numero, senha, titular);
        if (taxaRendimentoDiaria < 0) {
            throw new IllegalArgumentException("A taxa de rendimento não pode ser negativa.");
        }
        if (diaAniversario < 1 || diaAniversario > 28) {
            throw new IllegalArgumentException("O aniversário deve ser um dia entre 1 e 28.");
        }
        this.taxaRendimentoDiaria = taxaRendimentoDiaria;
        this.diaAniversario = diaAniversario;
    }

    public double getTaxaRendimentoDiaria() {
        return taxaRendimentoDiaria;
    }

    public int getDiaAniversario() {
        return diaAniversario;
    }

    @Override
    public String getTipoConta() {
        return "Conta Poupança";
    }

    /** Rendimento composto de 30 dias sobre o saldo atual. */
    @Override
    public void processarFimDeMes() {
        double rendimento = Transacao.arredondar(
                getSaldo() * (Math.pow(1 + taxaRendimentoDiaria, 30) - 1));
        if (rendimento > 0) {
            creditar(rendimento, "Rendimento mensal da poupança (aniversário dia " + diaAniversario + ")");
        }
    }
}
