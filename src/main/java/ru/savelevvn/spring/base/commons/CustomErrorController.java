package ru.savelevvn.spring.base.commons;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Получаем статус ошибки
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String errorMessage = "An unexpected error occurred";
        String errorTitle = "Error";

        if (statusCode != null) {
            // Устанавливаем заголовок и сообщение в зависимости от статуса
            switch (statusCode) {
                case 400:
                    errorTitle = "Bad Request";
                    errorMessage = "Your request contains invalid syntax";
                    break;
                case 401:
                    errorTitle = "Unauthorized";
                    errorMessage = "Authentication required";
                    break;
                case 403:
                    errorTitle = "Access Denied";
                    errorMessage = "You don't have permission to access this resource";
                    break;
                case 404:
                    errorTitle = "Page Not Found";
                    errorMessage = "The page you are looking for might have been removed or is temporarily unavailable";
                    break;
                case 500:
                    errorTitle = "Internal Server Error";
                    errorMessage = "Something went wrong on our server. Please try again later";
                    break;
                default:
                    errorTitle = HttpStatus.valueOf(statusCode).getReasonPhrase();
            }

            model.addAttribute("errorCode", statusCode);
        } else {
            // Статус неизвестен, используем значение по умолчанию
            model.addAttribute("errorCode", 500);
        }

        model.addAttribute("errorTitle", errorTitle);
        model.addAttribute("errorMessage", errorMessage);

        System.out.println("Error handled: " + statusCode + " - " + errorTitle);

        return "error";
    }
}