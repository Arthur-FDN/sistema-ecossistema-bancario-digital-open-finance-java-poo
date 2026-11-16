import java.time.LocalDate;
import java.time.Period;

public class ClientePessoaFisica extends Cliente {
    private final LocalDate dataNascimento;
    private double rendaMensal;
    private String estadoCivil;

    public ClientePessoaFisica(String nomeCompleto, String cpf, String endereco, String nivelKyc,
                               LocalDate dataNascimento, double rendaMensal, String estadoCivil) {
        super(nomeCompleto, cpf, endereco, nivelKyc);
        if (rendaMensal < 0) {
            throw new IllegalArgumentException("A renda mensal não pode ser negativa.");
        }
        this.dataNascimento = dataNascimento;
        this.rendaMensal = rendaMensal;
        this.estadoCivil = estadoCivil;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public int getIdade() {
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    public double getRendaMensal() {
        return rendaMensal;
    }

    public void setRendaMensal(double rendaMensal) {
        if (rendaMensal < 0) {
            throw new IllegalArgumentException("A renda mensal não pode ser negativa.");
        }
        this.rendaMensal = rendaMensal;
    }

    public String getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(String estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    @Override
    public double getRendaMensalReferencia() {
        return rendaMensal;
    }

    @Override
    public String getTipoCliente() {
        return "Pessoa Física";
    }
}
