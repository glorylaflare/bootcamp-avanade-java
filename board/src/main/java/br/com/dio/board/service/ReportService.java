package br.com.dio.board.service;

import br.com.dio.board.dto.CardMovementDTO;
import br.com.dio.board.persistence.dao.CardMovementDAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;

@AllArgsConstructor
public class ReportService {

    private final CardMovementDAO dao;
    private final ObjectMapper objectMapper;

    public void generateCardReport(final Long cardId) throws SQLException, IOException {
        List<CardMovementDTO> movements = dao.getMovementByCardId(cardId);

        if(movements.isEmpty()) {
            System.out.println("Nenhuma movimentação encontrada para o card " + cardId);
            return;
        }

        List<Map<String, String>> movementList = new ArrayList<>();

        for (int i = 0; i < movements.size(); i++) {
            CardMovementDTO movement = movements.get(i);
            var enteredAt = movement.entered_at();
            var leftedAt = movement.lefted_at();

            String timeSpent;

            if (leftedAt != null) {
                Duration duration = Duration.between(enteredAt, leftedAt);
                long hours = duration.toHours();
                long minutes = duration.toMinutes() % 60;
                long seconds = duration.toSeconds() % 60;

                timeSpent = (hours + " horas, " + minutes + " minutos e " + seconds + " segundos.");
            } else {
                timeSpent = "Ainda em andamento";
            }

            Map<String, String> movementData = new LinkedHashMap<>();
            movementData.put("ID da coluna:", movement.board_column_id().toString());
            movementData.put("Nome da coluna:", movement.column_type());
            movementData.put("Data de início:", enteredAt.toString());
            movementData.put("Data de conclusão:", leftedAt != null ? leftedAt.toString() : "Em andamento");
            movementData.put("Tempo gasto:", timeSpent);
            movementList.add(movementData);
        }

        var firstEntry = movements.get(0).entered_at();

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("ID:", cardId);
        reportData.put("Movimentação:", movementList);
        reportData.put("Data de criação:", firstEntry.toString());

        String reportPath = "src/main/resources/reports/card_" + cardId + "_report.json";

        Files.createDirectories(Paths.get("src/main/resources/reports"));
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(reportPath), reportData);
        System.out.println("Relatório gerado com sucesso " + reportPath);
    }
}
