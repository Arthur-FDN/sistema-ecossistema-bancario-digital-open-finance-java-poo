import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CartaoCredito extends Cartao {
    private final double limiteTotal;
    private double limiteDisponivel;
    private double faturaAtual;
    private final int melhorDiaCompra;
    private final List<Transacao> comprasDaFatura = new ArrayList<>();

    public CartaoCredito(String nomeImpresso, double limiteTotal, int melhorDiaCompra) {
        super(nomeImpresso);
        if (limiteTotal <= 0) {
            throw new IllegalArgumentException("O limite do cartão deve ser maior que zero.");
        }
        if (melhorDiaCompra < 1 || melhorDiaCompra > 28) {
            throw new IllegalArgumentException("O melhor dia de compra deve estar entre 1 e 28.");
        }
        this.limiteTotal = limiteTotal;
        this.limiteDisponivel = limiteTotal;
        this.faturaAtual = 0.0;
        this.melhorDiaCompra = melhorDiaCompra;
    }

    public double getLimiteTotal() {
        return limiteTotal;
    }

    public double getLimiteDisponivel() {
        return limiteDisponivel;
    }

    public double getFaturaAtual() {
        return faturaAtual;
    }

    public int getMelhorDiaCompra() {
        return melhorDiaCompra;
    }

    public List<Transacao> getComprasDaFatura() {
        return Collections.unmodifiableList(comprasDaFatura);
    }

    @Override
    public String getTipoCartao() {
        return "Crédito";
    }

    @Override
    public void pagar(double valor, String estabelecimento) {
        validarUso(valor);
        if (valor > limiteDisponivel) {
            throw new IllegalStateException(String.format(
                    "Limite insuficiente no cartão %s. Disponível: %s | Compra: %s",
                    getNumeroMascarado(),
                    Transacao.formatarMoeda(limiteDisponivel),
                    Transacao.formatarMoeda(valor)));
        }
        limiteDisponivel -= valor;
        faturaAtual += valor;
        comprasDaFatura.add(new Transacao(Transacao.DEBITO, valor, "Compra no crédito - " + estabelecimento));
    }

    /** Quita a fatura debitando a conta informada e libera todo o limite novamente. */
    public void pagarFatura(ContaBancaria conta) {
        if (faturaAtual <= 0) {
            throw new IllegalStateException("Não há fatura em aberto para o cartão " + getNumeroMascarado() + ".");
        }
        conta.pagar(faturaAtual, "Pagamento da fatura do cartão " + getNumeroMascarado());
        limiteDisponivel = limiteTotal;
        faturaAtual = 0.0;
        comprasDaFatura.clear();
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Limite: %s | Disponível: %s | Fatura: %s | Melhor dia: %d",
                Transacao.formatarMoeda(limiteTotal),
                Transacao.formatarMoeda(limiteDisponivel),
                Transacao.formatarMoeda(faturaAtual),
                melhorDiaCompra);
    }
}
