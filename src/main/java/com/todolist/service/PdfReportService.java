package com.todolist.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.todolist.entity.Task;
import com.todolist.entity.User;
import com.todolist.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdfReportService {

    private final TaskRepository taskRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public byte[] gerarRelatorioTarefasPdf() {
        User user = authService.obterUsuarioAutenticado();
        List<Task> tasks = taskRepository.findByUsuarioIdAndDeletadaFalseOrderByDataCriacaoDesc(user.getId());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(document, out);
            document.open();

            // Estilos de Fontes
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(30, 41, 59));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(100, 116, 139));
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(15, 23, 42));
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(30, 41, 59));
            Font boldCellFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(30, 41, 59));
            Font kpiNumberFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(37, 99, 235));
            Font kpiLabelFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(100, 116, 139));

            // Cabeçalho Corporativo
            Paragraph title = new Paragraph("RELATÓRIO EXECUTIVO DE TAREFAS & PRODUTIVIDADE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            Paragraph sub = new Paragraph("Gerado para: " + user.getNome() + " (" + user.getEmail() + ") • Em: " + LocalDateTime.now().format(dtf), subtitleFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(15f);
            document.add(sub);

            // Cálculos de KPIs
            int total = tasks.size();
            long concluidas = tasks.stream().filter(t -> Boolean.TRUE.equals(t.getConcluida())).count();
            long atrasadas = tasks.stream().filter(t -> t.getDataVencimento() != null && t.getDataVencimento().isBefore(LocalDate.now()) && !Boolean.TRUE.equals(t.getConcluida())).count();
            int totalPomodoros = tasks.stream().mapToInt(t -> t.getPomodorosRealizados() != null ? t.getPomodorosRealizados() : 0).sum();
            double taxaConclusao = total > 0 ? (concluidas * 100.0 / total) : 0.0;
            double taxaPontualidade = (total - atrasadas) > 0 && total > 0 ? ((total - atrasadas) * 100.0 / total) : 100.0;

            // Painel de KPIs (4 colunas)
            PdfPTable kpiTable = new PdfPTable(4);
            kpiTable.setWidthPercentage(100);
            kpiTable.setSpacingAfter(15f);

            Color kpiBg = new Color(248, 250, 252);
            adicionarCardKpi(kpiTable, "TOTAL DE TAREFAS", String.valueOf(total), kpiLabelFont, kpiNumberFont, kpiBg);
            adicionarCardKpi(kpiTable, "TAXA DE CONCLUSÃO", String.format("%.1f%%", taxaConclusao) + " (" + concluidas + ")", kpiLabelFont, kpiNumberFont, kpiBg);
            adicionarCardKpi(kpiTable, "PONTUALIDADE", String.format("%.1f%%", taxaPontualidade) + " (" + atrasadas + " atraso)", kpiLabelFont, kpiNumberFont, kpiBg);
            adicionarCardKpi(kpiTable, "POMODOROS CONCLUÍDOS", totalPomodoros + " 🍅", kpiLabelFont, kpiNumberFont, kpiBg);

            document.add(kpiTable);

            // Seção de Detalhamento
            Paragraph sectionTitle = new Paragraph("Detalhamento Operacional de Tarefas", sectionFont);
            sectionTitle.setSpacingAfter(8f);
            document.add(sectionTitle);

            // Tabela Principal de Tarefas
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.0f, 3.5f, 1.6f, 1.8f, 2.2f, 1.8f, 1.8f});

            Color headerColor = new Color(30, 41, 59);
            String[] headers = {"#", "Título & Subtarefas", "Prioridade", "Categoria", "Tags", "Vencimento", "Status"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setBackgroundColor(headerColor);
                cell.setPadding(6f);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);
            }

            Color rowAltColor = new Color(248, 250, 252);
            Color white = Color.WHITE;
            int rowIndex = 0;

            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Task t : tasks) {
                Color bg = (rowIndex % 2 == 0) ? white : rowAltColor;

                // ID
                PdfPCell cId = new PdfPCell(new Phrase(String.valueOf(t.getId()), cellFont));
                cId.setBackgroundColor(bg);
                cId.setHorizontalAlignment(Element.ALIGN_CENTER);
                cId.setPadding(5f);
                table.addCell(cId);

                // Título + Subtarefas
                StringBuilder desc = new StringBuilder(t.getTitulo());
                if (t.getSubtarefas() != null && !t.getSubtarefas().isEmpty()) {
                    long subConcluidas = t.getSubtarefas().stream().filter(s -> Boolean.TRUE.equals(s.getConcluida())).count();
                    desc.append("\n(").append(subConcluidas).append("/").append(t.getSubtarefas().size()).append(" subtarefas)");
                }
                PdfPCell cTitulo = new PdfPCell(new Phrase(desc.toString(), boldCellFont));
                cTitulo.setBackgroundColor(bg);
                cTitulo.setPadding(5f);
                table.addCell(cTitulo);

                // Prioridade
                PdfPCell cPrio = new PdfPCell(new Phrase(t.getPrioridade().name(), cellFont));
                cPrio.setBackgroundColor(bg);
                cPrio.setHorizontalAlignment(Element.ALIGN_CENTER);
                cPrio.setPadding(5f);
                table.addCell(cPrio);

                // Categoria
                PdfPCell cCat = new PdfPCell(new Phrase(t.getCategoria().name(), cellFont));
                cCat.setBackgroundColor(bg);
                cCat.setHorizontalAlignment(Element.ALIGN_CENTER);
                cCat.setPadding(5f);
                table.addCell(cCat);

                // Tags
                String tagsStr = (t.getTags() == null || t.getTags().isEmpty())
                        ? "-"
                        : t.getTags().stream().map(tag -> "#" + tag.getNome()).collect(Collectors.joining(", "));
                PdfPCell cTags = new PdfPCell(new Phrase(tagsStr, cellFont));
                cTags.setBackgroundColor(bg);
                cTags.setPadding(5f);
                table.addCell(cTags);

                // Vencimento
                String venc = t.getDataVencimento() != null ? t.getDataVencimento().format(df) : "Sem prazo";
                PdfPCell cVenc = new PdfPCell(new Phrase(venc, cellFont));
                cVenc.setBackgroundColor(bg);
                cVenc.setHorizontalAlignment(Element.ALIGN_CENTER);
                cVenc.setPadding(5f);
                table.addCell(cVenc);

                // Status
                String statusStr = Boolean.TRUE.equals(t.getConcluida()) ? "CONCLUÍDA" : t.getStatus().name().replace("_", " ");
                PdfPCell cStatus = new PdfPCell(new Phrase(statusStr, boldCellFont));
                cStatus.setBackgroundColor(bg);
                cStatus.setHorizontalAlignment(Element.ALIGN_CENTER);
                cStatus.setPadding(5f);
                table.addCell(cStatus);

                rowIndex++;
            }

            document.add(table);

            // Rodapé
            Paragraph footer = new Paragraph("\nRelatório gerado automaticamente pelo To-do List Pro • Exportação Executiva", subtitleFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório executivo em PDF: " + e.getMessage(), e);
        }
    }

    private void adicionarCardKpi(PdfPTable table, String label, String value, Font labelFont, Font valueFont, Color bg) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setPadding(8f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(new Color(226, 232, 240));

        Paragraph pVal = new Paragraph(value, valueFont);
        pVal.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pVal);

        Paragraph pLab = new Paragraph(label, labelFont);
        pLab.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pLab);

        table.addCell(cell);
    }
}
