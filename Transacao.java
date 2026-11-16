import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Registro imutável de uma movimentação financeira.
 * Também concentra os utilitários de formatação e arredondamento de moeda.
 */
public class Transacao {
    public static final String CREDITO = "CREDITO";
    public static final String DEBITO = "DEBITO";

    private static int contador = 1;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final NumberFormat FORMATO_MOEDA =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    private final String codigo;
    private final LocalDateTime dataHora;
    private final String tipo;
    private final double valor;
    private final String descricao;

    public Transacao(String tipo, double valor, String descricao) {
        this.codigo = String.format("TRX-%05d", contador++);
        this.dataHora = LocalDateTime.now();
        this.tipo = tipo;
        this.valor = valor;
        this.descricao = descricao;
    }

    public static String formatarMoeda(double valor) {
        return FORMATO_MOEDA.format(valor).replace('\u00A0', ' ');
    }

    public static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    public String getCodigo() {
        return codigo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getTipo() {
        return tipo;
    }

    public double getValor() {
        return valor;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        double valorComSinal = DEBITO.equals(tipo) ? -valor : valor;
        return String.format("%s | %s | %-7s | %16s | %s",
                codigo, dataHora.format(FORMATO_DATA), tipo, formatarMoeda(valorComSinal), descricao);
    }
}
