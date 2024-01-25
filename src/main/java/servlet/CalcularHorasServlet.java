package servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.Periodo;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@WebServlet("/calcularHoras")
public class CalcularHorasServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Obter parâmetros do request
            String tabelaHorarioParam = request.getParameter("tabelaHorario");
            String tabelaMarcacoesParam = request.getParameter("tabelaMarcacoes");

            // Converter os parâmetros para listas de períodos
            List<Periodo> tabelaHorario = converterStringParaLista(tabelaHorarioParam);
            List<Periodo> tabelaMarcacoes = converterStringParaLista(tabelaMarcacoesParam);

            // Calcular a subtração para atrasos e horas extras
            List<Periodo> atrasos = calcularSubtracao(tabelaHorario, tabelaMarcacoes, true);
            List<Periodo> horasExtras = calcularSubtracao(tabelaHorario, tabelaMarcacoes, false);

            // Enviar os resultados para o front-end
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // Configurar Gson para lidar com a serialização de datas
            Gson gson = new GsonBuilder().setDateFormat("HH:mm").create();

            // Converter as listas de objetos Periodo para JSON
            String atrasosJson = gson.toJson(atrasos);
            String horasExtrasJson = gson.toJson(horasExtras);

            // Criar um objeto JSON que contenha ambas as listas
            String resultadoJson = String.format("{\"atrasos\": %s, \"horasExtras\": %s}", atrasosJson, horasExtrasJson);

            // Escrever o JSON no response
            response.getWriter().write(resultadoJson);
        } catch (ParseException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Erro na formatação dos horários.");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro interno no servidor.");
        }
    }

    private List<Periodo> converterStringParaLista(String input) throws ParseException {
        List<Periodo> lista = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        String[] periodos = input.split(",");

        for (String periodo : periodos) {
            String[] horarios = periodo.split("-");
            Date inicio = sdf.parse(horarios[0]);
            Date fim = sdf.parse(horarios[1]);
            lista.add(new Periodo(inicio, fim));
        }

        return lista;
    }

    private String formatarData(Date data) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(data);
    }

    private List<Periodo> calcularSubtracao(List<Periodo> tabelaHorario, List<Periodo> tabelaMarcacoes, boolean isAtraso) {
        List<Periodo> resultado = new ArrayList<>();

        for (Periodo horario : tabelaHorario) {
            for (Periodo marcacao : tabelaMarcacoes) {
                if (horario.getFim().before(marcacao.getInicio()) || horario.getInicio().after(marcacao.getFim())) {
                    // Não há sobreposição, não há atraso nem hora extra
                } else {
                    // Calcular sobreposição entre horário e marcação
                    Date inicioSobreposicao = horario.getInicio().before(marcacao.getInicio()) ? marcacao.getInicio() : horario.getInicio();
                    Date fimSobreposicao = horario.getFim().before(marcacao.getFim()) ? horario.getFim() : marcacao.getFim();

                    if (isAtraso && horario.getInicio().before(marcacao.getInicio())) {
                        // Atraso
                        resultado.add(new Periodo(inicioSobreposicao, fimSobreposicao));
                    } else if (!isAtraso && horario.getInicio().after(marcacao.getInicio())) {
                        // Hora extra
                        resultado.add(new Periodo(inicioSobreposicao, fimSobreposicao));
                    }
                }
            }
        }

        return resultado;
    }
}
