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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@WebServlet("/calcularHoras")
public class CalculateHoursServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	/*OBS: Não foram  implementados todas as situação que estão nos exemplos nas informaçoes do teste.
    	 * Os exemplos, 1,2,3 funcionão e parte do 4, com apenas 1 marcação de horario.
    	 * O programa dar bug quando são adicionados multiplas marcações e falha.
    	 * Pois não tive tempo para fazer as correçoes necessárias.
    	 * Como disse parte da lógica funciona para todos os exemplos.
    	 * Como ja foi citado,o que faltaria é fazer a correção no comportamanto da iteração da lista de marcações.
    	 */
    	
    	   List<String> atrasos = new ArrayList<>(); // Inicializa a lista aqui
    	   List<String> horasExtras = new ArrayList<>();
    	   // Cria um array dinâmico para armazenar os valores acumulados
           List<Integer> markerEntryHourList = new ArrayList<>();
    	  
        try {
            // Obter parâmetros do request
            String[] tabelaHorarioParam = request.getParameterValues("tabelaHorario");
            String[] tabelaMarcacoesParam = request.getParameterValues("tabelaMarcacoes");

            // Converter os parâmetros para listas de períodos
            List<String> tabelaHorario = converterHorarios(tabelaHorarioParam);
            List<String> tabelaMarcacoes = converterHorarios(tabelaMarcacoesParam);
            
    
            
         // Lista para armazenar os objetos WorkSchedule
         // São os horarios de trabalho..
            List<WorkSchedule> schedules = new ArrayList<>();
          
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
                
                // Cria a lista para receber os horarios de marcação de ponto
                List<HourMarker> hm = new ArrayList<>();

                // Adiciona os objetos HourMarker à lista
                for (String horario : tabelaMarcacoes) {
                    // Cria instâncias de HourMarker com base nos horários formatados
                    HourMarker hourMarkerList = criarHourSchedule(horario);

                    // Adiciona à lista se não for nulo
                    if (hourMarkerList != null) {
                        hm.add(hourMarkerList);
                    }
                }

                     
               
                if (!hm.isEmpty()) {
                    HourMarker firstHourMarker = hm.get(0);
                    
                    // Coverte as strings dos horarios em inteiros
                    int markerEntryHour = Integer.parseInt(firstHourMarker.getEntryHour().split(":")[0]);
                    int markerEntryMinute = Integer.parseInt(firstHourMarker.getEntryHour().split(":")[1]);

                    int markerDepartureHour = Integer.parseInt(firstHourMarker.getDepartureTime().split(":")[0]);
                    int markerDepartureMinute = Integer.parseInt(firstHourMarker.getDepartureTime().split(":")[1]);
                    
                    
                    System.out.println("Entrada: " + markerEntryHour + ":" + markerEntryMinute);
                    System.out.println("Saída: " + markerDepartureHour + ":" + markerDepartureMinute);
                
                    markerEntryHourList.add(markerEntryHour);
			

			// Vai armazenar horas extras
			String horaExtra = "";
			// Vai armazenar horas atrasadas
			String horaAtraso = "";
			
			int hour = 0;
		
			int lastMinute = 0;
			
			
			//Percorro os horários de trabalho
			for (WorkSchedule schedule : schedules) {
			
				
				int horarioDeEntrada = Integer.parseInt(schedule.getEntryHour().split(":")[0]);
				int minutoDeEntrada = Integer.parseInt(schedule.getEntryHour().split(":")[1]);
				
				int horarioDeSaida = Integer.parseInt(schedule.getDepartureTime().split(":")[0]);
				int minutoDeSaida = Integer.parseInt(schedule.getDepartureTime().split(":")[1]);
				
				

				boolean horaExtraAntecipadaDetectada = false;
				boolean horaDeAtrasoAntecipadaDetectada = false;
				
				boolean horaExtraAposDetectado = false;
				boolean horaDeAtrasoAposDetectado = false;
				
				lastMinute = minutoDeSaida;
				
				
				/**
				 * Iniciando a jornada de trabalho
				 * 
				 * O valor de markerEntryHour é atrinuido a hour.
				 */
				
			      // Faz a ireção para cada hora de marcação digitado no front-end.
			        for (Integer currentHour : markerEntryHourList) {
				    hour = currentHour; // Atribua o valor a 'hour' para que esteja disponível fora do loop

				    for (; 
				            (hour <= markerDepartureHour && currentHour <= markerDepartureHour) || (hour > markerDepartureHour); 
				            hour++) {
				        
				    	if(hour == horarioDeSaida) {
							/**
							 * Marca a chegada ao final do expediente, passa para o proximo horário.
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
				     // Verifica se a hora da marcação for depois da hora de trabalho, caso seja é atraso.
				        if (dataHoraMarcacao.isAfter(dataHoraDeEntrada)) {
				            if (!horaDeAtrasoAntecipadaDetectada) {
				                horaDeAtrasoAntecipadaDetectada = true;
				                
				                // Formatando para sempre ter dois dígitos
				                String formattedEntryHour = String.format("%02d", markerEntryHour);
				                String formattedEntryMinute = String.format("%02d", markerEntryMinute);

				             
				                horaAtraso = schedule.getEntryHour() + " " + formattedEntryHour + ":" + formattedEntryMinute;		                
				                atrasos.add(horaAtraso);

				                System.out.println("Você se atrasou! " + horaAtraso);
				            }
				        } else if (dataHoraMarcacao.isBefore(dataHoraDeEntrada)) {
				        		        		
				     
				            String formattedEntryHour = String.format("%02d", markerEntryHour);
				            String formattedmarkerDepartureHour = String.format("%02d", markerDepartureHour);
				            String formattedEntryMinute = String.format("%02d", markerEntryMinute);
				            
				           
				            // Criando a string formatada para hora extra
				            horaExtra = formattedmarkerDepartureHour + ":" + formattedEntryMinute + " " + schedule.getEntryHour();
                            horasExtras.add(horaExtra);
				            System.out.println("Você se antecipou! " + horaExtra);
				            
				            
				            horaAtraso += formattedEntryHour +" " + schedule.getDepartureTime();
							atrasos.add(horaAtraso);
							System.out.println("Entrou atrasado: " + horaAtraso);
							break;
				        	
				            
				        }

					} else if(hour < horarioDeEntrada && hour < horarioDeSaida) {
						/**
						 * Entrou antes
						 */
						
							horaExtraAntecipadaDetectada = true;
							
							String formattedEntryMinute = String.format("%02d", markerEntryMinute);
							
							
							horaExtra = hour + ":" + formattedEntryMinute;
							
							System.out.println("Hora extra: " + horaExtra);
							
							
						
					} else if(hour == horarioDeEntrada && hour < horarioDeSaida) {
						/**
						 * Entrou no horário
						 */	
						if(horaExtraAntecipadaDetectada) {
							
							/**
							 * Se tiver encontrado hora extra antes quer dizer que tinha algo para colocar na variável
							 */
							//String formattedEntryMinute = String.format("%02d", markerEntryMinute);
							
							horaExtra += " " +  schedule.getEntryHour();
							
							horasExtras.add(horaExtra);
							System.out.println("Hora extra antes do expediente: " + horaExtra);
						} else {
							System.out.println("Entrou no horário");
						}
						
					
					} else if(hour > horarioDeEntrada && hour < horarioDeSaida) {
						//Entrou atrasado
						
						System.out.println("testando.. ");
							
						if(hour > horarioDeEntrada && !horaExtraAntecipadaDetectada) {
							/**
							 * Se não, ele entrou atrasado
							 */
							if(!horaDeAtrasoAntecipadaDetectada) {
								horaDeAtrasoAntecipadaDetectada = true;
								horaAtraso = hour + ":" + markerEntryMinute;						
							} else {
								
							    String formattedEntryHour = String.format("%02d", markerEntryHour);
							    String formattedEntryMinute = String.format("%02d", markerEntryMinute);
								horaAtraso =  schedule.getEntryHour() + " " + formattedEntryHour + ":"+ formattedEntryMinute;  
								atrasos.add(horaAtraso);
								System.out.println("Entrou atrasado: " + horaAtraso);
								break; // break de teste
								
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
							break;
						}
				
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
	        
	     // Limpa as listas após enviar os dados para o front-end
	        atrasos.clear();
	        horasExtras.clear();
	    
                }     
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro interno no servidor.");
        }
        }
	
    /* Converte as Strings vinda do front-end em um formato desejavel para os cálculos. 
       Por exemplo de : ["08:00-12:00"] para "22:00","05:00"
    */
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
    


	/*
	 *  Serve para armazenar os valores formatados nas listas de horario de trabalho.	
	 *  Método para criar instância de WorkSchedule a partir de um horário formatado    
	 */ 
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
      
    /* 
       Serve para armazenar os valores formatados nas listas de horario de trabalho.
    */
    private static HourMarker criarHourSchedule(String horarioFormatado) {
        String[] partes = horarioFormatado.split("-");

        if (partes.length == 2) {
            String horarioEntrada = partes[0];
            String horarioSaida = partes[1];

            // Cria e retorna uma instância de WorkSchedule com os horários formatados
            return new HourMarker(horarioEntrada, horarioSaida);
        } else {
            // Lida com o formato inválido, se necessário
            System.err.println("Formato inválido para criar HourMarker: " + horarioFormatado);
            return null;
        }
    }
			 
	
}

    


   