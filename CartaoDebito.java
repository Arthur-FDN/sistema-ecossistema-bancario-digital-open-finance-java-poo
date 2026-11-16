public class CartaoDebito extends Cartao {
    private final ContaBancaria conta;

    public CartaoDebito(String nomeImpresso, ContaBancaria conta) {
        super(nomeImpresso);
        this.conta = conta;
    }

    public ContaBancaria getConta() {
        return conta;
    }

    @Override
    public String getTipoCartao() {
        return "Débito";
    }

    /** Liquidação instantânea: o valor sai direto da conta vinculada. */
    @Override
    public void pagar(double valor, String estabelecimento) {
        validarUso(valor);
        conta.pagar(valor, "Compra no débito - " + estabelecimento);
    }
}
