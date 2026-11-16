import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Núcleo do banco: guarda as coleções globais e centraliza abertura de contas,
 * emissão de cartões, transferências, crédito, balanço e Open Finance.
 */
public class BancoSistemaCentral {
    private static final String AGENCIA_PADRAO = "0001";
    private static final double TARIFA_TED = 8.50;
    private static final double LIMITE_ALTO_VALOR = 10_000.00;

    private final String nome;
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<ContaBancaria> contas = new ArrayList<>();
    private final List<Cartao> cartoes = new ArrayList<>();
    private final List<EmprestimoFinanciamento> emprestimos = new ArrayList<>();
    private final AuditoriaSeguranca auditoria = new AuditoriaSeguranca();
    private int proximoNumeroConta = 10001;

    public BancoSistemaCentral(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public AuditoriaSeguranca getAuditoria() {
        return auditoria;
    }

    public List<Cliente> getClientes() {
        return Collections.unmodifiableList(clientes);
    }

    public List<ContaBancaria> getContas() {
        return Collections.unmodifiableList(contas);
    }

    public List<Cartao> getCartoes() {
        return Collections.unmodifiableList(cartoes);
    }

    // ---------- Clientes ----------

    public void cadastrarCliente(Cliente cliente) {
        for (Cliente existente : clientes) {
            if (existente.getDocumento().equals(cliente.getDocumento())) {
                auditoria.registrarConformidade("Cadastro recusado: documento já existente ("
                        + cliente.getDocumentoMascarado() + ")");
                throw new IllegalArgumentException("Já existe um cliente cadastrado com este documento.");
            }
        }
        clientes.add(cliente);
        auditoria.registrarConformidade("Cliente cadastrado: " + cliente);
    }

    private void exigirClienteCadastrado(Cliente cliente) {
        if (!clientes.contains(cliente)) {
            throw new IllegalArgumentException("O cliente " + cliente.getNomeCompleto() + " não está cadastrado.");
        }
    }

    // ---------- Abertura de contas ----------

    public ContaCorrente abrirContaCorrente(Cliente cliente, String senha,
                                            double limiteChequeEspecial, double taxaManutencao) {
        exigirClienteCadastrado(cliente);
        validarSenha(senha);
        ContaCorrente conta = new ContaCorrente(AGENCIA_PADRAO, gerarNumeroConta(), senha, cliente,
                limiteChequeEspecial, taxaManutencao);
        registrarConta(conta);
        return conta;
    }

    public ContaPoupanca abrirContaPoupanca(Cliente cliente, String senha,
                                            double taxaRendimentoDiaria, int diaAniversario) {
        exigirClienteCadastrado(cliente);
        validarSenha(senha);
        if (!(cliente instanceof ClientePessoaFisica)) {
            throw new IllegalArgumentException("Conta poupança é exclusiva para pessoas físicas.");
        }
        ContaPoupanca conta = new ContaPoupanca(AGENCIA_PADRAO, gerarNumeroConta(), senha, cliente,
                taxaRendimentoDiaria, diaAniversario);
        registrarConta(conta);
        return conta;
    }

    public ContaInvestimento abrirContaInvestimento(Cliente cliente, String senha,
                                                    String perfil, double taxaCorretagem) {
        exigirClienteCadastrado(cliente);
        validarSenha(senha);
        if (cliente.isKycBasico()) {
            throw new IllegalArgumentException("Conta de investimento exige KYC intermediário ou completo.");
        }
        ContaInvestimento conta = new ContaInvestimento(AGENCIA_PADRAO, gerarNumeroConta(), senha, cliente,
                perfil, taxaCorretagem);
        registrarConta(conta);
        return conta;
    }

    private void validarSenha(String senha) {
        if (senha == null || !senha.matches("\\d{4,8}")) {
            throw new IllegalArgumentException("A senha deve conter de 4 a 8 dígitos numéricos.");
        }
    }

    private String gerarNumeroConta() {
        return String.valueOf(proximoNumeroConta++);
    }

    private void registrarConta(ContaBancaria conta) {
        contas.add(conta);
        auditoria.registrarConformidade("Conta aberta: " + conta);
    }

    // ---------- Cartões ----------

    public CartaoDebito emitirCartaoDebito(ContaBancaria conta) {
        if (conta instanceof ContaInvestimento) {
            throw new IllegalArgumentException("Conta de investimento não permite cartão de débito.");
        }
        CartaoDebito cartao = new CartaoDebito(conta.getTitular().getNomeCompleto().toUpperCase(), conta);
        cartoes.add(cartao);
        return cartao;
    }

    public CartaoCredito emitirCartaoCredito(ContaBancaria conta, double limite, int melhorDiaCompra) {
        Cliente titular = conta.getTitular();
        if (titular.isKycBasico()) {
            throw new IllegalArgumentException("Cartão de crédito exige KYC intermediário ou completo.");
        }
        double limiteMaximo = titular.getRendaMensalReferencia() * 3;
        if (limite > limiteMaximo) {
            throw new IllegalArgumentException(String.format(
                    "Limite solicitado (%s) acima do permitido (%s = 3x a renda).",
                    Transacao.formatarMoeda(limite), Transacao.formatarMoeda(limiteMaximo)));
        }
        CartaoCredito cartao = new CartaoCredito(titular.getNomeCompleto().toUpperCase(), limite, melhorDiaCompra);
        cartoes.add(cartao);
        return cartao;
    }

    // ---------- Operações ----------

    public void depositar(ContaBancaria conta, double valor) {
        conta.depositar(valor);
    }

    public void sacar(ContaBancaria conta, double valor, String senha) {
        autenticarOuFalhar(conta, senha);
        conta.sacar(valor);
    }

    /** Transferência via PIX (gratuita) ou TED (tarifa fixa), sempre com senha da conta de origem. */
    public void transferir(ContaBancaria origem, ContaBancaria destino, double valor,
                           String modalidade, String senha) {
        String tipo = modalidade == null ? "" : modalidade.toUpperCase();
        if (!tipo.equals("PIX") && !tipo.equals("TED")) {
            throw new IllegalArgumentException("Modalidade inválida: use PIX ou TED.");
        }
        autenticarOuFalhar(origem, senha);
        double tarifa = tipo.equals("TED") ? TARIFA_TED : 0.0;
        if (valor + tarifa > origem.getSaldoDisponivel()) {
            throw new IllegalStateException(String.format(
                    "Saldo insuficiente para %s de %s (tarifa: %s). Disponível: %s",
                    tipo, Transacao.formatarMoeda(valor), Transacao.formatarMoeda(tarifa),
                    Transacao.formatarMoeda(origem.getSaldoDisponivel())));
        }
        origem.transferir(destino, valor, tipo);
        if (tarifa > 0) {
            origem.pagar(tarifa, "Tarifa de TED");
        }
        if (valor >= LIMITE_ALTO_VALOR) {
            auditoria.registrarConformidade(String.format(
                    "Transferência de alto valor (%s) de %s/%s para %s/%s comunicada ao Banco Central",
                    Transacao.formatarMoeda(valor), origem.getAgencia(), origem.getNumero(),
                    destino.getAgencia(), destino.getNumero()));
        }
    }

    private void autenticarOuFalhar(ContaBancaria conta, String senha) {
        if (!conta.autenticar(senha, auditoria)) {
            throw new IllegalStateException(conta.isBloqueada()
                    ? "Conta bloqueada. Procure o suporte para desbloqueio."
                    : "Senha incorreta.");
        }
    }

    // ---------- Crédito ----------

    /** Analisa o pedido; se aprovado, credita o valor na conta de destino. */
    public EmprestimoFinanciamento solicitarEmprestimo(Cliente cliente, ContaBancaria contaDestino,
                                                       double valor, double taxaJurosMensal, int parcelas) {
        exigirClienteCadastrado(cliente);
        if (contaDestino.getTitular() != cliente) {
            throw new IllegalArgumentException("A conta de destino precisa pertencer ao solicitante.");
        }
        EmprestimoFinanciamento emprestimo = new EmprestimoFinanciamento(cliente, valor, taxaJurosMensal, parcelas);
        emprestimo.analisarCredito();
        emprestimos.add(emprestimo);
        if (emprestimo.isAprovado()) {
            contaDestino.creditar(valor, "Crédito de empréstimo em " + parcelas + "x");
        }
        auditoria.registrarConformidade("Análise de crédito: " + emprestimo);
        return emprestimo;
    }

    // ---------- Rotinas e relatórios ----------

    public void processarFimDeMes() {
        for (ContaBancaria conta : contas) {
            conta.processarFimDeMes();
        }
        auditoria.registrarConformidade("Rotina de fim de mês executada em " + contas.size() + " contas");
    }

    public List<ContaBancaria> getContasDoCliente(Cliente cliente) {
        List<ContaBancaria> resultado = new ArrayList<>();
        for (ContaBancaria conta : contas) {
            if (conta.getTitular() == cliente) {
                resultado.add(conta);
            }
        }
        return resultado;
    }

    public void exibirExtratoConsolidado(Cliente cliente) {
        System.out.println("  Cliente: " + cliente);
        double total = 0;
        for (ContaBancaria conta : getContasDoCliente(cliente)) {
            conta.exibirExtrato();
            total += conta.getSaldo();
            System.out.println();
        }
        for (Cartao cartao : cartoes) {
            // Cartão de crédito não guarda a conta; compara pelo nome impresso do titular.
            if (cartao instanceof CartaoCredito
                    && cartao.getNomeImpresso().equalsIgnoreCase(cliente.getNomeCompleto())) {
                System.out.println("  " + cartao);
            }
        }
        System.out.println("  >>> Saldo consolidado de " + cliente.getNomeCompleto() + ": "
                + Transacao.formatarMoeda(total));
    }

    public void consolidarBalanco() {
        Map<String, Double> porTipo = new TreeMap<>();
        double totalGeral = 0;
        for (ContaBancaria conta : contas) {
            String tipo = conta.getTipoConta().replaceAll(" \\(.*\\)", "");
            porTipo.merge(tipo, conta.getSaldo(), Double::sum);
            totalGeral += conta.getSaldo();
        }
        double faturasEmAberto = 0;
        for (Cartao cartao : cartoes) {
            if (cartao instanceof CartaoCredito) {
                faturasEmAberto += ((CartaoCredito) cartao).getFaturaAtual();
            }
        }
        System.out.println("  Banco: " + nome);
        System.out.println("  Clientes: " + clientes.size() + " | Contas: " + contas.size()
                + " | Cartões: " + cartoes.size() + " | Empréstimos analisados: " + emprestimos.size());
        for (Map.Entry<String, Double> entrada : porTipo.entrySet()) {
            System.out.printf("  %-18s %s%n", entrada.getKey(), Transacao.formatarMoeda(entrada.getValue()));
        }
        System.out.println("  Total em contas:   " + Transacao.formatarMoeda(totalGeral));
        System.out.println("  Faturas em aberto: " + Transacao.formatarMoeda(faturasEmAberto));
    }

    /** Open Finance: só compartilha dados se o cliente tiver dado consentimento. */
    public void exportarDadosOpenFinance(Cliente cliente) {
        if (!cliente.isConsentimentoOpenFinance()) {
            auditoria.registrarConformidade("Compartilhamento Open Finance negado (sem consentimento): "
                    + cliente.getNomeCompleto());
            throw new IllegalStateException("O cliente não autorizou o compartilhamento de dados (Open Finance).");
        }
        System.out.println("  {");
        System.out.println("    \"cliente\": \"" + cliente.getNomeCompleto() + "\",");
        System.out.println("    \"documento\": \"" + cliente.getDocumentoMascarado() + "\",");
        System.out.println("    \"kyc\": \"" + cliente.getNivelKyc() + "\",");
        System.out.println("    \"contas\": [");
        List<ContaBancaria> contasCliente = getContasDoCliente(cliente);
        for (int i = 0; i < contasCliente.size(); i++) {
            ContaBancaria conta = contasCliente.get(i);
            System.out.printf("      { \"tipo\": \"%s\", \"agencia\": \"%s\", \"conta\": \"%s\", \"saldo\": %.2f }%s%n",
                    conta.getTipoConta(), conta.getAgencia(), conta.getNumero(), conta.getSaldo(),
                    i < contasCliente.size() - 1 ? "," : "");
        }
        System.out.println("    ]");
        System.out.println("  }");
        auditoria.registrarConformidade("Dados compartilhados via Open Finance: " + cliente.getNomeCompleto());
    }
}
