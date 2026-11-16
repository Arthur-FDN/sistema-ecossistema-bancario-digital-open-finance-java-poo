import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe abstrata base para todas as contas. Saldo e senha são privados:
 * só podem ser alterados por meio das operações validadas abaixo.
 * Regras violadas lançam IllegalArgumentException (dado inválido)
 * ou IllegalStateException (saldo insuficiente, conta bloqueada).
 */
public abstract class ContaBancaria {
    private static final int MAX_TENTATIVAS_SENHA = 3;

    private final String agencia;
    private final String numero;
    private final Cliente titular;
    private final String senha;
    private double saldo;
    private boolean bloqueada;
    private int tentativasFalhas;
    private final List<Transacao> extrato = new ArrayList<>();

    protected ContaBancaria(String agencia, String numero, String senha, Cliente titular) {
        this.agencia = agencia;
        this.numero = numero;
        this.senha = senha;
        this.titular = titular;
        this.saldo = 0.0;
    }

    // ---------- Consulta ----------

    public String getAgencia() {
        return agencia;
    }

    public String getNumero() {
        return numero;
    }

    public Cliente getTitular() {
        return titular;
    }

    public double getSaldo() {
        return saldo;
    }

    /** Valor máximo que pode ser retirado agora. Subclasses podem ampliar (ex.: cheque especial). */
    public double getSaldoDisponivel() {
        return saldo;
    }

    public boolean isBloqueada() {
        return bloqueada;
    }

    public List<Transacao> getExtrato() {
        return Collections.unmodifiableList(extrato);
    }

    public abstract String getTipoConta();

    /** Rotina de fim de mês: tarifas, rendimentos etc. */
    public abstract void processarFimDeMes();

    // ---------- Segurança ----------

    public boolean autenticar(String senhaInformada, AuditoriaSeguranca auditoria) {
        if (bloqueada) {
            auditoria.registrarTentativaSuspeita(this, "Tentativa de acesso em conta bloqueada");
            return false;
        }
        if (senha.equals(senhaInformada)) {
            tentativasFalhas = 0;
            return true;
        }
        tentativasFalhas++;
        auditoria.registrarTentativaSuspeita(this,
                "Senha incorreta (tentativa " + tentativasFalhas + " de " + MAX_TENTATIVAS_SENHA + ")");
        if (tentativasFalhas >= MAX_TENTATIVAS_SENHA) {
            bloqueada = true;
            auditoria.registrarBloqueio(this, "Conta bloqueada por excesso de tentativas de senha");
        }
        return false;
    }

    public void desbloquear() {
        bloqueada = false;
        tentativasFalhas = 0;
    }

    // ---------- Operações ----------

    public void depositar(double valor) {
        validarValor(valor);
        creditar(valor, "Depósito");
    }

    public void sacar(double valor) {
        validarValor(valor);
        debitar(valor, "Saque");
    }

    public void transferir(ContaBancaria destino, double valor) {
        transferir(destino, valor, "Transferência");
    }

    public void transferir(ContaBancaria destino, double valor, String modalidade) {
        validarValor(valor);
        if (destino == null) {
            throw new IllegalArgumentException("Conta de destino não informada.");
        }
        if (destino == this) {
            throw new IllegalArgumentException("Origem e destino não podem ser a mesma conta.");
        }
        debitar(valor, modalidade + " enviada para " + destino.getAgencia() + "/" + destino.getNumero());
        destino.creditar(valor, modalidade + " recebida de " + agencia + "/" + numero);
    }

    /** Pagamentos em geral: compras no débito, tarifas, faturas de cartão. */
    public void pagar(double valor, String descricao) {
        validarValor(valor);
        debitar(valor, descricao);
    }

    // ---------- Núcleo protegido ----------

    protected void validarValor(double valor) {
        if (Double.isNaN(valor) || valor <= 0) {
            throw new IllegalArgumentException("O valor da operação deve ser maior que zero.");
        }
    }

    protected void creditar(double valor, String descricao) {
        saldo += valor;
        extrato.add(new Transacao(Transacao.CREDITO, valor, descricao));
    }

    protected void debitar(double valor, String descricao) {
        if (bloqueada) {
            throw new IllegalStateException("Conta " + agencia + "/" + numero + " está bloqueada.");
        }
        if (valor > getSaldoDisponivel()) {
            throw new IllegalStateException(String.format(
                    "Saldo insuficiente na conta %s/%s. Disponível: %s | Necessário: %s",
                    agencia, numero,
                    Transacao.formatarMoeda(getSaldoDisponivel()),
                    Transacao.formatarMoeda(valor)));
        }
        saldo -= valor;
        extrato.add(new Transacao(Transacao.DEBITO, valor, descricao));
    }

    // ---------- Exibição ----------

    public void exibirExtrato() {
        System.out.println("  " + this);
        if (extrato.isEmpty()) {
            System.out.println("    (sem movimentações)");
        }
        for (Transacao transacao : extrato) {
            System.out.println("    " + transacao);
        }
        System.out.println("    Saldo atual: " + Transacao.formatarMoeda(saldo));
    }

    @Override
    public String toString() {
        return String.format("%s | Ag %s / Conta %s | Titular: %s%s",
                getTipoConta(), agencia, numero, titular.getNomeCompleto(),
                bloqueada ? " | *** BLOQUEADA ***" : "");
    }
}
