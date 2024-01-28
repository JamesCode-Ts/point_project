package servlet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.HourMarker;

import model.WorkSchedule;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@WebServlet("/calcularHoras")
public class CalcularHorasServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	   List<String> atrasos = new ArrayList<>(); // Inicializa a lista aqui
    	   List<String> horasExtras = new ArrayList<>();
    	  
        try {
            // Obter parâmetros do request
            String[] tabelaHorarioParam = request.getParameterValues("tabelaHorario");
            // String[] tabelaMarcacoesParam = request.getParameterValues("tabelaMarcacoes");

            // Converter os parâmetros para listas de períodos
            List<String> tabelaHorario = converterHorarios(tabelaHorarioParam);
           // List<Periodo> tabelaMarcacoes = converterStringParaLista(tabelaMarcacoesParam);
     
          
     

            
         // Lista para armazenar os objetos WorkSchedule
            List<WorkSchedule> schedules = new ArrayList<>();
           //    List<String> horarioFormatadoMaracao = converterHorariosMarcacao(tabelaMarcacoesParam);      
                // Adiciona os objetos WorkSchedule à lista
                for (String horario : tabelaHorario) {
                    // Cria instâncias de WorkSchedule com base nos horários formatados
                    WorkSchedule workSchedule = criarWorkSchedule(horario);

                    // Adiciona à lista se não for nulo
                    if (workSchedule != null) {
                        schedules.add(workSchedule);
                    }
                    for (WorkSchedule schedule : schedules) {
                        System.out.println(schedule.toString());
                    }
                }
                
              
            
           // schedules.add(new WorkSchedule("22:00","05:00"));
			
	     	HourMarker hm = new HourMarker("03:00", "07:00");
			
			int markerEntryHour = Integer.parseInt(hm.getEntryHour().split(":")[0]);
			int markerEntryMinute = Integer.parseInt(hm.getEntryHour().split(":")[1]);
			
			int markerDepartureHour = Integer.parseInt(hm.getDepartureTime().split(":")[0]);
			int markerDepartureMinute = Integer.parseInt(hm.getDepartureTime().split(":")[1]);
			

			//
			 System.out.println(markerEntryHour);
			 System.out.println(markerEntryMinute);
			 
			 System.out.println(markerDepartureHour);
			 
			 System.out.println(markerDepartureMinute);
			

			//Guarda a hora extra
			String horaExtra = "";
			
			//Guarda a hora de atraso
			String horaAtraso = "";
			
			//Início da jornada
			int hour = 0;
			
			//Guarda o último minuto processado
			int lastMinute = 0;
			
			
			//Percorro os horários de trabalho
			for (WorkSchedule schedule : schedules) {
				boolean horaExtraAntecipadaDetectada = false;
				boolean horaDeAtrasoAntecipadaDetectada = false;
				
				boolean horaExtraAposDetectado = false;
				boolean horaDeAtrasoAposDetectado = false;
				
				int horarioDeEntrada = Integer.parseInt(schedule.getEntryHour().split(":")[0]);
				int minutoDeEntrada = Integer.parseInt(schedule.getEntryHour().split(":")[1]);
				
				int horarioDeSaida = Integer.parseInt(schedule.getDepartureTime().split(":")[0]);
				int minutoDeSaida = Integer.parseInt(schedule.getDepartureTime().split(":")[1]);
				
				lastMinute = minutoDeSaida;
				
				
				/**
				 * Jornada de Trabalho do funcionário
				 * 
				 * Se hour for == 0, então quer dizer que ele iniciou o cálculo agora, mas caso já exista um valor, continua de onde parou
				 * Casos como uma jornada que passa de um turno para o outro por exemplo....
				 */
				for(hour = hour == 0 ? markerEntryHour : hour; hour <= markerDepartureHour; hour++) {
					if(hour == horarioDeSaida) {
						/**
						 * Isto marca a chegada ao final do expediente, então passa para o próximo horário, caso exista
						 */
						break;
					}
					
					/**
					 * Se passou o dia, o cálculo vai precisar ser um pouco diferente...
					 * Pois precisaremos pegar o horário de início, de término e verificar 
					 * quanto o funcionário se atrasou
					 * 
					 * Se for maior, quer dizer que passou de um dia para o outro
					 * Ex: 22:00 05:00 | 19:00 01:00 | 18:00 02:00
					 */
					if(horarioDeEntrada > horarioDeSaida) {
						//Aqui verifica se eu cheguei antes ou depois do horário
						GregorianCalendar calendar = new GregorianCalendar();
						
						LocalDateTime dataHoraDeEntrada =  LocalDateTime.of(calendar.get(GregorianCalendar.YEAR), 
								  calendar.get(GregorianCalendar.MONTH)+1, 
								  calendar.get(GregorianCalendar.DAY_OF_MONTH), 
								  horarioDeEntrada, 
								  minutoDeEntrada);
						
						LocalDateTime dataHoraMarcacao =  LocalDateTime.of(calendar.get(GregorianCalendar.YEAR), 
								  calendar.get(GregorianCalendar.MONTH)+1, //Iguala
								  calendar.get(GregorianCalendar.DAY_OF_MONTH), 
								  markerEntryHour, 
								  markerEntryMinute);
						
						
						/**
						 * Como não foi fornecido um campo de data para fazer a comparação se passou de um dia para o outro, 
						 * sempre que for um valor muito menor que a o horário de entrada, vou assumir que será um novo dia...
						 */
						
				        long diferencaEmHoras = Math.abs(ChronoUnit.HOURS.between(dataHoraDeEntrada, dataHoraMarcacao));
				        if(diferencaEmHoras > 6) { //"Permite" até 6 horas de extra....
				        	if (dataHoraMarcacao.isBefore(dataHoraDeEntrada)) {
				        		dataHoraMarcacao = dataHoraMarcacao.plusDays(1);
				        	}
				        }
						
				        /**
				         * Verificando as datas, e aqui será levado em consideração o dia....
				         */
				     // Antes do loop, inicialize a lista de atrasos
				       
				        if (dataHoraMarcacao.isAfter(dataHoraDeEntrada)) {
				            if (!horaDeAtrasoAntecipadaDetectada) {
				                horaDeAtrasoAntecipadaDetectada = true;
				                
				                // Formatando para sempre ter dois dígitos
				                String formattedEntryHour = String.format("%02d", markerEntryHour);
				                String formattedEntryMinute = String.format("%02d", markerEntryMinute);

				                // Criando a string formatada para hora de atraso
				                horaAtraso = schedule.getEntryHour() + " " + formattedEntryHour + ":" + formattedEntryMinute;
				                
				                
				                atrasos.add(horaAtraso);

				                System.out.println("Você se atrasou! " + horaAtraso);
				            }
				        } else if (dataHoraMarcacao.isBefore(dataHoraDeEntrada)) {
				            // Formatando para sempre ter dois dígitos
				            String formattedEntryHour = String.format("%02d", markerEntryHour);
				            String formattedEntryMinute = String.format("%02d", markerEntryMinute);

				            // Criando a string formatada para hora extra
				            horaExtra = formattedEntryHour + ":" + formattedEntryMinute + " " + schedule.getEntryHour();

				            System.out.println("Você se antecipou! " + horaExtra);
				        }

					} else if(hour < horarioDeEntrada && hour < horarioDeSaida) {
						/**
						 * Entrou antes
						 */
						if(!horaExtraAntecipadaDetectada) {
							horaExtraAntecipadaDetectada = true;
							horaExtra = hour + ":" + markerEntryMinute;
						}
					} else if(hour == horarioDeEntrada && hour < horarioDeSaida) {
						/**
						 * Entrou no horário
						 */	
						if(horaExtraAntecipadaDetectada) {
							/**
							 * Se tiver encontrado hora extra antesm quer dizer que tinha algo para colocar na variável
							 */
							
							
							horaExtra += " " + schedule.getEntryHour();
							System.out.println("Hora extra antes do expediente: " + horaExtra);
						} else {
							System.out.println("Entrou no horário");
						}
					
					} else if(hour > horarioDeEntrada && hour < horarioDeSaida) {
						//Se ele encontrou hora extra antes, quer dizer que o funcionário chegou antes do horário...
						/**
						 * Entrou Atrasado
						 */
						
						if(hour > horarioDeEntrada && !horaExtraAntecipadaDetectada) {
							/**
							 * Se não, ele entrou atrasado
							 */
							if(!horaDeAtrasoAntecipadaDetectada) {
								horaDeAtrasoAntecipadaDetectada = true;
								horaAtraso = hour + ":" + markerEntryMinute;
							} else {
								horaAtraso += " " + schedule.getDepartureTime();
								atrasos.add(horaAtraso);
								System.out.println("Entrou atrasado: " + horaAtraso);
							}
						} else if(hour == markerDepartureHour) {
							/* Se a hora for igual ao horário de saída marcado, então quer dizer que 
							 * o funcionário saiu, mas ainda deveria estar no expediente dele e será considerado 
							 * um atraso
							 */
							horaDeAtrasoAposDetectado = true;
							
							String formattedDepartureMinuter= String.format("%02d", markerDepartureMinute);
							
							horaAtraso =  hour + ":" + formattedDepartureMinuter + " " + schedule.getDepartureTime();
							atrasos.add(horaAtraso);
							System.out.println("Saiu mais cedo: " + horaAtraso);
						}
					
					} else if(hour > horarioDeEntrada && hour > horarioDeSaida) {
						/**
						 * Saiu depois do horário
						 */	
						if(!horaExtraAposDetectado) {
							/**
							 * Pega o início da hora extra
							 */
							horaExtraAposDetectado = true;
							horaExtra = schedule.getDepartureTime();
						} else {
							/**
							 * Fica atualizando até achar o final da hora extra
							 */
							  // Formatando para sempre ter dois dígitos
			                String formattedDepartureMinute= String.format("%02d", markerDepartureMinute);
			                String formattedhour= String.format("%02d", hour);

							horaExtra += " " + formattedhour + ":" + formattedDepartureMinute;
							horasExtras.add(horaExtra);
							System.out.println("Hora extra após o expediente: " + horaExtra);
						}
					}
				}
			}
			
			if(hour < markerDepartureHour) {
				/**
				 * Caso ainda tenham sobrado horas, serão horas extras após o expediente
				 */
				
				String formattedDepartureHour= String.format("%02d", markerDepartureHour);
				String formattedDepartureMinuter= String.format("%02d", markerDepartureMinute);
                String formattedhour= String.format("%02d", hour);
                String formattelastMinute= String.format("%02d", lastMinute);
                
				String horasExtrasRestantes = formattedhour + ":" + formattelastMinute + " " +  formattedDepartureHour + ":" + formattedDepartureMinuter; 
				horasExtras.add(horasExtrasRestantes);
				System.out.println("Hora extra após expediente: " + horasExtrasRestantes);
			}



		    // Enviar os resultados para o front-end
	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");

	        // Cria um PrintWriter para escrever na resposta
	        PrintWriter out = response.getWriter();

	        // Converte as listas para arrays JSON
	        JsonArray atrasosJsonArray = new JsonArray();
	        atrasos.forEach(atraso -> atrasosJsonArray.add(atraso));

	        JsonArray horasExtrasJsonArray = new JsonArray();
	        horasExtras.forEach(horaExtras -> horasExtrasJsonArray.add(horaExtras));

	        // Cria um objeto JSON que contém as listas de atrasos e horasExtras
	        JsonObject jsonResponse = new JsonObject();
	        jsonResponse.add("atrasos", atrasosJsonArray);
	        jsonResponse.add("horasExtras", horasExtrasJsonArray);
            
	        Gson gson = new Gson();
	        // Converte o objeto JSON para uma string JSON
	        String jsonResponseString = gson.toJson(jsonResponse);

	        // Envia a resposta para o front-end
	        out.println(jsonResponseString);

	        // Fecha o PrintWriter
	        out.close();
	    
			
			
		
			
            
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro interno no servidor.");
        }
    }

    // Adicione aqui seus métodos converterHorarios, calcularAtraso, calcularHoraExtra, etc.


	
	
    public static List<String> converterHorarios(String[] horarios) {
        List<String> horariosFormatados = new ArrayList<>();

        for (String horario : horarios) {
            // Remove os colchetes "[" e "]" se estiverem presentes
            horario = horario.replaceAll("\\[|\\]", "");

            // Remove as aspas duplas
            horario = horario.replaceAll("\"", "");

            String[] horariosSeparados = horario.split(",");
            for (String horarioSeparado : horariosSeparados) {
                // Remove espaços em branco antes e depois do horário e adiciona ao resultado formatado
                horariosFormatados.add(horarioSeparado.trim());
            }
        }
        return horariosFormatados;
    }


			    
    // Método para criar instância de WorkSchedule a partir de um horário formatado
    private static WorkSchedule criarWorkSchedule(String horarioFormatado) {
        String[] partes = horarioFormatado.split("-");

        if (partes.length == 2) {
            String horarioEntrada = partes[0];
            String horarioSaida = partes[1];

            // Cria e retorna uma instância de WorkSchedule com os horários formatados
            return new WorkSchedule(horarioEntrada, horarioSaida);
        } else {
            // Lida com o formato inválido, se necessário
            System.err.println("Formato inválido para criar WorkSchedule: " + horarioFormatado);
            return null;
        }
    }
			 

			//Exemplo 1
			
			// Correto!
		//	schedules.add(new WorkSchedule("08:00","12:00"));
		//	HourMarker hm = new HourMarker("07:00", "11:00");
			
			//Exemplo 2 Correto!
		//	schedules.add(new WorkSchedule("08:00","12:00"));
		//	HourMarker hm = new HourMarker("07:00", "11:00");
			
			//Exemplo 3 Correto!
		//	schedules.add(new WorkSchedule("08:00","12:00"));
		//	schedules.add(new WorkSchedule("13:30","17:30"));
			
	//		HourMarker hm = new HourMarker("06:00", "20:00");
		
			
			//Exemplo 4
			
	
}

    


   




