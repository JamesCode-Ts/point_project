package servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.Periodo;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
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
            String[] tabelaHorarioParam = request.getParameterValues("tabelaHorario");
            String[] tabelaMarcacoesParam = request.getParameterValues("tabelaMarcacoes");

        
            // Converter os parâmetros para listas de períodos
            List<Periodo> tabelaHorario = converterStringParaLista(tabelaHorarioParam);
            List<Periodo> tabelaMarcacoes = converterStringParaLista(tabelaMarcacoesParam);

            // Calcular a subtração paraatrasos e horas extras
            List<Periodo> atrasos = calcularAtraso(tabelaHorario, tabelaMarcacoes);
            List<Periodo> horasExtras = calcularHoraExtra(tabelaHorario, tabelaMarcacoes, false);

            // Enviar os resultados para o front-end
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // Configurar Gson para lidar com a serialização de datas
            Gson gson = new GsonBuilder().setDateFormat("HH:mm").create();

            // Criar um objeto JSON que contenha ambas as listas
            String resultadoJson = gson.toJson(new Resultado(atrasos, horasExtras));

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

    private List<Periodo> calcularHoraExtra(List<Periodo> tabelaHorario, List<Periodo> tabelaMarcacoes, boolean b) {
        List<Periodo> horasExtras = new ArrayList<>();

        for (Periodo periodoMarcacao : tabelaMarcacoes) {
            boolean coberto = false;

            for (Periodo periodoTrabalho : tabelaHorario) {
                // Verificar se há sobreposição entre os períodos
                if (periodoTrabalho.getInicio().before(periodoMarcacao.getFim()) && periodoTrabalho.getFim().after(periodoMarcacao.getInicio())) {
                    // Calcular a hora extra
                    Date inicioHoraExtra = periodoTrabalho.getInicio().after(periodoMarcacao.getInicio()) ? periodoTrabalho.getInicio() : periodoMarcacao.getInicio();
                    Date fimHoraExtra = periodoTrabalho.getFim().before(periodoMarcacao.getFim()) ? periodoTrabalho.getFim() : periodoMarcacao.getFim();

                    Periodo horaExtra = new Periodo(inicioHoraExtra, fimHoraExtra);
                    horasExtras.add(horaExtra);
                    coberto = true;
                }
            }

            if (!coberto) {
                horasExtras.add(periodoMarcacao);
            }
        }

        return horasExtras;
    }
    private List<Periodo> converterStringParaLista(String[] array) throws ParseException {
        List<Periodo> lista = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        if (array != null) {
            for (String item : array) {
                String horario = item.replace("[", "").replace("]", ""); // Remover colchetes
                String[] horarios = horario.trim().split(","); // Dividir por vírgula, se necessário

                for (String periodo : horarios) {
                    String[] partes = periodo.trim().split("-");

                    if (partes.length == 2) {
                        String inicioStr = partes[0].trim().replaceFirst("\"", ""); // Remover a primeira aspas dupla
                        String fimStr = partes[1].trim();

                        Date inicio = sdf.parse(inicioStr);
                        Date fim = sdf.parse(fimStr);
                        lista.add(new Periodo(inicio, fim));
                    } else {
                        throw new ParseException("Formato inválido para o período: " + periodo, 0);
                    }
                }
            }
        }

        return lista;
    }

    private class Resultado {
        private final List<Periodo> atrasos;
        private final List<Periodo> horasExtras;

        public Resultado(List<Periodo> atrasos, List<Periodo> horasExtras) {
            this.atrasos = atrasos;
            this.horasExtras = horasExtras;
        }

        public List<Periodo> getAtrasos() {
            return atrasos;
        }

        public List<Periodo> getHorasExtras() {
            return horasExtras;
        }
    }

    private List<Periodo> calcularAtraso(List<Periodo> tabelaHorario, List<Periodo> tabelaMarcacoes) {
        List<Periodo> atrasos = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

        for (Periodo periodoTrabalho : tabelaHorario) {
            for (Periodo periodoMarcacao : tabelaMarcacoes) {
                // Verificar se há sobreposição entre os períodos
                if (periodoTrabalho.getFim().after(periodoMarcacao.getInicio()) && periodoTrabalho.getInicio().before(periodoMarcacao.getFim())) {
                    // Calcular o atraso
                    Date inicioAtraso = periodoMarcacao.getFim();
                    Date fimAtraso = periodoTrabalho.getFim();
                    Periodo atraso = new Periodo(inicioAtraso, fimAtraso);
                    atrasos.add(atraso);
                }
            }
        }

        return atrasos;
    }
/*
    private List<Periodo> calcularSubtracao(List<Periodo> tabela1, List<Periodo> tabela2, boolean isAtraso) {
        List<Periodo> resultado = new ArrayList<>();

        for (Periodo periodo1 : tabela1) {
            boolean coberto = false;

            for (Periodo periodo2 : tabela2) {
                // Verificar a sobreposição entre os períodos
                if (periodo1.getFim().after(periodo2.getInicio()) && periodo1.getInicio().before(periodo2.getFim())) {
                    coberto = true;

                    // Calcular atraso ou hora extra com base na lógica correta
                    Date inicioSobreposicao = periodo1.getInicio().before(periodo2.getInicio()) ? periodo2.getInicio() : periodo1.getInicio();
                    Date fimSobreposicao = periodo1.getFim().before(periodo2.getFim()) ? periodo1.getFim() : periodo2.getFim();

                    if (isAtraso && periodo1.getInicio().before(periodo2.getInicio())) {
                        // Atraso
                        resultado.add(new Periodo(inicioSobreposicao, fimSobreposicao));
                    } else if (!isAtraso && periodo1.getFim().after(periodo2.getFim())) {
                        // Hora extra
                        resultado.add(new Periodo(inicioSobreposicao, fimSobreposicao));
                    }
                }
            }

            if (!coberto) {
                if (isAtraso) {
                    resultado.add(periodo1);
                } else {
                    // Adicionar um período vazio
                    resultado.add(new Periodo(null, null));
                }
            }
        }

        return resultado;

}

*/
    
}
