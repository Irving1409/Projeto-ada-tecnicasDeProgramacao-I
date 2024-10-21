import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CampeonatoBrasileiro {
    private static final Path DATA_DIR = Paths.get("src/data/campeonato-brasileiro-full.csv");
    private static final Path DATA_DIR_GOLS = Paths.get("src/data/campeonato-brasileiro-gols.csv");
    private static final Path DATA_DIR_CARDS = Paths.get("src/data/campeonato-brasileiro-cartoes.csv");

    public static void main(String[] args) {
        try{
            List<String> lines = Files.readAllLines(DATA_DIR);
            List<String> linesGols = Files.readAllLines(DATA_DIR_GOLS);
            List<String> lineCards = Files.readAllLines(DATA_DIR_CARDS);

            List<String> teamsWithMostWins = teamWithMostWinsIn2008(lines);
            System.out.println("Times que mais venceu jogos no ano 2008: " + teamsWithMostWins);

            String stateWithLeastGames = stateWithLeastGames(lines);
            System.out.println("Estado que teve menos jogos entre 2003 e 2022: " + stateWithLeastGames);


            String topScorer = topScorer(linesGols);
            String topPenaltyScorer = topPenaltyScorer(linesGols);
            System.out.println("Jogador que mais fez gols: " + topScorer);
            System.out.println("Jogador que mais fez gols de pênalti: " + topPenaltyScorer);

            String playerWithMostOwnGoals = playerWithMostOwnGoals(lines);
            System.out.println("Jogador que mais fez gols contra: " + playerWithMostOwnGoals);

            Optional<Map.Entry<String, Long>> playerYellowCarded = playerWithMostYellowCards(lineCards);
            if (playerYellowCarded.isPresent()) {
                System.out.println("Jogador com mais cartões amarelos: " + playerYellowCarded.get().getKey() +
                        " com " + playerYellowCarded.get().getValue() + " cartões.");
            } else {
                System.out.println("Nenhum jogador com cartões amarelos foi encontrado.");
            }

            Optional<Map.Entry<String, Long>> playerRedCarded = playerWithMostRedCards(lineCards);
            if (playerRedCarded.isPresent()) {
                System.out.println("Jogador com mais cartões vermelhos: " + playerRedCarded.get().getKey() +
                        " com " + playerRedCarded.get().getValue() + " cartões.");
            } else {
                System.out.println("Nenhum jogador com cartões vermelho foi encontrado.");
            }

            String matchWithMostGoals = matchWithMostGoals(lines);
            System.out.println("Placar da partida com mais gols: " + matchWithMostGoals);

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static List<String> teamWithMostWinsIn2008(List<String> lines){
        Map<String, Long> winCounts = lines.stream()
                .skip(1)
                .filter(line -> line.split(",")[2].contains("2008"))
                .map(line -> line.split(",")[10])
                .filter(winner -> !winner.contains("-"))
                .filter(winner -> winner != null && !winner.trim().isEmpty())
                //.peek(winner -> System.out.println("Vencedor encontrado: " + winner))
                .collect(Collectors.groupingBy(winner -> winner, Collectors.counting()));

                long maxWins = winCounts.values().stream()
                        .max(Long::compare)
                        .orElse(0L);

                return winCounts.entrySet().stream()
                        .filter(entry -> entry.getValue() == maxWins)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
    }

    private static String stateWithLeastGames(List<String> lines) {
        return lines.stream()
                .skip(1)
                .filter(line -> {
                    String[] columns = line.split(",");
                    String data = columns[2].trim();

                    if (data.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                        String year = data.split("/")[2];
                        int yearInt = Integer.parseInt(year);
                        return yearInt >= 2003 && yearInt <= 2022;
                    }
                    return false;
                })
                .flatMap(line -> {
                    String[] columns = line.split(",");
                    String mandanteEstado = columns[13].trim();
                    String visitanteEstado = columns[14].trim();
                    return Arrays.stream(new String[]{mandanteEstado, visitanteEstado});
                })
                .collect(Collectors.groupingBy(state -> state, Collectors.counting()))
                .entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Nenhum estado encontrado");
    }

    private static String topScorer(List<String> lines){
        return lines.stream()
                .skip(1)
                .filter(line -> !line.contains("Gol Contra"))
                .map(line -> line.split(","))
                .collect(Collectors.groupingBy(
                        parts -> parts[3],
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Nenhum Jogador Encontrado");
    }

    private static String topPenaltyScorer(List<String> lines) {
        return lines.stream()
                .skip(1) // Ignora o cabeçalho
                .filter(line -> line.contains("Penalty"))
                .map(line -> line.split(","))
                .collect(Collectors.groupingBy(
                        parts -> parts[3],
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Nenhum jogador encontrado");
    }

    private static String playerWithMostOwnGoals(List<String> lines) {
        return lines.stream()
                .skip(1)
                .filter(line -> line.contains("Gol Contra"))
                .map(line -> line.split(","))
                .collect(Collectors.groupingBy(
                        parts -> parts[3],
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Nenhum jogador encontrado");
    }

    public static Optional<Map.Entry<String, Long>> playerWithMostYellowCards(List<String> linesCards) {
        return linesCards.stream()
                .skip(1)
                .filter(line -> line.split(",")[3].trim().equalsIgnoreCase("Amarelo"))
                .collect(Collectors.groupingBy(
                        line -> line.split(",")[4].trim(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue());
    }

    public static Optional<Map.Entry<String, Long>> playerWithMostRedCards(List<String> linesCards) {
        return linesCards.stream()
                .skip(1)
                .filter(line -> line.split(",")[3].trim().equalsIgnoreCase("Vermelho"))
                .collect(Collectors.groupingBy(
                        line -> line.split(",")[4].trim(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue());
    }
    public static String matchWithMostGoals(List<String> lines) {
        Optional<int[]> result = Optional.of(lines.stream()
                .skip(1)
                .map(line -> line.split(","))
                .map(columns -> {
                    try {
                        int mandantePlacar = Integer.parseInt(columns[12].trim().replaceAll("\"",""));
                        int visitantePlacar = Integer.parseInt(columns[13].trim().replaceAll("\"",""));
                        return new int[]{mandantePlacar, visitantePlacar};
                    } catch (NumberFormatException e) {
                        System.err.println("Erro de conversão do placar:" + e.getMessage());
                        return new int[]{0, 0};
                    }
                })
                .reduce(new int[]{0, 0}, (currentMax, goals) ->
                        (goals[0] + goals[1] > currentMax[0] + currentMax[1]) ? goals : currentMax
                ));
        return result.map(goals -> goals[0] + " x " + goals[1]).orElse("Nenhuma partida encontrada");
    }
}















