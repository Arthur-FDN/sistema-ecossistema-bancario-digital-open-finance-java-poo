import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Classe abstrata base para cartões. Número completo e CVV nunca são expostos.
 */
public abstract class Cartao {
    public static final String ATIVO = "ATIVO";
    public static final String BLOQUEADO = "BLOQUEADO";

    private static final DateTimeFormatter FORMATO_VALIDADE = DateTimeFormatter.ofPattern("MM/yy");

    private final String numero;
    private final String nomeImpresso;
    private final String cvv;
    private final LocalDate validade;
    private String status;

    protected Cartao(String nomeImpresso) {
        this.numero = gerarNumero();
        this.nomeImpresso = nomeImpresso;
        this.cvv = String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
        this.validade = LocalDate.now().plusYears(5);
        this.status = ATIVO;
    }

    private static String gerarNumero() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(10));
        }
        return sb.toString();
    }

    public String getNumeroMascarado() {
        return "**** **** **** " + numero.substring(12);
    }

    public String getNomeImpresso() {
        return nomeImpresso;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public String getStatus() {
        return status;
    }

    public boolean isAtivo() {
        return ATIVO.equals(status);
    }

    public boolean validarCvv(String cvvInformado) {
        return cvv.equals(cvvInformado);
    }

    public void bloquear() {
        status = BLOQUEADO;
    }

    public void desbloquear() {
        status = ATIVO;
    }

    protected void validarUso(double valor) {
        if (!isAtivo()) {
            throw new IllegalStateException("O cartão " + getNumeroMascarado() + " está bloqueado.");
        }
        if (LocalDate.now().isAfter(validade)) {
            throw new IllegalStateException("O cartão " + getNumeroMascarado() + " está vencido.");
        }
        if (Double.isNaN(valor) || valor <= 0) {
            throw new IllegalArgumentException("O valor da compra deve ser maior que zero.");
        }
    }

    public abstract String getTipoCartao();

    public abstract void pagar(double valor, String estabelecimento);

    @Override
    public String toString() {
        return String.format("%s %s | %s | Val: %s | %s",
                getTipoCartao(), getNumeroMascarado(), nomeImpresso,
                validade.format(FORMATO_VALIDADE), status);
    }
}
