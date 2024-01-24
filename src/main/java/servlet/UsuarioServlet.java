package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Usuario;


@WebServlet("/usuario")
public class UsuarioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
        // Obter parâmetros do formulário
        String entrada = request.getParameter("nome");
        String saida = request.getParameter("email");

        // Realizar alguma lógica com os dados (por exemplo, salvar em um banco de dados)
         System.out.println(entrada);

        // Construir a resposta para a requisição Ajax
        String resposta = "Entradas e Saídas recebidas:\n Entrada: " + entrada + "\nSaida: " + saida;

        // Enviar a resposta de volta para a página HTML
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        out.print(resposta);
        out.flush();
    }
}