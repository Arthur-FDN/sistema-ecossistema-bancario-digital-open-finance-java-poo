import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registra acessos suspeitos, bloqueios e eventos de conformidade regulatória.
 */
public class AuditoriaSeguranca {
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final List<String> registros = new ArrayList<>();
    private int totalTentativasSuspeitas;
    private int totalBloqueios;
    private int totalEventosConformidade;

    public void registrarTentativaSuspeita(ContaBancaria conta, String motivo) {
        totalTentativasSuspeitas++;
        adicionar("SUSPEITO", "Conta " + identificar(conta) + " - " + motivo);
    }

    public void registrarBloqueio(ContaBancaria conta, String motivo) {
        totalBloqueios++;
        adicionar("BLOQUEIO", "Conta " + identificar(conta) + " - " + motivo);
    }

    public void registrarConformidade(String descricao) {
        totalEventosConformidade++;
        adicionar("CONFORMIDADE", descricao);
    }

    public List<String> getRegistros() {
        return Collections.unmodifiableList(registros);
    }

    public void gerarRelatorio() {
        System.out.println("  Tentativas suspeitas: " + totalTentativasSuspeitas
                + " | Bloqueios: " + totalBloqueios
                + " | Eventos de conformidade: " + totalEventosConformidade);
        System.out.println("  ----------------------------------------------------------------");
        if (registros.isEmpty()) {
            System.out.println("  (nenhum registro)");
        }
        for (String registro : registros) {
            System.out.println("  " + registro);
        }
    }

    private void adicionar(String categoria, String mensagem) {
        registros.add(String.format("[%s] %-12s %s",
                LocalDateTime.now().format(FORMATO_DATA), categoria, mensagem));
    }

    private String identificar(ContaBancaria conta) {
        return conta.getAgencia() + "/" + conta.getNumero();
    }
}
