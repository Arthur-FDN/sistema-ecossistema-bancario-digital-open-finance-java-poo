import java.time.LocalDate;

/**
 * Simula o ecossistema bancário digital em funcionamento.
 * Para executar no VS Code: abra esta pasta e clique em "Run" acima do método main.
 */
public class Programa {

    /** Executa uma operação e mostra se deu certo ou qual regra de negócio a barrou. */
    private static void executar(String descricao, Runnable acao) {
        try {
            acao.run();
            System.out.println("  [OK]   " + descricao);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("  [ERRO] " + descricao + " -> " + e.getMessage());
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("==================================================================");
        System.out.println(" " + texto);
        System.out.println("==================================================================");
    }

    public static void main(String[] args) {
        BancoSistemaCentral banco = new BancoSistemaCentral("Fintech Verde Bank");

        // ------------------------------------------------------------------
        titulo("1. Cadastro de clientes");
        ClientePessoaFisica ana = new ClientePessoaFisica(
                "Ana Souza", "12345678909", "Rua das Flores, 100 - Belo Horizonte/MG",
                Cliente.KYC_COMPLETO, LocalDate.of(1992, 5, 14), 8500.00, "Solteira");
        ClientePessoaFisica carlos = new ClientePessoaFisica(
                "Carlos Lima", "98765432100", "Av. Brasil, 450 - Contagem/MG",
                Cliente.KYC_BASICO, LocalDate.of(1998, 11, 3), 2500.00, "Casado");
        ClientePessoaJuridica ecoTech = new ClientePessoaJuridica(
                "EcoTech Soluções Sustentáveis Ltda", "12345678000190",
                "Av. Afonso Pena, 1500 - Belo Horizonte/MG",
                Cliente.KYC_COMPLETO, 1_800_000.00, "EcoTech", "Energia solar");

        banco.cadastrarCliente(ana);
        banco.cadastrarCliente(carlos);
        banco.cadastrarCliente(ecoTech);
        for (Cliente cliente : banco.getClientes()) {
            System.out.println("  " + cliente);
        }
        executar("Cadastro duplicado (mesmo CPF da Ana)", () -> banco.cadastrarCliente(
                new ClientePessoaFisica("Ana Duplicada", "12345678909", "Rua X",
                        Cliente.KYC_BASICO, LocalDate.of(2000, 1, 1), 1000, "Solteira")));

        // ------------------------------------------------------------------
        titulo("2. Abertura de contas");
        ContaCorrente ccAna = banco.abrirContaCorrente(ana, "1234", 2000.00, 29.90);
        ContaPoupanca poupAna = banco.abrirContaPoupanca(ana, "1234", 0.0002, 15);
        ContaInvestimento invAna = banco.abrirContaInvestimento(ana, "4321", ContaInvestimento.MODERADO, 0.005);
        ContaCorrente ccEco = banco.abrirContaCorrente(ecoTech, "9999", 20000.00, 79.90);
        ContaCorrente ccCarlos = banco.abrirContaCorrente(carlos, "5555", 500.00, 29.90);
        ContaPoupanca poupCarlos = banco.abrirContaPoupanca(carlos, "5555", 0.0002, 10);
        for (ContaBancaria conta : banco.getContas()) {
            System.out.println("  " + conta);
        }
        executar("Poupança para pessoa jurídica",
                () -> banco.abrirContaPoupanca(ecoTech, "9999", 0.0002, 1));
        executar("Investimento para cliente com KYC básico",
                () -> banco.abrirContaInvestimento(carlos, "5555", ContaInvestimento.ARROJADO, 0.005));
        executar("Conta com senha inválida",
                () -> banco.abrirContaCorrente(ana, "12", 0, 0));

        // ------------------------------------------------------------------
        titulo("3. Operações: depósitos, saques e transferências (PIX/TED)");
        executar("Depósito de R$ 5.000,00 na conta da Ana", () -> banco.depositar(ccAna, 5000.00));
        executar("Depósito de R$ 100.000,00 na conta da EcoTech", () -> banco.depositar(ccEco, 100000.00));
        executar("Depósito de R$ 800,00 na conta do Carlos", () -> banco.depositar(ccCarlos, 800.00));
        executar("Depósito de valor negativo", () -> banco.depositar(ccAna, -50.00));
        executar("TED de R$ 12.000,00 da EcoTech para Ana",
                () -> banco.transferir(ccEco, ccAna, 12000.00, "TED", "9999"));
        executar("PIX de R$ 1.500,00 da Ana para Carlos",
                () -> banco.transferir(ccAna, ccCarlos, 1500.00, "PIX", "1234"));
        executar("PIX com senha errada",
                () -> banco.transferir(ccAna, ccCarlos, 100.00, "PIX", "0000"));
        executar("Transferência com modalidade inválida",
                () -> banco.transferir(ccAna, ccCarlos, 100.00, "DOC", "1234"));
        executar("Saque de R$ 300,00 da Ana", () -> banco.sacar(ccAna, 300.00, "1234"));
        executar("Saque de R$ 2.500,00 do Carlos (usa cheque especial)",
                () -> banco.sacar(ccCarlos, 2500.00, "5555"));
        executar("Saque de R$ 5.000,00 do Carlos (acima do disponível)",
                () -> banco.sacar(ccCarlos, 5000.00, "5555"));
        executar("PIX de R$ 3.000,00 da Ana para sua conta de investimento",
                () -> banco.transferir(ccAna, invAna, 3000.00, "PIX", "1234"));
        executar("PIX de R$ 2.000,00 da Ana para sua poupança",
                () -> banco.transferir(ccAna, poupAna, 2000.00, "PIX", "1234"));
        executar("Resgate de R$ 1.000,00 do investimento (com corretagem)",
                () -> banco.sacar(invAna, 1000.00, "4321"));

        // ------------------------------------------------------------------
        titulo("4. Cartões de débito e crédito");
        CartaoDebito debitoAna = banco.emitirCartaoDebito(ccAna);
        CartaoCredito creditoAna = banco.emitirCartaoCredito(ccAna, 10000.00, 5);
        System.out.println("  " + debitoAna);
        System.out.println("  " + creditoAna);
        executar("Débito para conta de investimento", () -> banco.emitirCartaoDebito(invAna));
        executar("Crédito para Carlos com KYC básico",
                () -> banco.emitirCartaoCredito(ccCarlos, 3000.00, 10));
        carlos.setNivelKyc(Cliente.KYC_INTERMEDIARIO);
        System.out.println("  (KYC do Carlos atualizado para " + carlos.getNivelKyc() + ")");
        executar("Crédito para Carlos acima de 3x a renda",
                () -> banco.emitirCartaoCredito(ccCarlos, 50000.00, 10));
        CartaoCredito creditoCarlos = banco.emitirCartaoCredito(ccCarlos, 3000.00, 10);
        System.out.println("  " + creditoCarlos);

        executar("Compra no débito de R$ 250,00 (Ana, supermercado)",
                () -> debitoAna.pagar(250.00, "Supermercado Verde"));
        executar("Compra no crédito de R$ 1.800,00 (Ana, bicicleta)",
                () -> creditoAna.pagar(1800.00, "Loja de Bicicletas"));
        executar("Compra no crédito de R$ 9.000,00 (acima do limite disponível)",
                () -> creditoAna.pagar(9000.00, "Loja de Eletrônicos"));
        executar("Compra no crédito de R$ 3.500,00 (Carlos, acima do limite)",
                () -> creditoCarlos.pagar(3500.00, "Loja de Móveis"));
        creditoCarlos.bloquear();
        executar("Compra com cartão bloqueado (Carlos)",
                () -> creditoCarlos.pagar(100.00, "Padaria"));
        creditoCarlos.desbloquear();
        System.out.println("  " + creditoAna);
        executar("Pagamento da fatura da Ana pela conta corrente",
                () -> creditoAna.pagarFatura(ccAna));
        System.out.println("  " + creditoAna);

        // ------------------------------------------------------------------
        titulo("5. Análise de empréstimos");
        executar("Empréstimo para Ana: R$ 20.000,00 em 24x a 1,8% a.m.", () -> {
            EmprestimoFinanciamento e = banco.solicitarEmprestimo(ana, ccAna, 20000.00, 0.018, 24);
            System.out.println("         " + e);
        });
        executar("Empréstimo para a EcoTech: R$ 300.000,00 em 36x a 1,5% a.m.", () -> {
            EmprestimoFinanciamento e = banco.solicitarEmprestimo(ecoTech, ccEco, 300000.00, 0.015, 36);
            System.out.println("         " + e);
        });
        executar("Empréstimo para Carlos: R$ 50.000,00 em 12x a 2,5% a.m.", () -> {
            EmprestimoFinanciamento e = banco.solicitarEmprestimo(carlos, ccCarlos, 50000.00, 0.025, 12);
            System.out.println("         " + e);
        });

        // ------------------------------------------------------------------
        titulo("6. Segurança: tentativas de senha e bloqueio de conta");
        executar("Saque na poupança do Carlos com senha errada (1)", () -> banco.sacar(poupCarlos, 10, "0000"));
        executar("Saque na poupança do Carlos com senha errada (2)", () -> banco.sacar(poupCarlos, 10, "1111"));
        executar("Saque na poupança do Carlos com senha errada (3)", () -> banco.sacar(poupCarlos, 10, "2222"));
        executar("Saque com a senha correta após o bloqueio", () -> banco.sacar(poupCarlos, 10, "5555"));

        // ------------------------------------------------------------------
        titulo("7. Open Finance (compartilhamento com consentimento)");
        ana.setConsentimentoOpenFinance(true);
        executar("Exportar dados da Ana (com consentimento)", () -> banco.exportarDadosOpenFinance(ana));
        executar("Exportar dados do Carlos (sem consentimento)", () -> banco.exportarDadosOpenFinance(carlos));

        // ------------------------------------------------------------------
        titulo("8. Rotina de fim de mês (tarifas e rendimentos)");
        banco.processarFimDeMes();
        System.out.println("  Tarifas cobradas e rendimentos creditados.");

        // ------------------------------------------------------------------
        titulo("9. Extrato consolidado - Ana Souza");
        banco.exibirExtratoConsolidado(ana);

        titulo("10. Extrato consolidado - Carlos Lima");
        banco.exibirExtratoConsolidado(carlos);

        titulo("11. Extrato consolidado - EcoTech");
        banco.exibirExtratoConsolidado(ecoTech);

        titulo("12. Balanço geral do banco");
        banco.consolidarBalanco();

        titulo("13. Relatório de auditoria e segurança");
        banco.getAuditoria().gerarRelatorio();
    }
}
