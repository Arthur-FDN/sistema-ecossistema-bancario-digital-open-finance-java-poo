/**
 * Classe abstrata base para todos os clientes do banco.
 */
public abstract class Cliente {
    public static final String KYC_BASICO = "Básico";
    public static final String KYC_INTERMEDIARIO = "Intermediário";
    public static final String KYC_COMPLETO = "Completo";

    private static int proximoId = 1;

    private final int id;
    private final String nomeCompleto;
    private final String documento;
    private String endereco;
    private String nivelKyc;
    private boolean consentimentoOpenFinance;

    protected Cliente(String nomeCompleto, String documento, String endereco, String nivelKyc) {
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            throw new IllegalArgumentException("O nome do cliente é obrigatório.");
        }
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("O documento do cliente é obrigatório.");
        }
        validarKyc(nivelKyc);
        this.id = proximoId++;
        this.nomeCompleto = nomeCompleto;
        this.documento = documento;
        this.endereco = endereco;
        this.nivelKyc = nivelKyc;
        this.consentimentoOpenFinance = false;
    }

    private static void validarKyc(String nivel) {
        if (!KYC_BASICO.equals(nivel) && !KYC_INTERMEDIARIO.equals(nivel) && !KYC_COMPLETO.equals(nivel)) {
            throw new IllegalArgumentException("Nível de KYC inválido: " + nivel);
        }
    }

    public int getId() {
        return id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    /** Uso interno do sistema (ex.: checagem de duplicidade). */
    String getDocumento() {
        return documento;
    }

    /** Exibe apenas os dois últimos dígitos para proteger o dado sensível. */
    public String getDocumentoMascarado() {
        int visiveis = Math.min(2, documento.length());
        return "*".repeat(documento.length() - visiveis)
                + documento.substring(documento.length() - visiveis);
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getNivelKyc() {
        return nivelKyc;
    }

    public void setNivelKyc(String nivelKyc) {
        validarKyc(nivelKyc);
        this.nivelKyc = nivelKyc;
    }

    public boolean isKycBasico() {
        return KYC_BASICO.equals(nivelKyc);
    }

    public boolean isConsentimentoOpenFinance() {
        return consentimentoOpenFinance;
    }

    public void setConsentimentoOpenFinance(boolean consentimentoOpenFinance) {
        this.consentimentoOpenFinance = consentimentoOpenFinance;
    }

    /** Renda mensal usada pelas regras de crédito (varia conforme o tipo de cliente). */
    public abstract double getRendaMensalReferencia();

    public abstract String getTipoCliente();

    @Override
    public String toString() {
        return String.format("[%s #%d] %s | Doc: %s | KYC: %s",
                getTipoCliente(), id, nomeCompleto, getDocumentoMascarado(), nivelKyc);
    }
}
