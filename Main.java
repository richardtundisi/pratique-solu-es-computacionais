import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

// --- CLASSES DE MODELO ---

class Usuario {
    private String nome;
    private String email;
    private String cidade;

    public Usuario(String nome, String email, String cidade) {
        this.nome = nome;
        this.email = email;
        this.cidade = cidade;
    }

    public String getNome() { return nome; }
    public String getCidade() { return cidade; }

    @Override
    public String toString() {
        return "Usuário: " + nome + " | Email: " + email + " | Cidade: " + cidade;
    }
}

class Evento implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nome;
    private String endereco;
    private String categoria;
    private LocalDateTime horario;
    private String descricao;

    public Evento(String nome, String endereco, String categoria, LocalDateTime horario, String descricao) {
        this.nome = nome;
        this.endereco = endereco;
        this.categoria = categoria;
        this.horario = horario;
        this.descricao = descricao;
    }

    public String getNome() { return nome; }
    public LocalDateTime getHorario() { return horario; }

    @Override
    public String toString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return String.format("[%s] %s\nLocal: %s | Data: %s\nDescrição: %s\n-----------------", 
                categoria.toUpperCase(), nome, endereco, horario.format(dtf), descricao);
    }
}

// --- CLASSE PRINCIPAL ---

public class Main {
    private static final String FILE_NAME = "events.data";
    private static Scanner sc = new Scanner(System.in);
    private static List<Evento> eventos = new ArrayList<>();
    private static List<Evento> participacoes = new ArrayList<>();
    private static Usuario usuarioLogado;

    public static void main(String[] args) {
        carregarEventos();
        configurarUsuario();

        int opcao = -1;
        while (opcao != 0) {
            exibirMenu();
            try {
                opcao = Integer.parseInt(sc.nextLine());
                switch (opcao) {
                    case 1 -> cadastrarEvento();
                    case 2 -> listarEventos();
                    case 3 -> participarEvento();
                    case 4 -> visualizarMinhasParticipacoes();
                    case 5 -> cancelarParticipacao();
                    case 0 -> System.out.println("Salvando dados e saindo...");
                    default -> System.out.println("Opção inválida.");
                }
            } catch (Exception e) {
                System.out.println("Erro: Digite apenas números.");
                opcao = -1;
            }
        }
        salvarEventos();
    }

    private static void configurarUsuario() {
        System.out.println("--- CADASTRO INICIAL DO USUÁRIO ---");
        System.out.print("Nome: "); String nome = sc.nextLine();
        System.out.print("Email: "); String email = sc.nextLine();
        System.out.print("Cidade: "); String cidade = sc.nextLine();
        usuarioLogado = new Usuario(nome, email, cidade);
        System.out.println("\nBem-vindo(a), " + nome + "!");
    }

    private static void exibirMenu() {
        System.out.println("\n--- MENU DE EVENTOS EM " + usuarioLogado.getCidade().toUpperCase() + " ---");
        System.out.println("1. Cadastrar Novo Evento");
        System.out.println("2. Consultar Todos os Eventos");
        System.out.println("3. Confirmar Minha Presença");
        System.out.println("4. Ver Meus Eventos Confirmados");
        System.out.println("5. Cancelar Participação");
        System.out.println("0. Sair");
        System.out.print("Escolha: ");
    }

    private static void cadastrarEvento() {
        try {
            System.out.print("Nome do Evento: "); String nome = sc.nextLine();
            System.out.print("Endereço: "); String endereco = sc.nextLine();
            System.out.print("Categoria (Festa/Show/Esporte): "); String cat = sc.nextLine();
            System.out.print("Data e Hora (dd/MM/yyyy HH:mm): ");
            String dataStr = sc.nextLine();
            LocalDateTime data = LocalDateTime.parse(dataStr, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            System.out.print("Descrição: "); String desc = sc.nextLine();

            eventos.add(new Evento(nome, endereco, cat, data, desc));
            System.out.println("Sucesso: Evento cadastrado!");
        } catch (Exception e) {
            System.out.println("Erro no formato da data! Use: dd/MM/yyyy HH:mm");
        }
    }

    private static void listarEventos() {
        if (eventos.isEmpty()) {
            System.out.println("Não há eventos cadastrados.");
            return;
        }

        LocalDateTime agora = LocalDateTime.now();
        eventos.sort(Comparator.comparing(Evento::getHorario));

        System.out.println("\n--- LISTA DE EVENTOS ---");
        for (int i = 0; i < eventos.size(); i++) {
            Evento e = eventos.get(i);
            String status = e.getHorario().isBefore(agora) ? "[ENCERRADO]" : "[FUTURO]";
            if (e.getHorario().toLocalDate().isEqual(agora.toLocalDate()) && 
                Math.abs(e.getHorario().getHour() - agora.getHour()) < 2) {
                status = "[OCORRENDO AGORA]";
            }
            System.out.println(i + ". " + status + " " + e);
        }
    }

    private static void participarEvento() {
        listarEventos();
        if (eventos.isEmpty()) return;
        
        System.out.print("Número do evento para confirmar presença: ");
        int index = Integer.parseInt(sc.nextLine());
        if (index >= 0 && index < eventos.size()) {
            participacoes.add(eventos.get(index));
            System.out.println("Confirmado! Você está participando de: " + eventos.get(index).getNome());
        }
    }

    private static void visualizarMinhasParticipacoes() {
        if (participacoes.isEmpty()) {
            System.out.println("Você ainda não confirmou presença em nenhum evento.");
            return;
        }
        System.out.println("\n--- MINHA AGENDA DE EVENTOS ---");
        participacoes.forEach(System.out::println);
    }

    private static void cancelarParticipacao() {
        if (participacoes.isEmpty()) return;
        
        for (int i = 0; i < participacoes.size(); i++) {
            System.out.println(i + ". " + participacoes.get(i).getNome());
        }
        System.out.print("Número da participação para remover: ");
        int index = Integer.parseInt(sc.nextLine());
        if (index >= 0 && index < participacoes.size()) {
            participacoes.remove(index);
            System.out.println("Participação removida.");
        }
    }

    private static void salvarEventos() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(eventos);
        } catch (IOException e) {
            System.out.println("Aviso: Dados não puderam ser salvos localmente.");
        }
    }

    private static void carregarEventos() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
                eventos = (List<Evento>) ois.readObject();
            } catch (Exception e) {
                eventos = new ArrayList<>();
            }
        }
    }
}
